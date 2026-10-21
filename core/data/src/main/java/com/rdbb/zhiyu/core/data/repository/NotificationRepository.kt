package com.rdbb.zhiyu.core.data.repository

import com.rdbb.zhiyu.core.model.Notification
import com.rdbb.zhiyu.core.network.api.ApiService
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val api: ApiService,
    json: Json
) : BaseRepository(json) {

    suspend fun all(): Result<List<Notification>> =
        safeCall { api.notifications(pageSize = 50) }.mapCatching { it.items }

    suspend fun unreadCount(): Result<Int> =
        safeCall { api.unreadCount() }.mapCatching { it.unreadCount }

    suspend fun markRead(id: Int): Result<Notification> = safeCall { api.readNotification(id) }

    suspend fun markAllRead() = safeCall { api.readAllNotifications() }
}
