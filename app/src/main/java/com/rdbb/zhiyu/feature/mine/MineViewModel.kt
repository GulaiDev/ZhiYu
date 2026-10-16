package com.rdbb.zhiyu.feature.mine

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rdbb.zhiyu.core.data.repository.AuthRepository
import com.rdbb.zhiyu.core.data.repository.UserRepository
import com.rdbb.zhiyu.core.network.Post
import com.rdbb.zhiyu.core.network.UserDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class MineTab(val label: String) {
    POSTS("帖子"), FAVORITES("收藏"), LIKED("赞过")
}

data class MineState(
    val me: UserDetail? = null,
    val tab: MineTab = MineTab.POSTS,
    val posts: List<Post> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val loggingOut: Boolean = false
)

@HiltViewModel
class MineViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    var state by mutableStateOf(MineState())
        private set

    init {
        loadMe()
        loadPosts(MineTab.POSTS)
    }

    fun refresh() {
        loadMe()
        loadPosts(state.tab)
    }

    /** 从编辑页返回时仅刷新用户信息，帖子列表保留。 */
    fun onResume() {
        loadMe()
    }

    private fun loadMe() {
        viewModelScope.launch {
            authRepository.me().onSuccess { state = state.copy(me = it) }
        }
    }

    fun switchTab(tab: MineTab) {
        if (state.tab == tab) return
        state = state.copy(tab = tab, posts = emptyList())
        loadPosts(tab)
    }

    private fun loadPosts(tab: MineTab) {
        state = state.copy(loading = true, error = null)
        viewModelScope.launch {
            val result = when (tab) {
                MineTab.POSTS -> userRepository.myPosts(1, 50)
                MineTab.FAVORITES -> userRepository.myFavorites(1, 50)
                MineTab.LIKED -> userRepository.myLiked(1, 50)
            }
            result
                .onSuccess { state = state.copy(loading = false, posts = it.items) }
                .onFailure { state = state.copy(loading = false, error = it.message) }
        }
    }

    fun changePassword(old: String, new: String, onResult: (Boolean, String?) -> Unit) {
        if (new.length !in 6..20) {
            onResult(false, "新密码长度需 6-20 位")
            return
        }
        viewModelScope.launch {
            userRepository.changePassword(old, new)
                .onSuccess { onResult(true, "密码修改成功") }
                .onFailure { onResult(false, it.message) }
        }
    }

    fun logout(onDone: () -> Unit) {
        state = state.copy(loggingOut = true)
        viewModelScope.launch {
            authRepository.logout()
            state = state.copy(loggingOut = false)
            onDone()
        }
    }
}
