package com.rdbb.zhiyu.core.network.interceptor

import com.rdbb.zhiyu.core.datastore.UserManager
import com.rdbb.zhiyu.core.model.RefreshRequest
import com.rdbb.zhiyu.core.network.api.AuthApiService
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val userManager: UserManager,
    @Named("NoAuth") private val authApi: AuthApiService
) : Authenticator {

    private val lock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        // 防止无限重试：刷新过一次仍然 401，视为登录态失效，清除本地 token
        if (responseCount(response) >= 2) {
            runBlocking { userManager.clearTokens() }
            return null
        }

        // 登录/刷新接口不处理
        if (response.request.url.encodedPath.contains("auth/")) {
            return null
        }

        synchronized(lock) {
            // 再次检查当前 token 是否已经被其他线程刷新过
            val currentToken = runBlocking { userManager.getToken() }
            val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")

            // 如果 token 已经变了，说明别人已经刷新成功，直接用新 token 重试
            if (currentToken != null && currentToken != requestToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            // 真正去刷新
            val newToken = refreshToken()
            return if (newToken != null) {
                response.request.newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .build()
            } else {
                // refresh_token 也失效：refreshToken() 内已清除登录态，UI 通过 tokenFlow 感知
                null
            }
        }
    }

    private fun refreshToken(): String? = runBlocking {
        val refresh = userManager.getRefreshToken() ?: return@runBlocking null
        try {
            // 后端刷新接口只下发新的 access_token，refresh_token 保持不变
            val accessToken = authApi.refresh(RefreshRequest(refresh)).data?.accessToken
                ?: run {
                    userManager.clearTokens()
                    return@runBlocking null
                }
            userManager.saveToken(accessToken)
            accessToken
        } catch (e: Exception) {
            userManager.clearTokens()
            null
        }
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var prior = response.priorResponse
        while (prior != null) {
            result++
            prior = prior.priorResponse
        }
        return result
    }
}
