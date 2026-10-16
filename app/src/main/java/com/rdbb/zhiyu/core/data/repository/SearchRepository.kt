package com.rdbb.zhiyu.core.data.repository

import com.rdbb.zhiyu.core.data.SessionManager
import com.rdbb.zhiyu.core.network.ApiService
import com.rdbb.zhiyu.core.network.Community
import com.rdbb.zhiyu.core.network.PageData
import com.rdbb.zhiyu.core.network.Post
import com.rdbb.zhiyu.core.network.SearchAllResult
import com.rdbb.zhiyu.core.network.UserBrief
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepository @Inject constructor(
    private val api: ApiService,
    private val session: SessionManager,
    json: Json
) : BaseRepository(session, json) {

    suspend fun searchAll(q: String): Result<SearchAllResult> =
        safeCall { api.searchAll(q.trim()) }

    suspend fun searchPosts(q: String, page: Int): Result<PageData<Post>> =
        safeCall { api.searchPosts(q.trim(), page = page) }

    suspend fun searchUsers(q: String): Result<List<UserBrief>> =
        safeCall { api.searchUsers(q.trim()) }.mapCatching { it.items }

    suspend fun searchCommunities(q: String): Result<List<Community>> =
        safeCall { api.searchCommunities(q.trim()) }.mapCatching { it.items }
}
