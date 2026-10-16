package com.rdbb.zhiyu.core.data.repository

import com.rdbb.zhiyu.core.data.SessionManager
import com.rdbb.zhiyu.core.network.ApiService
import com.rdbb.zhiyu.core.network.LoginRequest
import com.rdbb.zhiyu.core.network.RegisterRequest
import com.rdbb.zhiyu.core.network.UserBrief
import com.rdbb.zhiyu.core.network.UserDetail
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiService,
    private val session: SessionManager,
    json: Json
) : BaseRepository(session, json) {

    suspend fun login(username: String, password: String): Result<UserBrief> {
        val result = safeCall { api.login(LoginRequest(username.trim(), password)) }
        result.getOrNull()?.let {
            session.saveLogin(it.accessToken, it.refreshToken, it.user)
            return Result.success(it.user)
        }
        return Result.failure(result.exceptionOrNull() ?: IllegalStateException("登录失败"))
    }

    suspend fun register(username: String, password: String, nickname: String?): Result<UserBrief> {
        val result = safeCall {
            api.register(
                RegisterRequest(
                    username = username.trim(),
                    password = password,
                    nickname = nickname?.trim()?.ifBlank { null }
                )
            )
        }
        result.getOrNull()?.let {
            session.saveLogin(it.accessToken, it.refreshToken, it.user)
            return Result.success(it.user)
        }
        return Result.failure(result.exceptionOrNull() ?: IllegalStateException("注册失败"))
    }

    suspend fun me(): Result<UserDetail> = safeCall { api.me() }

    suspend fun logout() = session.logout()
}
