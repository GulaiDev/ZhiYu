package com.rdbb.zhiyu.core.network.interceptor

import com.rdbb.zhiyu.core.datastore.UserManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * 认证拦截器：自动为每个请求附加 Token
 */
class AuthInterceptor @Inject constructor(
    private val userManager: UserManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        // OkHttp 拦截器是同步的，用 runBlocking 从 DataStore 读取一次 token
        val token = runBlocking { userManager.getToken() }
        val request = chain.request().newBuilder()
            .apply {
                if (!token.isNullOrBlank()) {
                    addHeader("Authorization", "Bearer $token")
                }
            }
            .build()
        return chain.proceed(request)
    }
}