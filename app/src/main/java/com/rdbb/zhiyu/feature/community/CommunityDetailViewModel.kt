package com.rdbb.zhiyu.feature.community

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rdbb.zhiyu.core.data.repository.CommunityRepository
import com.rdbb.zhiyu.core.data.repository.PostRepository
import com.rdbb.zhiyu.core.data.repository.UserRepository
import com.rdbb.zhiyu.core.network.Community
import com.rdbb.zhiyu.core.network.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CommunityDetailState(
    val community: Community? = null,
    val posts: List<Post> = emptyList(),
    val loading: Boolean = false,
    val loadingMore: Boolean = false,
    val error: String? = null,
    val page: Int = 1,
    val hasMore: Boolean = true
)

@HiltViewModel
class CommunityDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val communityRepository: CommunityRepository,
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val communityId: Int = checkNotNull(savedStateHandle["communityId"])

    var state by mutableStateOf(CommunityDetailState())
        private set

    init { load() }

    fun load() {
        state = state.copy(loading = true, error = null)
        viewModelScope.launch {
            communityRepository.detail(communityId)
                .onSuccess { state = state.copy(community = it) }
                .onFailure { state = state.copy(error = it.message) }
            communityRepository.posts(communityId, 1)
                .onSuccess { page ->
                    state = state.copy(
                        loading = false,
                        posts = page.items,
                        hasMore = page.items.size >= 10
                    )
                }
                .onFailure { state = state.copy(loading = false, error = it.message) }
        }
    }

    fun loadMore() {
        if (state.loading || state.loadingMore || !state.hasMore) return
        state = state.copy(loadingMore = true)
        val next = state.page + 1
        viewModelScope.launch {
            communityRepository.posts(communityId, next)
                .onSuccess { page ->
                    state = state.copy(
                        loadingMore = false,
                        posts = state.posts + page.items,
                        page = next,
                        hasMore = page.items.size >= 10
                    )
                }
                .onFailure { state = state.copy(loadingMore = false) }
        }
    }

    fun toggleJoin() {
        val c = state.community ?: return
        val join = !c.isJoined
        state = state.copy(
            community = c.copy(isJoined = join, memberCount = c.memberCount + if (join) 1 else -1)
        )
        viewModelScope.launch {
            communityRepository.join(communityId, join)
                .onSuccess { state = state.copy(community = it) }
        }
    }

    fun toggleLike(post: Post) {
        val liked = !post.isLiked
        updatePost(post.id) { it.copy(isLiked = liked, likeCount = it.likeCount + if (liked) 1 else -1) }
        viewModelScope.launch {
            postRepository.like(post.id, liked)
                .onSuccess { r -> updatePost(post.id) { it.copy(isLiked = r.isLiked, likeCount = r.likeCount) } }
        }
    }

    fun toggleFavorite(post: Post) {
        val fav = !post.isFavorited
        updatePost(post.id) {
            it.copy(isFavorited = fav, favoriteCount = it.favoriteCount + if (fav) 1 else -1)
        }
        viewModelScope.launch {
            postRepository.favorite(post.id, fav)
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
