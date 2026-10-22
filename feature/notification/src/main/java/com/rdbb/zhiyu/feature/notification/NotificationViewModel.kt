package com.rdbb.zhiyu.feature.notification

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rdbb.zhiyu.core.data.repository.NotificationRepository
import com.rdbb.zhiyu.core.data.repository.UserRepository
import com.rdbb.zhiyu.core.model.Notification
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class NotificationCategory(val label: String) {
    ALL("全部"),
    LIKE("赞和收藏"),
    COMMENT("评论"),
    FOLLOW("新增关注"),
    SYSTEM("系统通知")
}

data class NotificationState(
    val loading: Boolean = false,
    val error: String? = null,
    val category: NotificationCategory = NotificationCategory.ALL,
    val notifications: List<Notification> = emptyList()
) {
    private fun List<Notification>.match(category: NotificationCategory) = when (category) {
        NotificationCategory.ALL -> this
        NotificationCategory.LIKE -> filter { it.type == "like_post" || it.type == "like_comment" }
        NotificationCategory.COMMENT -> filter { it.type == "comment" || it.type == "comment_reply" }
        NotificationCategory.FOLLOW -> filter { it.type == "follow" }
        NotificationCategory.SYSTEM -> filter { it.type == "system" }
    }

    val visible: List<Notification> get() = notifications.match(category)

    fun count(category: NotificationCategory): Int =
        notifications.count { !it.isRead }.let { unread ->
            if (category == NotificationCategory.ALL) unread
            else notifications.match(category).count { !it.isRead }
        }
}

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    var state by mutableStateOf(NotificationState())
        private set

    init { refresh() }

    fun refresh() {
        state = state.copy(loading = true, error = null)
        viewModelScope.launch {
            notificationRepository.all()
                .onSuccess { state = state.copy(loading = false, notifications = it) }
                .onFailure { state = state.copy(loading = false, error = it.message) }
        }
    }

    fun selectCategory(category: NotificationCategory) {
        state = state.copy(category = category)
    }

    /** 点击通知：标记已读，返回跳转目标。 */
    fun markRead(notification: Notification) {
        if (notification.isRead) return
        update(notification.id) { it.copy(isRead = true) }
        viewModelScope.launch { notificationRepository.markRead(notification.id) }
    }

    fun markAllRead() {
        state = state.copy(notifications = state.notifications.map { it.copy(isRead = true) })
        viewModelScope.launch { notificationRepository.markAllRead() }
    }

    fun followBack(notification: Notification) {
        val actorId = notification.actor?.id ?: return
        viewModelScope.launch {
            userRepository.follow(actorId).onSuccess {
                update(notification.id) { n ->
                    n.copy(actor = n.actor?.copy(isFollowing = true))
                }
            }
        }
    }

    private fun update(id: Int, transform: (Notification) -> Notification) {
        state = state.copy(
            notifications = state.notifications.map { if (it.id == id) transform(it) else it }
        )
    }
}
