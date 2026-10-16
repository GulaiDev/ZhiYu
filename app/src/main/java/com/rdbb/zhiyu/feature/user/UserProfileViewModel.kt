package com.rdbb.zhiyu.feature.user

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rdbb.zhiyu.core.data.repository.UserRepository
import com.rdbb.zhiyu.core.network.Post
import com.rdbb.zhiyu.core.network.UserDetail
import com.rdbb.zhiyu.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserProfileState(
    val loading: Boolean = true,
    val error: String? = null,
    val user: UserDetail? = null,
    val posts: List<Post> = emptyList()
)

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userRepository: UserRepository
) : ViewModel() {

    private val userId: Int = checkNotNull(savedStateHandle[Routes.Args.USER_ID])

    var state by mutableStateOf(UserProfileState())
        private set

    init {
        load()
    }

    fun load() {
        state = state.copy(loading = true, error = null)
        viewModelScope.launch {
            userRepository.user(userId)
                .onSuccess { detail ->
                    state = state.copy(loading = false, user = detail)
                    userRepository.userPosts(userId, 1, 50)
                        .onSuccess { state = state.copy(posts = it.items) }
                }
                .onFailure { state = state.copy(loading = false, error = it.message) }
        }
    }

    fun toggleFollow() {
        val user = state.user ?: return
        val following = !user.isFollowing
        state = state.copy(user = user.copy(isFollowing = following, followerCount = user.followerCount + if (following) 1 else -1))
        viewModelScope.launch {
            if (following) userRepository.follow(user.id) else userRepository.unfollow(user.id)
        }
    }
}
