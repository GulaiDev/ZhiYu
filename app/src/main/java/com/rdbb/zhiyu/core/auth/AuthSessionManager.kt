package com.rdbb.zhiyu.core.auth

import com.rdbb.zhiyu.core.datastore.UserManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 认证会话管理器
 * 用于管理认证会话，包括会话过期通知
 */
@Singleton
class AuthSessionManager @Inject constructor(
    private val userManager: UserManager
) {
    private val _sessionExpired = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1
    )

    val sessionExpired = _sessionExpired.asSharedFlow()

    suspend fun sessionExpired() {
        userManager.clearTokens()
        _sessionExpired.emit(Unit)
    }
}