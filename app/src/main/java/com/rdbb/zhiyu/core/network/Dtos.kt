package com.rdbb.zhiyu.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ============================== 用户 ==============================

@Serializable
data class UserBrief(
    val id: Int,
    val username: String,
    val nickname: String,
    val avatar: String? = null,
    val bio: String? = null,
    val gender: Int = 0,
    @SerialName("is_following") val isFollowing: Boolean = false
)

@Serializable
data class UserDetail(
    val id: Int,
    val username: String,
    val nickname: String,
    val avatar: String? = null,
    val bio: String? = null,
    val gender: Int = 0,
    @SerialName("is_following") val isFollowing: Boolean = false,
    @SerialName("post_count") val postCount: Int = 0,
    @SerialName("follower_count") val followerCount: Int = 0,
    @SerialName("following_count") val followingCount: Int = 0,
    @SerialName("created_at") val createdAt: String? = null,
    val email: String? = null
)

@Serializable
data class FollowResult(
    @SerialName("is_following") val isFollowing: Boolean,
    @SerialName("follower_count") val followerCount: Int
)

// ============================== 认证 ==============================

@Serializable
data class LoginRequest(val username: String, val password: String)

@Serializable
data class RegisterRequest(
    val username: String,
    val password: String,
    val nickname: String? = null
)

@Serializable
data class RefreshRequest(@SerialName("refresh_token") val refreshToken: String)

@Serializable
data class TokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("token_type") val tokenType: String = "bearer",
    val user: UserBrief
)

@Serializable
data class RefreshResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String = "bearer"
)

@Serializable
data class UserUpdateRequest(
    val nickname: String? = null,
    val avatar: String? = null,
    val bio: String? = null,
    val gender: Int? = null
)

@Serializable
data class PasswordUpdateRequest(
    @SerialName("old_password") val oldPassword: String,
    @SerialName("new_password") val newPassword: String
)

// ============================== 圈子 ==============================

@Serializable
data class Community(
    val id: Int,
    val name: String,
    val description: String? = null,
    val icon: String? = null,
    val cover: String? = null,
    @SerialName("post_count") val postCount: Int = 0,
    @SerialName("member_count") val memberCount: Int = 0,
    @SerialName("is_joined") val isJoined: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class CommunityCreateRequest(
    val name: String,
    val description: String? = null,
    val icon: String? = null,
    val cover: String? = null
)

// ============================== 帖子 ==============================

@Serializable
data class Post(
    val id: Int,
    val title: String,
    val content: String,
    val images: List<String> = emptyList(),
    val author: UserBrief,
    val community: Community? = null,
    @SerialName("view_count") val viewCount: Int = 0,
    @SerialName("like_count") val likeCount: Int = 0,
    @SerialName("comment_count") val commentCount: Int = 0,
    @SerialName("favorite_count") val favoriteCount: Int = 0,
    @SerialName("is_liked") val isLiked: Boolean = false,
    @SerialName("is_favorited") val isFavorited: Boolean = false,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class PostCreateRequest(
    val title: String,
    val content: String,
    val images: List<String> = emptyList(),
    @SerialName("community_id") val communityId: Int? = null
)

@Serializable
data class LikeResult(
    @SerialName("is_liked") val isLiked: Boolean,
    @SerialName("like_count") val likeCount: Int
)

@Serializable
data class FavoriteResult(
    @SerialName("is_favorited") val isFavorited: Boolean,
    @SerialName("favorite_count") val favoriteCount: Int
)

// ============================== 评论 ==============================

@Serializable
data class Comment(
    val id: Int,
    @SerialName("post_id") val postId: Int,
    @SerialName("parent_id") val parentId: Int? = null,
    val content: String,
    val author: UserBrief,
    @SerialName("reply_to_user") val replyToUser: UserBrief? = null,
    @SerialName("like_count") val likeCount: Int = 0,
    @SerialName("is_liked") val isLiked: Boolean = false,
    val replies: List<Comment> = emptyList(),
    @SerialName("reply_count") val replyCount: Int = 0,
    @SerialName("created_at") val createdAt: String = ""
)

@Serializable
data class CommentCreateRequest(
    val content: String,
    @SerialName("parent_id") val parentId: Int? = null,
    @SerialName("reply_to_user_id") val replyToUserId: Int? = null
)

@Serializable
data class CommentLikeResult(
    @SerialName("is_liked") val isLiked: Boolean,
    @SerialName("like_count") val likeCount: Int
)

// ============================== 通知 ==============================

@Serializable
data class Notification(
    val id: Int,
    val type: String,
    val content: String? = null,
    @SerialName("is_read") val isRead: Boolean = false,
    val actor: UserBrief? = null,
    @SerialName("post_id") val postId: Int? = null,
    @SerialName("comment_id") val commentId: Int? = null,
    @SerialName("created_at") val createdAt: String = ""
)

@Serializable
data class UnreadCount(@SerialName("unread_count") val unreadCount: Int)

// ============================== 搜索 / 上传 ==============================

@Serializable
data class SearchAllResult(
    val users: List<UserBrief> = emptyList(),
    val posts: List<Post> = emptyList(),
    val communities: List<Community> = emptyList()
)

@Serializable
data class UploadResult(
    val url: String,
    @SerialName("full_url") val fullUrl: String
)

@Serializable
data class MessageResult(val message: String? = null)
