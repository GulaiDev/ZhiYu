package com.rdbb.zhiyu.core.network.interceptor

import android.util.Log
import com.rdbb.zhiyu.BuildConfig
import com.rdbb.zhiyu.core.auth.AuthSessionManager
import com.rdbb.zhiyu.core.datastore.UserManager
import com.rdbb.zhiyu.core.network.refresh.RefreshApi
import com.rdbb.zhiyu.core.network.refresh.RefreshRequest
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 401 认证器：业务请求返回 401（access_token 过期）时，自动用 refresh_token
 * 通过 [RefreshApi] 调用 /api/v1/auth/refresh 换取新的 access_token，并用新
 * token 重试原请求。
 *
 * 关键设计：
 * 1. [refreshApi] 基于 NetworkModule 中 @Named("refreshRetrofit") 的独立链路
 *    创建 —— 底层 OkHttp（@Named("refreshOkHttp")）不挂 AuthInterceptor，并显式
 *    设置 Authenticator.NONE，刷新接口自身 401 时不会再次进入本认证器，杜绝递归刷新。
 * 2. [lock] + token 比对保证多个并发请求同时 401 时只刷新一次，其余线程直接
 *    复用新 token 重放；synchronized 不会让网络请求变成非阻塞，因此刷新客户端
 *    在 NetworkModule 中配置了较短且合理的超时，避免断网时并发请求集体卡死。
 * 3. 失败分类处理：只有服务端明确判定 refresh_token 失效（HTTP 401）或本地已无
 *    refresh_token 时，才通过 [AuthSessionManager] 清除登录态；网络断开、
 *    服务器错误（5xx）、参数错误（4xx）、响应数据异常等不确定情况一律保留
 *    凭证并放弃本次重试（返回 null），交由上层按普通 401 处理。
 * 4. 公开接口守卫与本地存储容错：原请求未携带 Bearer Token（登录、注册等
 *    公开接口的 401）时直接放弃，不发起刷新；UserManager 读写与会话清理的
 *    本地异常均被捕获，不会穿透 OkHttp 调度线程；新 access_token 持久化失败
 *    时不使用尚未落盘的 token 重放请求。
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val userManager: UserManager,
    private val authSessionManager: AuthSessionManager,
    private val refreshApi: RefreshApi,
) : Authenticator {

    private val lock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        // 限制重试次数：count>=2 说明带新 token 重试过一次后仍然 401，放弃
        if (responseCount(response) >= MAX_RESPONSE_COUNT) {
            return null
        }

        // 防御性判断：刷新接口走独立客户端正常不会进入这里；若其他链路直接
        // 请求刷新接口且返回 401，也必须立即放弃，防止递归刷新
        if (response.request.url.encodedPath.endsWith(REFRESH_PATH)) {
            return null
        }

        // 原请求未携带 Bearer Token（如登录、注册等公开接口返回 401）时，
        // 不触发刷新流程：避免无意义的刷新请求，也避免本地无凭证时误终结会话
        val requestToken = response.request
            .header(AUTHORIZATION)
            ?.takeIf { it.startsWith(BEARER_PREFIX, ignoreCase = true) }
            ?.substring(BEARER_PREFIX.length)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: return null

        return synchronized(lock) {
            val currentToken = try {
                runBlocking { userManager.getToken() }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // 本地存储读取异常时无法判断凭证状态，保留登录态并放弃本次重试
                logDebug("failed to read access token: ${e.javaClass.simpleName}")
                return@synchronized null
            }

            // 本地 token 与失败请求携带的 token 不一致，说明其他并发请求已经
            // 完成刷新：无需重复刷新，直接用最新 access_token 重放原请求
            if (!currentToken.isNullOrBlank() && currentToken != requestToken) {
                return@synchronized response.request.newBuilder()
                    .header(AUTHORIZATION, "$BEARER_PREFIX$currentToken")
                    .build()
            }

            val refreshToken = try {
                runBlocking { userManager.getRefreshToken() }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // 读取失败不能证明凭证失效，保留登录态，放弃本次重试
                logDebug("failed to read refresh token: ${e.javaClass.simpleName}")
                return@synchronized null
            }
            if (refreshToken.isNullOrBlank()) {
                // 本地已无 refresh_token，确认无法继续认证，终结会话
                expireSessionSafely()
                return@synchronized null
            }

            val newAccessToken = requestNewAccessToken(refreshToken)
                ?: return@synchronized null

            // 后端刷新接口只下发新的 access_token，refresh_token 保持不变。
            // 持久化失败时不使用尚未落盘的 token 重放，避免进程重建后凭证丢失
            try {
                runBlocking { userManager.saveToken(newAccessToken) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logDebug("failed to persist new access token: ${e.javaClass.simpleName}")
                return@synchronized null
            }

            response.request.newBuilder()
                .header(AUTHORIZATION, "$BEARER_PREFIX$newAccessToken")
                .build()
        }
    }

    /**
     * 通过 [RefreshApi] 同步调用刷新接口换取新的 access_token。
     *
     * @return 新 access_token；无法刷新时返回 null。
     * 仅当服务端明确返回 401（refresh_token 无效/过期）时终结会话；
     * 网络故障、服务端错误、响应异常均保留登录凭证。
     */
    private fun requestNewAccessToken(refreshToken: String): String? {
        val apiResponse = try {
            // authenticate 在 OkHttp 调度线程中同步执行，且刷新期间持有 lock，
            // 用 runBlocking 桥接 suspend 接口；刷新客户端的 callTimeout 兜底，
            // 防止断网/弱网时其他并发 401 请求长时间阻塞
            runBlocking { refreshApi.refresh(RefreshRequest(refreshToken)) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            if (e.code() == HTTP_UNAUTHORIZED) {
                // refresh_token 被服务端明确判定无效或过期，会话无法继续
                expireSessionSafely()
            } else {
                // 其他 4xx（如 422 参数错误）/ 5xx 服务器错误：
                // 均不能证明凭证失效，保留登录态，放弃本次重试
                logDebug("refresh failed with HTTP ${e.code()}")
            }
            return null
        } catch (e: IOException) {
            // 网络故障不代表登录过期，保留登录态，网络恢复后仍可重试
            logDebug("refresh aborted by network error: ${e.javaClass.simpleName}")
            return null
        } catch (e: SerializationException) {
            // 响应数据异常（非法 JSON / 结构不符），可能是服务端临时故障，
            // 不能据此判定登录失效
            logDebug("refresh response parse failed: ${e.javaClass.simpleName}")
            return null
        } catch (e: Exception) {
            // 其他未预期异常同样不能证明凭证失效：保留登录态并放弃本次重试，
            // 让原 401 响应正常返回给上层，避免异常向上穿透 OkHttp 调度线程
            logDebug("refresh aborted unexpectedly: ${e.javaClass.simpleName}")
            return null
        }

        // 统一响应包：成功时 code=0（后端约定，见后端 exception_handlers.py），
        // 新令牌位于 data.access_token
        if (apiResponse.code != BIZ_CODE_SUCCESS) {
            logDebug("refresh business code is not success: ${apiResponse.code}")
            return null
        }

        val accessToken = apiResponse.data?.accessToken?.trim().orEmpty()
        return accessToken.ifBlank {
            logDebug("refresh response missing access_token")
            null
        }
    }

    /** 统计该响应经过的重试次数（含自身），用于阻止刷新成功后仍 401 的死循环 */
    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }

    /** 终结会话；本地清理失败不应穿透到 OkHttp 调度线程 */
    private fun expireSessionSafely() {
        try {
            runBlocking { authSessionManager.sessionExpired() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logDebug("failed to expire session: ${e.javaClass.simpleName}")
        }
    }

    private fun logDebug(message: String) {
        if (BuildConfig.DEBUG) {
            Log.w(TAG, message)
        }
    }

    companion object {
        private const val TAG = "TokenAuthenticator"

        private const val AUTHORIZATION = "Authorization"
        private const val BEARER_PREFIX = "Bearer "

        /** OkHttp 最多带新凭证重试一次：首次 401 + 重试 401 即放弃 */
        private const val MAX_RESPONSE_COUNT = 2

        private const val HTTP_UNAUTHORIZED = 401

        /** 后端统一响应体成功码：成功为 0，失败为对应 HTTP 状态码 */
        private const val BIZ_CODE_SUCCESS = 0

        /** 刷新接口路径，与 RefreshApi 中 @POST("api/v1/auth/refresh") 对应；用于递归判断 */
        private const val REFRESH_PATH = "/api/v1/auth/refresh"
    }
}
