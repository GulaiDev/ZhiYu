package com.rdbb.zhiyu.core.data

import com.rdbb.zhiyu.core.datastore.UserManager
import com.rdbb.zhiyu.core.model.UserBrief
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 登录态门面：包装 [UserManager]，额外提供内存态与全局登录失效事件。
 * 不新建 DataStore，持久化仍是唯一来源，TokenAuthenticator 清理 token 后这里也能感知。
 */
@Singleton
class SessionManager @Inject constructor(
    private val userManager: UserManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    var user: UserBrief? = null
        private set

    @Volatile
    var isLoggedIn: Boolean = false
        private set

    private val _authExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /** 401 或主动登出时发射，由导航层统一跳转登录页。 */
    val authExpired: SharedFlow<Unit> = _authExpired

    init {
        scope.launch {
            userManager.tokenFlow.collect { token ->
                isLoggedIn = !token.isNullOrBlank()
                if (!isLoggedIn) user = null
            }
        }
        scope.launch {
            userManager.userFlow.collect { user = it }
        }
    }

    /** 启动页调用：从本地恢复登录态。 */
    suspend fun restore() {
        isLoggedIn = !userManager.getToken().isNullOrBlank()
        user = userManager.getUser()
    }

    suspend fun saveLogin(access: String, refresh: String, user: UserBrief) {
        userManager.saveLogin(access, refresh, user)
        isLoggedIn = true
        this.user = user
    }

    suspend fun updateUser(user: UserBrief) {
        userManager.updateUser(user)
        this.user = user
    }

    /** Authenticator 刷新成功后同步落盘。 */
    fun applyRefreshedAccessToken(token: String) {
        scope.launch { userManager.saveToken(token) }
    }

    suspend fun logout() {
        userManager.logout()
        clearLocal()
        _authExpired.tryEmit(Unit)
    }

    /** refresh 也失败：清登录态并通知全局跳转登录页。 */
    fun forceExpired() {
        scope.launch { userManager.logout() }
        clearLocal()
        _authExpired.tryEmit(Unit)
    }

    private fun clearLocal() {
        isLoggedIn = false
        user = null
    }
}
