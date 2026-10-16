package com.rdbb.zhiyu.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rdbb.zhiyu.core.data.SessionManager
import com.rdbb.zhiyu.core.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 全局导航宿主状态：未读消息数、登录态过期事件。 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val session: SessionManager
) : ViewModel() {

    var unreadCount by mutableIntStateOf(0)
        private set

    val authExpired: SharedFlow<Unit> = session.authExpired

    fun refreshUnread() {
        if (!session.isLoggedIn) {
            unreadCount = 0
            return
        }
        viewModelScope.launch {
            notificationRepository.unreadCount()
                .onSuccess { unreadCount = it }
        }
    }

    fun clearUnread() {
        unreadCount = 0
    }
}
