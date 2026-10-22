package com.rdbb.zhiyu.core.data.repository

import com.rdbb.zhiyu.core.data.SessionManager
import com.rdbb.zhiyu.core.model.PageData
import com.rdbb.zhiyu.core.model.PasswordUpdateRequest
import com.rdbb.zhiyu.core.model.Post
import com.rdbb.zhiyu.core.model.UserBrief
import com.rdbb.zhiyu.core.model.UserDetail
import com.rdbb.zhiyu.core.model.UserUpdateRequest
import com.rdbb.zhiyu.core.network.api.ApiService
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val api: ApiService,
    private val session: SessionManager,
    json: Json
) : BaseRepository(session, json) {

    suspend fun updateMe(
        nickname: String?,
        bio: String?,
        gender: Int?,
        avatar: String?
    ): Result<UserBrief> {
        val result = safeCall { api.updateMe(UserUpdateRequest(nickname, avatar, bio, gender)) }
        result.getOrNull()?.let { session.updateUser(it) }
        return result
    }

    suspend fun changePassword(old: String, new: String) =
        safeCall { api.updatePassword(PasswordUpdateRequest(old, new)) }

    suspend fun user(id: Int): Result<UserDetail> = safeCall { api.user(id) }

    suspend fun follow(id: Int) = safeCall { api.follow(id) }
    suspend fun unfollow(id: Int) = safeCall { api.unfollow(id) }

    suspend fun followers(id: Int): Result<List<UserBrief>> =
        safeCall { api.followers(id) }.mapCatching { it.items }

    suspend fun following(id: Int): Result<List<UserBrief>> =
        safeCall { api.following(id) }.mapCatching { it.items }

    suspend fun myPosts(page: Int, size: Int = 10): Result<PageData<Post>> =
        safeCall { api.myPosts(page, size) }

    suspend fun myFavorites(page: Int, size: Int = 10): Result<PageData<Post>> =
        safeCall { api.myFavorites(page, size) }

    suspend fun myLiked(page: Int, size: Int = 10): Result<PageData<Post>> =
        safeCall { api.myLiked(page, size) }

    suspend fun userPosts(id: Int, page: Int, size: Int = 10): Result<PageData<Post>> =
        safeCall { api.userPosts(id, page, size) }
}
