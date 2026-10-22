package com.rdbb.zhiyu.feature.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rdbb.zhiyu.core.data.repository.CommunityRepository
import com.rdbb.zhiyu.core.data.repository.NotificationRepository
import com.rdbb.zhiyu.core.data.repository.PostRepository
import com.rdbb.zhiyu.core.data.repository.UserRepository
import com.rdbb.zhiyu.core.model.Community
import com.rdbb.zhiyu.core.model.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class FeedTab(val apiSort: String?, val label: String) {
    LATEST("latest", "推荐"),
    HOT("hot", "热门"),
    FOLLOWING(null, "关注")
}

data class HomeState(
    val tab: FeedTab = FeedTab.LATEST,
    val posts: List<Post> = emptyList(),
    val communities: List<Community> = emptyList(),
    val loading: Boolean = false,
    val loadingMore: Boolean = false,
    val error: String? = null,
    val page: Int = 1,
    val hasMore: Boolean = true,
    val unreadCount: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val communityRepository: CommunityRepository,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    var state by mutableStateOf(HomeState())
        private set

    init {
        loadCommunities()
        refresh()
        refreshUnread()
    }

    fun refreshUnread() {
        viewModelScope.launch {
            notificationRepository.unreadCount().onSuccess { state = state.copy(unreadCount = it) }
        }
    }

    private fun loadCommunities() {
        viewModelScope.launch {
            communityRepository.list().onSuccess { state = state.copy(communities = it.take(6)) }
        }
    }

    fun switchTab(tab: FeedTab) {
        if (state.tab == tab) return
        state = state.copy(tab = tab, posts = emptyList(), page = 1, hasMore = true, error = null)
        refresh()
    }

    fun refresh() {
        state = state.copy(loading = true, error = null, page = 1, hasMore = true)
        viewModelScope.launch {
            val result = postRepository.feed(sort = state.tab.apiSort, page = 1)
            result.onSuccess { page ->
                val list = if (state.tab == FeedTab.FOLLOWING)
                    page.items.filter { it.author.isFollowing } else page.items
                state = state.copy(
                    loading = false,
                    posts = list,
                    hasMore = page.items.size >= 10
                )
            }.onFailure {
                state = state.copy(loading = false, error = it.message ?: "加载失败")
            }
        }
    }

    fun loadMore() {
        if (state.loading || state.loadingMore || !state.hasMore) return
        state = state.copy(loadingMore = true)
        val nextPage = state.page + 1
        viewModelScope.launch {
            postRepository.feed(sort = state.tab.apiSort, page = nextPage)
                .onSuccess { page ->
                    val list = if (state.tab == FeedTab.FOLLOWING)
                        page.items.filter { it.author.isFollowing } else page.items
                    state = state.copy(
                        loadingMore = false,
                        posts = state.posts + list,
                        page = nextPage,
                        hasMore = page.items.size >= 10
                    )
                }
                .onFailure { state = state.copy(loadingMore = false) }
        }
    }

    fun toggleLike(post: Post) {
        val newLiked = !post.isLiked
        updatePost(post.id) {
            it.copy(
                isLiked = newLiked,
                likeCount = it.likeCount + if (newLiked) 1 else -1
            )
        }
        viewModelScope.launch {
            postRepository.like(post.id, newLiked)
                .onSuccess { r -> updatePost(post.id) { it.copy(isLiked = r.isLiked, likeCount = r.likeCount) } }
        }
    }

    fun toggleFavorite(post: Post) {
        val newFav = !post.isFavorited
        updatePost(post.id) {
            it.copy(
                isFavorited = newFav,
                favoriteCount = it.favoriteCount + if (newFav) 1 else -1
            )
        }
        viewModelScope.launch {
            postRepository.favorite(post.id, newFav)
                .onSuccess { r ->
                    updatePost(post.id) {
                        it.copy(isFavorited = r.isFavorited, favoriteCount = r.favoriteCount)
                    }
                }
        }
    }

    fun follow(authorId: Int, postId: Int) {
        viewModelScope.launch {
            userRepository.follow(authorId).onSuccess {
                updatePost(postId) { it.copy(author = it.author.copy(isFollowing = true)) }
            }
        }
    }

    private fun updatePost(id: Int, transform: (Post) -> Post) {
        state = state.copy(posts = state.posts.map { if (it.id == id) transform(it) else it })
    }
}
