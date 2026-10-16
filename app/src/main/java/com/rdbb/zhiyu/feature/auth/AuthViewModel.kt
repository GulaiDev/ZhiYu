package com.rdbb.zhiyu.feature.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rdbb.zhiyu.core.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    var state by mutableStateOf(AuthUiState())
        private set

    fun login(username: String, password: String) {
        if (!validate(username, password)) return
        state = state.copy(loading = true, error = null)
        viewModelScope.launch {
            val result = authRepository.login(username, password)
            state = if (result.isSuccess) {
                state.copy(loading = false, success = true)
            } else {
                state.copy(loading = false, error = result.exceptionOrNull()?.message ?: "登录失败")
            }
        }
    }

    fun register(username: String, password: String, nickname: String) {
        if (username.length !in 3..20 || !username.matches(Regex("[a-zA-Z0-9_]+"))) {
            state = state.copy(error = "用户名为 3-20 位字母、数字或下划线")
            return
        }
        if (password.length !in 6..20) {
            state = state.copy(error = "密码长度需 6-20 位")
            return
        }
        state = state.copy(loading = true, error = null)
        viewModelScope.launch {
            val result = authRepository.register(username, password, nickname)
            state = if (result.isSuccess) {
                state.copy(loading = false, success = true)
            } else {
                state.copy(loading = false, error = result.exceptionOrNull()?.message ?: "注册失败")
            }
        }
    }

    fun consumeError() {
        state = state.copy(error = null)
    }

    private fun validate(username: String, password: String): Boolean {
        if (username.isBlank()) {
            state = state.copy(error = "请输入账号")
            return false
        }
        if (password.isBlank()) {
            state = state.copy(error = "请输入密码")
            return false
        }
        return true
    }
}
