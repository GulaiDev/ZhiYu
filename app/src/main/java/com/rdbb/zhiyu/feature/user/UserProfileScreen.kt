package com.rdbb.zhiyu.feature.user

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.MoreVert
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rdbb.zhiyu.core.network.Post
import com.rdbb.zhiyu.core.ui.components.Avatar
import com.rdbb.zhiyu.core.ui.components.EmptyBox
import com.rdbb.zhiyu.core.ui.components.ErrorBox
import com.rdbb.zhiyu.core.ui.components.LoadingBox
import com.rdbb.zhiyu.core.ui.components.NetworkImage
import com.rdbb.zhiyu.core.ui.theme.Brand
import com.rdbb.zhiyu.core.ui.theme.CardWhite
import com.rdbb.zhiyu.core.ui.theme.Ink
import com.rdbb.zhiyu.core.ui.theme.Ink2
import com.rdbb.zhiyu.core.ui.theme.Line
import com.rdbb.zhiyu.core.ui.theme.Paper
import com.rdbb.zhiyu.core.util.compactCount

@Composable
fun UserProfileScreen(
    onBack: () -> Unit,
    onOpenPost: (Int) -> Unit,
    onOpenFollowList: (Int, String) -> Unit,
    viewModel: UserProfileViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val context = LocalContext.current

    when {
        state.loading -> Box(Modifier.fillMaxSize().background(Paper)) { LoadingBox() }
        state.error != null && state.user == null ->
            ErrorBox(state.error ?: "加载失败", onRetry = { viewModel.load() })
        state.user == null -> EmptyBox("用户不存在")
        else -> {
            val user = state.user!!
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Paper)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFFFDCCB), Color(0xFFFBD2C3), Color(0xFFF7E9D8))
                                )
                            )
                            .statusBarsPadding()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, end = 12.dp, top = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x40000000))
                                    .clickable(onClick = onBack),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.ArrowBack,
                                    contentDescription = "返回",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.weight(1f))
                            var menu by remember { mutableStateOf(false) }
                            Box {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x40000000))
                                        .clickable { menu = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Outlined.MoreVert,
                                        contentDescription = "更多",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                                    DropdownMenuItem(
                                        text = { Text("分享主页") },
                                        onClick = {
                                            menu = false
                                            val intent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(
                                                    Intent.EXTRA_TEXT,
                                                    "来知遇看看 ${user.nickname} 的主页吧！"
                                                )
                                            }
                                            context.startActivity(Intent.createChooser(intent, "分享主页"))
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(18.dp))
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Avatar(user, size = 64.dp)
                            Spacer(Modifier.width(15.dp))
                            Column {
                                Text(
                                    user.nickname,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF3A2C25)
                                )
                                Spacer(Modifier.height(5.dp))
                                Text(
                                    "@${user.username}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF96735F)
                                )
                            }
                        }

                        user.bio?.takeIf { it.isNotBlank() }?.let {
                            Spacer(Modifier.height(10.dp))
                            Text(
                                it,
                                fontSize = 13.sp,
                                color = Color(0xFF6E5041),
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(horizontal = 20.dp)
                            )
                        }

                        Spacer(Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            UserStat(user.followingCount, "关注") {
                                onOpenFollowList(user.id, "following")
                            }
                            UserStat(user.followerCount, "粉丝") {
                                onOpenFollowList(user.id, "followers")
                            }
                            UserStat(user.postCount, "帖子") {}
                        }

                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(if (user.isFollowing) CardWhite else Brand)
                                    .then(
                                        if (user.isFollowing)
                                            Modifier.clip(RoundedCornerShape(18.dp))
                                        else Modifier
                                    )
                                    .clickable { viewModel.toggleFollow() }
                                    .padding(vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (user.isFollowing) "已关注" else "+ 关注",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (user.isFollowing) Ink2 else Color.White
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(CardWhite)
                                    .clickable {
                                        android.widget.Toast
                                            .makeText(context, "私信功能即将上线", android.widget.Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                    .padding(vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Outlined.Email,
                                        contentDescription = null,
                                        tint = Ink2,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(Modifier.width(5.dp))
                                    Text("私信", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = Ink2)
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Paper)
                            .padding(start = 20.dp, top = 14.dp, bottom = 10.dp)
                    ) {
                        Text(
                            "全部动态",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("${user.postCount}", fontSize = 12.sp, color = Ink2)
                    }
                }

                if (state.posts.isEmpty()) {
                    item {
                        EmptyBox("TA 还没有发布动态", modifier = Modifier.fillMaxWidth().height(200.dp))
                    }
                } else {
                    val rows = state.posts.chunked(3)
                    item {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rows.forEach { triple ->
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    triple.forEach { post ->
                                        GridPost(post, Modifier.weight(1f), onOpenPost)
                                    }
                                    repeat(3 - triple.size) {
                                        Spacer(Modifier.weight(1f).aspectRatio(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun UserStat(value: Int, label: String, onClick: () -> Unit) {
    Row(modifier = Modifier.clickable(onClick = onClick)) {
        Text(compactCount(value), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF3A2C25))
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 12.sp, color = Color(0xFF87634F))
    }
}

@Composable
private fun GridPost(post: Post, modifier: Modifier, onOpen: (Int) -> Unit) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onOpen(post.id) }
    ) {
        if (post.images.isNotEmpty()) {
            NetworkImage(
                url = post.images.first(),
                modifier = Modifier.fillMaxSize(),
                cornerRadius = 8.dp
            )
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(listOf(Color(0xFF5A6378), Color(0xFF2C3142)))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    post.title,
                    color = Color.White,
                    fontSize = 11.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
        // 底部渐变遮罩 + 标题
        if (post.images.isNotEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0x66000000))
                        )
                    )
                    .padding(6.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Text(
                    post.title,
                    color = Color.White,
                    fontSize = 10.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
