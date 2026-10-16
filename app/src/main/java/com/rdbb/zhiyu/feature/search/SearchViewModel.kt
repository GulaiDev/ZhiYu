package com.rdbb.zhiyu.feature.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rdbb.zhiyu.core.data.repository.SearchRepository
import com.rdbb.zhiyu.core.data.repository.UserRepository
import com.rdbb.zhiyu.core.network.Community
import com.rdbb.zhiyu.core.network.Post
import com.rdbb.zhiyu.core.network.SearchAllResult
import com.rdbb.zhiyu.core.network.UserBrief
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SearchTab(val label: String) {
    ALL("综合"), USER("用户"), COMMUNITY("圈子"), POST("帖子")
}

data class SearchState(
    val query: String = "",
    val tab: SearchTab = SearchTab.ALL,
    val loading: Boolean = false,
    val searched: Boolean = false,
    val all: SearchAllResult? = null,
    val users: List<UserBrief> = emptyList(),
    val communities: List<Community> = emptyList(),
    val posts: List<Post> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    var state by mutableStateOf(SearchState())
        private set

    private var searchJob: Job? = null

    val hotKeywords = listOf("食堂", "考研", "失物招领", "社团招新", "图书馆", "实习", "期末", "合租")

    fun onQueryChange(q: String) {
        state = state.copy(query = q)
        searchJob?.cancel()
        if (q.isBlank()) {
            state = state.copy(searched = false, loading = false)
            return
        }
        searchJob = viewModelScope.launch {
            delay(250)
            runSearch()
        }
    }

    fun submit(q: String) {
        searchJob?.cancel()
        state = state.copy(query = q)
        if (q.isBlank()) return
        viewModelScope.launch { runSearch() }
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

    fun selectTab(tab: SearchTab) {
        if (state.tab == tab) return
        state = state.copy(tab = tab)
        if (state.query.isNotBlank()) runSearch()
    }

    private fun runSearch() {
        val q = state.query.trim()
        if (q.isEmpty()) return
        state = state.copy(loading = true, searched = true, error = null)
        viewModelScope.launch {
            when (state.tab) {
                SearchTab.ALL -> searchRepository.searchAll(q)
                    .onSuccess { state = state.copy(loading = false, all = it) }
                    .onFailure { state = state.copy(loading = false, error = it.message) }
                SearchTab.USER -> searchRepository.searchUsers(q)
                    .onSuccess { state = state.copy(loading = false, users = it) }
                    .onFailure { state = state.copy(loading = false, error = it.message) }
                SearchTab.COMMUNITY -> searchRepository.searchCommunities(q)
                    .onSuccess { state = state.copy(loading = false, communities = it) }
                    .onFailure { state = state.copy(loading = false, error = it.message) }
                SearchTab.POST -> searchRepository.searchPosts(q, 1)
                    .onSuccess { state = state.copy(loading = false, posts = it.items) }
                    .onFailure { state = state.copy(loading = false, error = it.message) }
            }
        }
    }
}
