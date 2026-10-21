package com.rdbb.zhiyu.core.network.api

import com.rdbb.zhiyu.core.model.ApiResponse
import com.rdbb.zhiyu.core.model.CommentCreateRequest
import com.rdbb.zhiyu.core.model.CommentLikeResult
import com.rdbb.zhiyu.core.model.Community
import com.rdbb.zhiyu.core.model.CommunityCreateRequest
import com.rdbb.zhiyu.core.model.FavoriteResult
import com.rdbb.zhiyu.core.model.FollowResult
import com.rdbb.zhiyu.core.model.LikeResult
import com.rdbb.zhiyu.core.model.MessageResult
import com.rdbb.zhiyu.core.model.PageData
import com.rdbb.zhiyu.core.model.PasswordUpdateRequest
import com.rdbb.zhiyu.core.model.Post
import com.rdbb.zhiyu.core.model.PostCreateRequest
import com.rdbb.zhiyu.core.model.SearchAllResult
import com.rdbb.zhiyu.core.model.UnreadCount
import com.rdbb.zhiyu.core.model.UploadResult
import com.rdbb.zhiyu.core.model.UserBrief
import com.rdbb.zhiyu.core.model.UserDetail
import com.rdbb.zhiyu.core.model.UserUpdateRequest
import com.rdbb.zhiyu.core.model.Notification
import com.rdbb.zhiyu.core.model.Comment
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("api/v1/auth/me")
    suspend fun me(): ApiResponse<UserDetail>

    // ---------- 用户 ----------
    @PUT("api/v1/users/me")
    suspend fun updateMe(@Body body: UserUpdateRequest): ApiResponse<UserBrief>

    @PUT("api/v1/users/me/password")
    suspend fun updatePassword(@Body body: PasswordUpdateRequest): ApiResponse<MessageResult>

    @GET("api/v1/users/me/posts")
    suspend fun myPosts(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 10
    ): ApiResponse<PageData<Post>>

    @GET("api/v1/users/me/favorites")
    suspend fun myFavorites(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 10
    ): ApiResponse<PageData<Post>>

    @GET("api/v1/users/me/liked")
    suspend fun myLiked(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 10
    ): ApiResponse<PageData<Post>>

    @GET("api/v1/users/{id}")
    suspend fun user(@Path("id") id: Int): ApiResponse<UserDetail>

    @GET("api/v1/users/{id}/posts")
    suspend fun userPosts(
        @Path("id") id: Int,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 10
    ): ApiResponse<PageData<Post>>

    @POST("api/v1/users/{id}/follow")
    suspend fun follow(@Path("id") id: Int): ApiResponse<FollowResult>

    @DELETE("api/v1/users/{id}/follow")
    suspend fun unfollow(@Path("id") id: Int): ApiResponse<FollowResult>

    @GET("api/v1/users/{id}/followers")
    suspend fun followers(
        @Path("id") id: Int,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 50
    ): ApiResponse<PageData<UserBrief>>

    @GET("api/v1/users/{id}/following")
    suspend fun following(
        @Path("id") id: Int,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 50
    ): ApiResponse<PageData<UserBrief>>

    // ---------- 圈子 ----------
    @GET("api/v1/communities")
    suspend fun communities(@Query("q") q: String? = null): ApiResponse<List<Community>>

    @POST("api/v1/communities")
    suspend fun createCommunity(@Body body: CommunityCreateRequest): ApiResponse<Community>

    @GET("api/v1/communities/{id}")
    suspend fun community(@Path("id") id: Int): ApiResponse<Community>

    @POST("api/v1/communities/{id}/join")
    suspend fun joinCommunity(@Path("id") id: Int): ApiResponse<Community>

    @DELETE("api/v1/communities/{id}/join")
    suspend fun leaveCommunity(@Path("id") id: Int): ApiResponse<Community>

    @GET("api/v1/communities/{id}/posts")
    suspend fun communityPosts(
        @Path("id") id: Int,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 10
    ): ApiResponse<PageData<Post>>

    // ---------- 帖子 ----------
    @GET("api/v1/posts")
    suspend fun posts(
        @Query("sort") sort: String? = null,
        @Query("community_id") communityId: Int? = null,
        @Query("author_id") authorId: Int? = null,
        @Query("keyword") keyword: String? = null,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 10
    ): ApiResponse<PageData<Post>>

    @POST("api/v1/posts")
    suspend fun createPost(@Body body: PostCreateRequest): ApiResponse<Post>

    @PUT("api/v1/posts/{id}")
    suspend fun updatePost(
        @Path("id") id: Int,
        @Body body: PostCreateRequest
    ): ApiResponse<Post>

    @GET("api/v1/posts/{id}")
    suspend fun post(@Path("id") id: Int): ApiResponse<Post>

    @DELETE("api/v1/posts/{id}")
    suspend fun deletePost(@Path("id") id: Int): ApiResponse<MessageResult>

    @POST("api/v1/posts/{id}/like")
    suspend fun likePost(@Path("id") id: Int): ApiResponse<LikeResult>

    @DELETE("api/v1/posts/{id}/like")
    suspend fun unlikePost(@Path("id") id: Int): ApiResponse<LikeResult>

    @POST("api/v1/posts/{id}/favorite")
    suspend fun favoritePost(@Path("id") id: Int): ApiResponse<FavoriteResult>

    @DELETE("api/v1/posts/{id}/favorite")
    suspend fun unfavoritePost(@Path("id") id: Int): ApiResponse<FavoriteResult>

    // ---------- 评论 ----------
    @GET("api/v1/posts/{id}/comments")
    suspend fun comments(
        @Path("id") postId: Int,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 10
    ): ApiResponse<PageData<Comment>>

    @POST("api/v1/posts/{id}/comments")
    suspend fun createComment(
        @Path("id") postId: Int,
        @Body body: CommentCreateRequest
    ): ApiResponse<Comment>

    @DELETE("api/v1/comments/{id}")
    suspend fun deleteComment(@Path("id") id: Int): ApiResponse<MessageResult>

    @POST("api/v1/comments/{id}/like")
    suspend fun likeComment(@Path("id") id: Int): ApiResponse<CommentLikeResult>

    @DELETE("api/v1/comments/{id}/like")
    suspend fun unlikeComment(@Path("id") id: Int): ApiResponse<CommentLikeResult>

    // ---------- 通知 ----------
    @GET("api/v1/notifications")
    suspend fun notifications(
        @Query("type") type: String? = null,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): ApiResponse<PageData<Notification>>

    @GET("api/v1/notifications/unread-count")
    suspend fun unreadCount(): ApiResponse<UnreadCount>

    @PUT("api/v1/notifications/{id}/read")
    suspend fun readNotification(@Path("id") id: Int): ApiResponse<Notification>

    @PUT("api/v1/notifications/read-all")
    suspend fun readAllNotifications(): ApiResponse<MessageResult>

    // ---------- 搜索 ----------
    @GET("api/v1/search")
    suspend fun searchAll(
        @Query("q") q: String,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 10
    ): ApiResponse<SearchAllResult>

    @GET("api/v1/search")
    suspend fun searchPosts(
        @Query("q") q: String,
        @Query("type") type: String = "post",
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 10
    ): ApiResponse<PageData<Post>>

    @GET("api/v1/search")
    suspend fun searchUsers(
        @Query("q") q: String,
        @Query("type") type: String = "user",
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): ApiResponse<PageData<UserBrief>>

    @GET("api/v1/search")
    suspend fun searchCommunities(
        @Query("q") q: String,
        @Query("type") type: String = "community",
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): ApiResponse<PageData<Community>>

    // ---------- 上传 ----------
    @Multipart
    @POST("api/v1/upload/image")
    suspend fun uploadImage(@Part file: MultipartBody.Part): ApiResponse<UploadResult>
}
