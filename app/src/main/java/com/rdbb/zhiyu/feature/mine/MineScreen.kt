package com.rdbb.zhiyu.feature.mine

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.runtime.DisposableEffect
import com.rdbb.zhiyu.core.network.Post
import com.rdbb.zhiyu.core.ui.components.Avatar
import com.rdbb.zhiyu.core.ui.components.EmptyBox
import com.rdbb.zhiyu.core.ui.components.LoadingBox
import com.rdbb.zhiyu.core.ui.components.NetworkImage
import com.rdbb.zhiyu.core.ui.theme.Brand
import com.rdbb.zhiyu.core.ui.theme.CardWhite
import com.rdbb.zhiyu.core.ui.theme.Ink
import com.rdbb.zhiyu.core.ui.theme.Ink2
import com.rdbb.zhiyu.core.ui.theme.Ink3
import com.rdbb.zhiyu.core.ui.theme.Line
import com.rdbb.zhiyu.core.ui.theme.Paper
import com.rdbb.zhiyu.core.util.compactCount

@Composable
fun MineScreen(
    onOpenPost: (Int) -> Unit,
    onEditProfile: () -> Unit,
    onOpenFollowList: (Int, String) -> Unit,
    onLoggedOut: () -> Unit,
    viewModel: MineViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.onResume()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    var menuExpanded by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showPasswordDialog) {
        PasswordChangeDialog(
            onDismiss = { showPasswordDialog = false },
            onConfirm = { old, new, cb -> viewModel.changePassword(old, new, cb) }
        )
    }
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("退出登录") },
            text = { Text("确定要退出当前账号吗？") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    viewModel.logout { onLoggedOut() }
                }) { Text("退出", color = Brand) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("取消", color = Ink2) }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
    ) {
        item {
            // 顶部暖色头部
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFFFE4D6), Color(0xFFFCD9CC), Color(0xFFF8EFE3))
                        )
                    )
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, bottom = 18.dp)
            ) {
                Box(Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 6.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Settings,
                            contentDescription = "设置",
                            tint = Color(0xFF7A5142),
                            modifier = Modifier
                                .size(34.dp)
                                .clickable { menuExpanded = true }
                        )
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text("修改密码") },
                                leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                                onClick = {
                                    menuExpanded = false
                                    showPasswordDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("退出登录") },
                                leadingIcon = { Icon(Icons.Outlined.Logout, null, tint = Brand) },
                                onClick = {
                                    menuExpanded = false
                                    showLogoutDialog = true
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Avatar(state.me, size = 66.dp)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                state.me?.nickname ?: "…",
                                fontSize = 21.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF3A2C25)
                            )
                            GenderBadge(state.me?.gender ?: 0)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            state.me?.bio ?: "这个人很神秘",
                            fontSize = 12.5.sp,
                            color = Color(0xFF8A6654)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(26.dp)) {
                    StatItem(
                        value = state.me?.followingCount ?: 0,
                        label = "关注"
                    ) { state.me?.let { onOpenFollowList(it.id, "following") } }
                    StatItem(
                        value = state.me?.followerCount ?: 0,
                        label = "粉丝"
                    ) { state.me?.let { onOpenFollowList(it.id, "followers") } }
                    StatItem(value = state.me?.postCount ?: 0, label = "帖子") {}
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 14.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProfileAction("编辑资料", brand = true, modifier = Modifier.weight(1f), onClick = onEditProfile)
                ProfileAction(
                    "分享主页",
                    modifier = Modifier.weight(1f)
                ) {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "我在知遇社区：${state.me?.nickname ?: ""}，一起来记录校园生活吧！"
                        )
                    }
                    context.startActivity(Intent.createChooser(intent, "分享主页"))
                }
                ProfileAction(
                    "我的收藏",
                    modifier = Modifier.weight(1f)
                ) { viewModel.switchTab(MineTab.FAVORITES) }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(40.dp)
            ) {
                MineTab.entries.forEach { tab ->
                    val selected = state.tab == tab
                    Column(
                        modifier = Modifier
                            .padding(end = 26.dp)
                            .clickable { viewModel.switchTab(tab) }
                    ) {
                        Spacer(Modifier.weight(1f))
                        Text(
                            tab.label,
                            fontSize = if (selected) 16.sp else 14.sp,
                            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Normal,
                            color = if (selected) Ink else Ink2
                        )
                        Spacer(Modifier.height(7.dp))
                    }
                }
            }
        }

        item {
            when {
                state.loading -> LoadingBox(Modifier.fillMaxWidth().height(240.dp))
                state.posts.isEmpty() ->
                    EmptyBox(
                        when (state.tab) {
                            MineTab.POSTS -> "还没有发布过动态"
                            MineTab.FAVORITES -> "还没有收藏内容"
                            MineTab.LIKED -> "还没有赞过的内容"
                        },
                        modifier = Modifier.fillMaxWidth().height(240.dp)
                    )
                else -> {
                    val rows = state.posts.chunked(2)
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rows.forEach { pair ->
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                MasonryCard(pair[0], Modifier.weight(1f), onOpenPost)
                                if (pair.size > 1) {
                                    MasonryCard(pair[1], Modifier.weight(1f), onOpenPost)
                                } else {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatItem(value: Int, label: String, onClick: () -> Unit) {
    Row(modifier = Modifier.clickable(onClick = onClick)) {
        Text(
            compactCount(value),
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF3A2C25)
        )
        Spacer(Modifier.width(5.dp))
        Text(label, fontSize = 12.5.sp, color = Color(0xFF7A5142))
    }
}

@Composable
private fun GenderBadge(gender: Int) {
    if (gender == 0) return
    val color = if (gender == 1) Color(0xFF3D7EFF) else Color(0xFFE05687)
    val symbol = if (gender == 1) "♂" else "♀"
    Box(
        modifier = Modifier
            .padding(start = 6.dp)
            .size(18.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(symbol, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ProfileAction(
    text: String,
    brand: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (brand) Brand else CardWhite)
            .then(
                if (brand) Modifier
                else Modifier.border(1.dp, Line, RoundedCornerShape(18.dp))
            )
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (brand) Color.White else Ink
        )
    }
}

@Composable
private fun MasonryCard(post: Post, modifier: Modifier = Modifier, onOpen: (Int) -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CardWhite)
            .clickable { onOpen(post.id) }
    ) {
        if (post.images.isNotEmpty()) {
            NetworkImage(
                url = post.images.first(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(118.dp),
                cornerRadius = 0.dp
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(118.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF5A6378), Color(0xFF2C3142))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(post.title.take(1), color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            }
        }
        Column(Modifier.padding(horizontal = 11.dp, vertical = 9.dp)) {
            Text(
                post.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(7.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Favorite, null, tint = Ink3, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(5.dp))
                Text(compactCount(post.likeCount), fontSize = 11.sp, color = Ink3)
                Spacer(Modifier.width(10.dp))
                Icon(Icons.Outlined.Visibility, null, tint = Ink3, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(5.dp))
                Text(compactCount(post.viewCount), fontSize = 11.sp, color = Ink3)
            }
        }
    }
}

@Composable
private fun PasswordChangeDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, (Boolean, String?) -> Unit) -> Unit
) {
    var old by remember { mutableStateOf("") }
    var new by remember { mutableStateOf("") }
    var tip by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("修改密码", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = old,
                    onValueChange = { old = it },
                    label = { Text("原密码") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = new,
                    onValueChange = { new = it },
                    label = { Text("新密码（6-20位）") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                if (tip != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(tip!!, fontSize = 12.sp, color = Brand)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (old.isBlank()) {
                    tip = "请输入原密码"
                    return@TextButton
                }
                onConfirm(old, new) { ok, msg ->
                    if (ok) onDismiss() else tip = msg
                }
            }) { Text("确认", color = Brand) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消", color = Ink2) }
        }
    )
}
