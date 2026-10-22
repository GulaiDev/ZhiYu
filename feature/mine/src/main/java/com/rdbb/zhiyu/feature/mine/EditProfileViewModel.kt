package com.rdbb.zhiyu.feature.mine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rdbb.zhiyu.core.data.repository.AuthRepository
import com.rdbb.zhiyu.core.data.repository.PostRepository
import com.rdbb.zhiyu.core.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

data class EditProfileState(
    val loading: Boolean = true,
    val nickname: String = "",
    val bio: String = "",
    val gender: Int = 0,
    val avatar: String? = null,
    val uploading: Boolean = false,
    val saving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val postRepository: PostRepository
) : ViewModel() {

    var state by mutableStateOf(EditProfileState())
        private set

    init {
        viewModelScope.launch {
            authRepository.me()
                .onSuccess {
                    state = state.copy(
                        loading = false,
                        nickname = it.nickname,
                        bio = it.bio ?: "",
                        gender = it.gender,
                        avatar = it.avatar
                    )
                }
                .onFailure { state = state.copy(loading = false, error = it.message) }
        }
    }

    fun onNickname(v: String) {
        if (v.length <= 12) state = state.copy(nickname = v)
    }

    fun onBio(v: String) {
        if (v.length <= 100) state = state.copy(bio = v)
    }

    fun onGender(g: Int) {
        state = state.copy(gender = g)
    }

    fun uploadAvatar(part: MultipartBody.Part) {
        state = state.copy(uploading = true)
        viewModelScope.launch {
            postRepository.uploadImage(part)
                .onSuccess { state = state.copy(uploading = false, avatar = it.url) }
                .onFailure { state = state.copy(uploading = false, error = it.message) }
        }
    }

    fun save() {
        if (state.nickname.isBlank()) {
            state = state.copy(error = "昵称不能为空")
            return
        }
        state = state.copy(saving = true, error = null)
        viewModelScope.launch {
            userRepository.updateMe(
                nickname = state.nickname.trim(),
                bio = state.bio.ifBlank { null },
                gender = state.gender,
                avatar = state.avatar
            )
                .onSuccess { state = state.copy(saving = false, saved = true) }
                .onFailure { state = state.copy(saving = false, error = it.message) }
        }
    }

    fun consumeError() {
        state = state.copy(error = null)
    }
}
