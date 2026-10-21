package com.rdbb.zhiyu.core.data.repository

import com.rdbb.zhiyu.core.model.Community
import com.rdbb.zhiyu.core.model.PageData
import com.rdbb.zhiyu.core.model.Post
import com.rdbb.zhiyu.core.network.api.ApiService
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommunityRepository @Inject constructor(
    private val api: ApiService,
    json: Json
) : BaseRepository(json) {

    suspend fun list(q: String? = null): Result<List<Community>> = safeCall {
        api.communities(q?.trim()?.ifBlank { null })
    }

    suspend fun detail(id: Int): Result<Community> = safeCall { api.community(id) }

    suspend fun join(id: Int, join: Boolean): Result<Community> =
        safeCall { if (join) api.joinCommunity(id) else api.leaveCommunity(id) }

    suspend fun posts(id: Int, page: Int, size: Int = 10): Result<PageData<Post>> =
        safeCall { api.communityPosts(id, page, size) }
}
