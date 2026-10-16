package com.rdbb.zhiyu.core.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rdbb.zhiyu.core.network.UserBrief
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "zhiyu_session")

/**
 * 登录态：DataStore 持久化 + 内存缓存（拦截器同步读取）。
 */
@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile var accessToken: String? = null
        private set

    @Volatile var refreshToken: String? = null
        private set

    @Volatile var user: UserBrief? = null
        private set

    private val _authExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val authExpired: SharedFlow<Unit> = _authExpired

    val isLoggedIn: Boolean get() = accessToken != null

    /** 启动页调用：从本地恢复登录态。 */
    suspend fun restore() {
        val prefs = context.dataStore.data.first()
        accessToken = prefs[KEY_ACCESS]
        refreshToken = prefs[KEY_REFRESH]
        user = prefs[KEY_USER]?.let {
            runCatching { json.decodeFromString<UserBrief>(it) }.getOrNull()
        }
    }

    suspend fun saveLogin(access: String, refresh: String, user: UserBrief) {
        accessToken = access
        refreshToken = refresh
        this.user = user
        persist()
    }

    suspend fun updateUser(user: UserBrief) {
        this.user = user
        persist()
    }

    /** Authenticator 刷新成功后同步更新内存，并异步落盘。 */
    fun applyRefreshedAccessToken(token: String) {
        accessToken = token
        scope.launch { context.dataStore.edit { it[KEY_ACCESS] = token } }
    }

    suspend fun logout() {
        clearTokens()
        context.dataStore.edit { it.clear() }
        _authExpired.tryEmit(Unit)
    }

    /** refresh 也失败：清登录态并通知全局跳转登录页。 */
    fun forceExpired() {
        clearTokens()
        scope.launch { context.dataStore.edit { it.clear() } }
        _authExpired.tryEmit(Unit)
    }

    private fun clearTokens() {
        accessToken = null
        refreshToken = null
        user = null
    }

    private suspend fun persist() {
        val a = accessToken
        val r = refreshToken
        val u = user
        context.dataStore.edit { prefs ->
            if (a != null) prefs[KEY_ACCESS] = a else prefs.remove(KEY_ACCESS)
            if (r != null) prefs[KEY_REFRESH] = r else prefs.remove(KEY_REFRESH)
            if (u != null) prefs[KEY_USER] = json.encodeToString(UserBrief.serializer(), u)
            else prefs.remove(KEY_USER)
        }
    }

    companion object {
        private val KEY_ACCESS = stringPreferencesKey("access_token")
        private val KEY_REFRESH = stringPreferencesKey("refresh_token")
        private val KEY_USER = stringPreferencesKey("user")
    }
}
