package com.rdbb.zhiyu.feature.post

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rdbb.zhiyu.core.data.SessionManager
import com.rdbb.zhiyu.core.data.repository.PostRepository
import com.rdbb.zhiyu.core.data.repository.UserRepository
import com.rdbb.zhiyu.core.network.Comment
import com.rdbb.zhiyu.core.network.Post
import com.rdbb.zhiyu.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 回复目标：parent 为楼中楼所属根评论，replyTo 为被回复用户。 */
data class ReplyTarget(
    val parent: Comment,
    val replyToUserId: Int?,
    val replyToName: String
)

data class PostDetailState(
    val loading: Boolean = true,
    val error: String? = null,
    val post: Post? = null,
    val comments: List<Comment> = emptyList(),
    val commentsLoading: Boolean = false,
    val draft: String = "",
    val replyTarget: ReplyTarget? = null,
    val sending: Boolean = false
)

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val session: SessionManager
) : ViewModel() {

    private val postId: Int = checkNotNull(savedStateHandle[Routes.Args.POST_ID])
    val currentUserId: Int? get() = session.user?.id

    var state by mutableStateOf(PostDetailState())
        private set

    init {
        load()
    }

    fun load() {
        state = state.copy(loading = true, error = null)
        viewModelScope.launch {
            postRepository.detail(postId)
                .onSuccess {
                    state = state.copy(loading = false, post = it)
                    loadComments()
                }
                .onFailure { state = state.copy(loading = false, error = it.message) }
        }
    }

    fun retry() = load()

    private fun loadComments() {
        state = state.copy(commentsLoading = true)
        viewModelScope.launch {
            postRepository.comments(postId)
                .onSuccess { state = state.copy(commentsLoading = false, comments = it) }
                .onFailure { state = state.copy(commentsLoading = false) }
        }
    }

    fun onDraftChange(v: String) {
        if (v.length <= 500) state = state.copy(draft = v)
    }

    fun setReplyTarget(target: ReplyTarget?) {
        state = state.copy(replyTarget = target)
    }

    fun toggleLike() {
        val post = state.post ?: return
        val liked = !post.isLiked
        state = state.copy(
            post = post.copy(
                isLiked = liked,
                likeCount = (post.likeCount + if (liked) 1 else -1).coerceAtLeast(0)
            )
        )
        viewModelScope.launch {
            postRepository.like(post.id, liked)
                .onSuccess {
                    state = state.copy(
                        post = state.post?.copy(isLiked = it.isLiked, likeCount = it.likeCount)
                    )
                }
                .onFailure { /* 保持乐观结果即可 */ }
        }
    }

    fun toggleFavorite() {
        val post = state.post ?: return
        val favorited = !post.isFavorited
        state = state.copy(
            post = post.copy(
                isFavorited = favorited,
                favoriteCount = (post.favoriteCount + if (favorited) 1 else -1).coerceAtLeast(0)
            )
        )
        viewModelScope.launch {
            postRepository.favorite(post.id, favorited)
                .onSuccess {
                    state = state.copy(
                        post = state.post?.copy(
                            isFavorited = it.isFavorited,
                            favoriteCount = it.favoriteCount
                        )
                    )
                }
        }
    }

    fun toggleFollow() {
        val post = state.post ?: return
        val following = !(post.author.isFollowing)
        state = state.copy(post = post.copy(author = post.author.copy(isFollowing = following)))
        viewModelScope.launch {
            val result =
                if (following) userRepository.follow(post.author.id)
                else userRepository.unfollow(post.author.id)
            result.onFailure {
                state = state.copy(
                    post = state.post?.copy(
                        author = state.post!!.author.copy(isFollowing = !following)
                    )
                )
            }
        }
    }

    fun toggleCommentLike(comment: Comment) {
        val liked = !comment.isLiked
        updateComment(comment.id) {
            it.copy(
                isLiked = liked,
                likeCount = (it.likeCount + if (liked) 1 else -1).coerceAtLeast(0)
            )
        }
        viewModelScope.launch { postRepository.likeComment(comment.id, liked) }
    }

    fun sendComment(onDone: () -> Unit) {
        val content = state.draft.trim()
        if (content.isBlank() || state.sending) return
        state = state.copy(sending = true)
        val target = state.replyTarget
        viewModelScope.launch {
            postRepository.createComment(
                postId = postId,
                content = content,
                parentId = target?.parent?.id,
                replyToUserId = target?.replyToUserId
            )
                .onSuccess { created ->
                    val comments = if (target == null) {
                        listOf(created) + state.comments
                    } else {
                        state.comments.map { root ->
                            if (root.id == target.parent.id) {
                                root.copy(replies = root.replies + created)
                            } else root
                        }
                    }
                    state = state.copy(
                        sending = false,
                        comments = comments,
                        draft = "",
                        replyTarget = null,
                        post = state.post?.copy(commentCount = state.post!!.commentCount + 1)
                    )
                    onDone()
                }
                .onFailure { state = state.copy(sending = false) }
        }
    }

    private fun updateComment(id: Int, transform: (Comment) -> Comment) {
        fun walk(items: List<Comment>): List<Comment> = items.map { c ->
            when {
                c.id == id -> transform(c)
                c.replies.isNotEmpty() -> c.copy(replies = walk(c.replies))
                else -> c
            }
        }
        state = state.copy(comments = walk(state.comments))
    }
}
