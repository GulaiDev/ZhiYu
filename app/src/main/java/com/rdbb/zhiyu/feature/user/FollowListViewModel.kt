package com.rdbb.zhiyu.feature.user

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rdbb.zhiyu.core.data.repository.UserRepository
import com.rdbb.zhiyu.core.network.UserBrief
import com.rdbb.zhiyu.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FollowListState(
    val loading: Boolean = true,
    val title: String = "",
    val users: List<UserBrief> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class FollowListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userRepository: UserRepository
) : ViewModel() {

    private val userId: Int = checkNotNull(savedStateHandle[Routes.Args.USER_ID])
    private val type: String = checkNotNull(savedStateHandle[Routes.Args.FOLLOW_TYPE])

    var state by mutableStateOf(
        FollowListState(title = if (type == "followers") "粉丝" else "关注")
    )
        private set

    init {
        load()
    }

    private fun load() {
        state = state.copy(loading = true)
        viewModelScope.launch {
            val result = if (type == "followers") {
                userRepository.followers(userId)
            } else {
                userRepository.following(userId)
            }
            result
                .onSuccess { state = state.copy(loading = false, users = it) }
                .onFailure { state = state.copy(loading = false, error = it.message) }
        }
    }

    fun toggleFollow(id: Int) {
        val user = state.users.firstOrNull { it.id == id } ?: return
        val following = !user.isFollowing
        state = state.copy(users = state.users.map {
            if (it.id == id) it.copy(isFollowing = following) else it
        })
        viewModelScope.launch {
            if (following) userRepository.follow(id) else userRepository.unfollow(id)
        }
    }
}
