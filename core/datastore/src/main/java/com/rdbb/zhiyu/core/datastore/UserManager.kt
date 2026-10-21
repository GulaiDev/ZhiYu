package com.rdbb.zhiyu.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
// 顶层扩展属性：整个应用共享同一个 DataStore 实例，切勿在多处重复创建
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val tokenKey = stringPreferencesKey(TOKEN_KEY)
    private val refreshTokenKey = stringPreferencesKey(REFRESH_TOKEN_KEY)

    /** 观察 access_token 变化，未登录或已退出时发射 null */
    val tokenFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[tokenKey]
    }

    /** 观察 refresh_token 变化，未登录或已退出时发射 null */
    val refreshTokenFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[refreshTokenKey]
    }

    /** 一次性读取 access_token，例如在 OkHttp 拦截器中同步（挂起）获取 */
    suspend fun getToken(): String? = tokenFlow.first()

    /** 一次性读取 refresh_token，例如在 401 自动刷新时获取 */
    suspend fun getRefreshToken(): String? = refreshTokenFlow.first()

    /** 保存 access_token */
    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[tokenKey] = token
        }
    }

    /** 保存 refresh_token */
    suspend fun saveRefreshToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[refreshTokenKey] = token
        }
    }

    /** 登录/注册成功后一次性保存双 token */
    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        context.dataStore.edit { preferences ->
            preferences[tokenKey] = accessToken
            preferences[refreshTokenKey] = refreshToken
        }
    }

    /** 清除全部登录态（退出登录，或 refresh_token 也失效时调用） */
    suspend fun clearTokens() {
        context.dataStore.edit { preferences ->
            preferences.remove(tokenKey)
            preferences.remove(refreshTokenKey)
        }
    }

    companion object {
        const val TOKEN_KEY = "token"
        const val REFRESH_TOKEN_KEY = "refresh_token"
    }
}
