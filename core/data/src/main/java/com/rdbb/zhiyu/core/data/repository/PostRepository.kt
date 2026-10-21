package com.rdbb.zhiyu.core.data.repository

import com.rdbb.zhiyu.core.model.Comment
import com.rdbb.zhiyu.core.model.CommentCreateRequest
import com.rdbb.zhiyu.core.model.CommentLikeResult
import com.rdbb.zhiyu.core.model.PageData
import com.rdbb.zhiyu.core.model.Post
import com.rdbb.zhiyu.core.model.PostCreateRequest
import com.rdbb.zhiyu.core.network.api.ApiService
import kotlinx.serialization.json.Json
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepository @Inject constructor(
    private val api: ApiService,
    json: Json
) : BaseRepository(json) {

    suspend fun feed(
        sort: String? = null,
        communityId: Int? = null,
        authorId: Int? = null,
        keyword: String? = null,
        page: Int = 1,
        size: Int = 10
    ): Result<PageData<Post>> =
        safeCall { api.posts(sort, communityId, authorId, keyword, page, size) }

    suspend fun detail(id: Int): Result<Post> = safeCall { api.post(id) }

    suspend fun create(
        title: String,
        content: String,
        images: List<String>,
        communityId: Int?
    ): Result<Post> = safeCall {
        api.createPost(PostCreateRequest(title.trim(), content.trim(), images, communityId))
    }

    suspend fun delete(id: Int) = safeCall { api.deletePost(id) }

    suspend fun like(id: Int, liked: Boolean) =
        safeCall { if (liked) api.likePost(id) else api.unlikePost(id) }

    suspend fun favorite(id: Int, favorited: Boolean) =
        safeCall { if (favorited) api.favoritePost(id) else api.unfavoritePost(id) }

    suspend fun comments(postId: Int): Result<List<Comment>> =
        safeCall { api.comments(postId, pageSize = 50) }.mapCatching { it.items }

    suspend fun createComment(
        postId: Int,
        content: String,
        parentId: Int? = null,
        replyToUserId: Int? = null
    ): Result<Comment> =
        safeCall {
            api.createComment(postId, CommentCreateRequest(content.trim(), parentId, replyToUserId))
        }

    suspend fun likeComment(id: Int, liked: Boolean): Result<CommentLikeResult> =
        safeCall { if (liked) api.likeComment(id) else api.unlikeComment(id) }

    suspend fun uploadImage(part: MultipartBody.Part) = safeCall { api.uploadImage(part) }
}
