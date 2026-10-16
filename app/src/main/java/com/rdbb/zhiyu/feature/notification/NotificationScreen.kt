package com.rdbb.zhiyu.feature.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rdbb.zhiyu.core.network.Notification
import com.rdbb.zhiyu.core.ui.components.Avatar
import com.rdbb.zhiyu.core.ui.components.EmptyBox
import com.rdbb.zhiyu.core.ui.components.ErrorBox
import com.rdbb.zhiyu.core.ui.components.LoadingBox
import com.rdbb.zhiyu.core.ui.components.SystemAvatar
import com.rdbb.zhiyu.core.ui.theme.Amber
import com.rdbb.zhiyu.core.ui.theme.Brand
import com.rdbb.zhiyu.core.ui.theme.CardWhite
import com.rdbb.zhiyu.core.ui.theme.Ink
import com.rdbb.zhiyu.core.ui.theme.Ink2
import com.rdbb.zhiyu.core.ui.theme.Ink3
import com.rdbb.zhiyu.core.ui.theme.Line
import com.rdbb.zhiyu.core.ui.theme.LinkBlue
import com.rdbb.zhiyu.core.ui.theme.Paper
import com.rdbb.zhiyu.core.ui.theme.SuccessGreen
import com.rdbb.zhiyu.core.util.timeAgo

@Composable
fun NotificationScreen(
    onOpenPost: (Int) -> Unit,
    onOpenUser: (Int) -> Unit,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val state = viewModel.state
    var menuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(start = 20.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("消息", fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
            Spacer(Modifier.weight(1f))
            Box {
                Icon(
                    Icons.Outlined.MoreVert,
                    contentDescription = "更多",
                    tint = Ink,
                    modifier = Modifier
                        .size(34.dp)
                        .clickable { menuExpanded = true }
                )
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("全部已读") },
                        onClick = {
                            menuExpanded = false
                            viewModel.markAllRead()
                        }
                    )
                }
            }
        }

        when {
            state.loading && state.notifications.isEmpty() -> LoadingBox()
            state.error != null && state.notifications.isEmpty() ->
                ErrorBox(state.error!!, onRetry = { viewModel.refresh() })
            state.notifications.isEmpty() -> EmptyBox("还没有消息通知")
            else -> {
                // 快捷分类
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 6.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CardWhite)
                        .padding(vertical = 16.dp)
                ) {
                    QuickItem(
                        label = "赞和收藏",
                        colors = listOf(Color(0xFFFF8A65), Color(0xFFF4502C)),
                        badge = state.count(NotificationCategory.LIKE),
                        icon = Icons.Outlined.Favorite,
                        selected = state.category == NotificationCategory.LIKE
                    ) { viewModel.selectCategory(NotificationCategory.LIKE) }
                    QuickItem(
                        label = "评论",
                        colors = listOf(Color(0xFF7C9CFF), Color(0xFF4A63E8)),
                        badge = state.count(NotificationCategory.COMMENT),
                        icon = Icons.Outlined.ChatBubbleOutline,
                        selected = state.category == NotificationCategory.COMMENT
                    ) { viewModel.selectCategory(NotificationCategory.COMMENT) }
                    QuickItem(
                        label = "新增关注",
                        colors = listOf(Color(0xFF4ECCA3), Color(0xFF1E8F6E)),
                        badge = state.count(NotificationCategory.FOLLOW),
                        icon = Icons.Outlined.Person,
                        selected = state.category == NotificationCategory.FOLLOW
                    ) { viewModel.selectCategory(NotificationCategory.FOLLOW) }
                    QuickItem(
                        label = "系统通知",
                        colors = listOf(Color(0xFFFFB347), Amber),
                        badge = 0,
                        icon = Icons.Outlined.Notifications,
                        selected = state.category == NotificationCategory.SYSTEM
                    ) { viewModel.selectCategory(NotificationCategory.SYSTEM) }
                }

                if (state.category != NotificationCategory.ALL) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "‹ 返回全部",
                            fontSize = 12.sp,
                            color = Brand,
                            modifier = Modifier.clickable {
                                viewModel.selectCategory(NotificationCategory.ALL)
                            }
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp, end = 16.dp, top = 4.dp, bottom = 12.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    items(state.visible.size) { index ->
                        val n = state.visible[index]
                        NotificationRow(
                            notification = n,
                            onClick = {
                                viewModel.markRead(n)
                                when {
                                    n.postId != null -> onOpenPost(n.postId)
                                    n.actor != null -> onOpenUser(n.actor.id)
                                }
                            },
                            onFollowBack = { viewModel.followBack(n) },
                            onOpenUser = onOpenUser
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.QuickItem(
    label: String,
    colors: List<Color>,
    badge: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(colors))
                    .then(
                        if (selected) Modifier.background(
                            Color(0x3320232B)
                        ) else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(21.dp))
            }
            if (badge > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Brand)
                        .padding(horizontal = 4.dp)
                        .height(17.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (badge > 99) "99+" else badge.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(Modifier.height(7.dp))
        Text(label, fontSize = 12.sp, color = Ink2)
    }
}

@Composable
private fun NotificationRow(
    notification: Notification,
    onClick: () -> Unit,
    onFollowBack: () -> Unit,
    onOpenUser: (Int) -> Unit
) {
    val unreadBg = if (notification.isRead) CardWhite else Color(0xFFFFF8F6)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(unreadBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!notification.isRead) {
            Box(
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Brand)
            )
            Spacer(Modifier.width(8.dp))
        } else {
            Spacer(Modifier.width(0.dp))
        }

        if (notification.actor != null) {
            Avatar(notification.actor, size = 42.dp, onClick = { onOpenUser(notification.actor.id) })
        } else {
            SystemAvatar(size = 42.dp)
        }

        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = buildMessage(notification),
                fontSize = 13.5.sp,
                lineHeight = 21.sp,
                color = Ink
            )
            Spacer(Modifier.height(3.dp))
            Text(timeAgo(notification.createdAt), fontSize = 11.sp, color = Ink3)
        }

        if (notification.type == "follow" && notification.actor != null) {
            Spacer(Modifier.width(8.dp))
            val following = notification.actor.isFollowing
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(13.dp))
                    .background(CardWhite)
                    .clickable(enabled = !following) { if (!following) onFollowBack() }
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    if (following) "已关注" else "回关",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (following) Ink3 else Brand
                )
            }
        }
    }
}

@Composable
private fun buildMessage(n: Notification) = buildAnnotatedString {
    val actorName = n.actor?.nickname ?: "知遇小助手"
    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Ink)) { append(actorName) }
    withStyle(SpanStyle(color = Ink2)) {
        append(" " + when (n.type) {
            "like_post" -> "赞了你的帖子"
            "like_comment" -> "赞了你的评论"
            "comment" -> "评论了你"
            "comment_reply" -> "回复了你"
            "follow" -> "关注了你"
            else -> ""
        })
    }
    if (n.type == "system" && !n.content.isNullOrBlank()) {
        withStyle(SpanStyle(color = Ink2)) { append(" ") ; append(n.content) }
    }
}
