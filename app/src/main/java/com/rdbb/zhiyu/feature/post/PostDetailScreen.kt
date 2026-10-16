package com.rdbb.zhiyu.feature.post

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rdbb.zhiyu.core.network.Comment
import com.rdbb.zhiyu.core.network.Post
import com.rdbb.zhiyu.core.ui.components.Avatar
import com.rdbb.zhiyu.core.ui.components.EmptyBox
import com.rdbb.zhiyu.core.ui.components.ErrorBox
import com.rdbb.zhiyu.core.ui.components.FollowPill
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
import com.rdbb.zhiyu.core.util.timeAgo

@Composable
fun PostDetailScreen(
    onBack: () -> Unit,
    onOpenUser: (Int) -> Unit,
    viewModel: PostDetailViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
    ) {
        when {
            state.loading -> LoadingBox()
            state.error != null && state.post == null ->
                ErrorBox(state.error ?: "加载失败", onRetry = { viewModel.retry() })
            state.post != null -> {
                val post = state.post!!
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    if (post.images.isNotEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(264.dp)
                            ) {
                                NetworkImage(
                                    url = post.images.first(),
                                    modifier = Modifier.fillMaxSize(),
                                    cornerRadius = 0.dp
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color(0x55000000), Color.Transparent, Color(0x22000000))
                                            )
                                        )
                                )
                                BackButton(
                                    light = true,
                                    modifier = Modifier
                                        .statusBarsPadding()
                                        .padding(start = 12.dp, top = 6.dp),
                                    onBack = onBack
                                )
                            }
                        }
                    } else {
                        item {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .statusBarsPadding()
                                    .padding(start = 12.dp, top = 6.dp)
                            ) {
                                BackButton(light = false, onBack = onBack)
                            }
                        }
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                                .background(CardWhite)
                                .padding(horizontal = 18.dp)
                                .padding(top = 18.dp)
                        ) {
                            // 作者栏
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Avatar(post.author, size = 42.dp, onClick = { onOpenUser(post.author.id) })
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        post.author.nickname,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Ink
                                    )
                                    Text(timeAgo(post.createdAt), fontSize = 11.sp, color = Ink3)
                                }
                                val isOwn = viewModel.currentUserId == post.author.id
                                if (!isOwn) {
                                    FollowPill(
                                        following = post.author.isFollowing,
                                        onClick = { viewModel.toggleFollow() }
                                    )
                                }
                            }

                            Spacer(Modifier.height(14.dp))
                            Text(
                                post.title,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Ink,
                                lineHeight = 27.sp
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                post.content,
                                fontSize = 15.sp,
                                color = Color(0xFF3F3D38),
                                lineHeight = 24.sp
                            )

                            // 其余图片
                            if (post.images.size > 1) {
                                Spacer(Modifier.height(12.dp))
                                post.images.drop(1).forEach { img ->
                                    NetworkImage(
                                        url = img,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 10.dp),
                                        cornerRadius = 12.dp
                                    )
                                }
                            }

                            Spacer(Modifier.height(12.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                StatIcon(Icons.Outlined.Visibility, compactCount(post.viewCount), Ink3)
                                StatIcon(
                                    if (post.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    compactCount(post.likeCount),
                                    if (post.isLiked) Brand else Ink3
                                )
                                StatIcon(
                                    Icons.Outlined.ChatBubbleOutline,
                                    compactCount(post.commentCount),
                                    Ink3
                                )
                            }
                        }
                    }

                    item {
                        Spacer(Modifier.height(9.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CardWhite)
                                .padding(horizontal = 18.dp, vertical = 14.dp)
                        ) {
                            Text(
                                "评论 ${post.commentCount}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )
                            Spacer(Modifier.height(6.dp))
                            if (state.comments.isEmpty() && !state.commentsLoading) {
                                EmptyBox("还没有评论，快来抢沙发", modifier = Modifier.fillMaxWidth().height(150.dp))
                            }
                        }
                    }

                    items(state.comments.size) { index ->
                        RootComment(
                            comment = state.comments[index],
                            onOpenUser = onOpenUser,
                            onLike = { viewModel.toggleCommentLike(it) },
                            onReply = { root, replyToUserId, replyToName ->
                                viewModel.setReplyTarget(
                                    ReplyTarget(
                                        parent = root,
                                        replyToUserId = replyToUserId,
                                        replyToName = replyToName
                                    )
                                )
                            },
                            isLast = index == state.comments.lastIndex
                        )
                    }

                    item { Spacer(Modifier.height(80.dp)) }
                }

                // 底部操作条
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(CardWhite)
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Line)
                    )
                    state.replyTarget?.let { target ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFBF8F2))
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "回复 @${target.replyToName}",
                                fontSize = 12.sp,
                                color = Ink2,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "取消",
                                fontSize = 12.sp,
                                color = Ink3,
                                modifier = Modifier.clickable { viewModel.setReplyTarget(null) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .imePadding()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.TextField(
                            value = state.draft,
                            onValueChange = { viewModel.onDraftChange(it) },
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = Ink),
                            placeholder = {
                                Text(
                                    state.replyTarget?.let { "回复 @${it.replyToName}" } ?: "说点什么…",
                                    fontSize = 14.sp,
                                    color = Ink3
                                )
                            },
                            shape = RoundedCornerShape(21.dp),
                            colors = androidx.compose.material3.TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF4F1EA),
                                unfocusedContainerColor = Color(0xFFF4F1EA),
                                disabledContainerColor = Color(0xFFF4F1EA),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                focusedPlaceholderColor = Ink3,
                                unfocusedPlaceholderColor = Ink3,
                                cursorColor = Brand
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        // 点赞
                        BottomCircleIcon(
                            icon = if (post.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            tint = if (post.isLiked) Brand else Ink2
                        ) { viewModel.toggleLike() }
                        // 收藏
                        BottomCircleIcon(
                            icon = if (post.isFavorited) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            tint = if (post.isFavorited) Color(0xFFF5A623) else Ink2
                        ) { viewModel.toggleFavorite() }
                        // 分享
                        BottomCircleIcon(icon = Icons.Outlined.IosShare, tint = Ink2) {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TITLE, post.title)
                                putExtra(Intent.EXTRA_TEXT, "${post.title}\n来自知遇校园社区")
                            }
                            context.startActivity(Intent.createChooser(intent, "分享帖子"))
                        }
                        Spacer(Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (state.draft.isNotBlank()) Brand else Color(0xFFDDD8CD))
                                .clickable(enabled = state.draft.isNotBlank() && !state.sending) {
                                    viewModel.sendComment { }
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            if (state.sending) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Text("发送", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BackButton(light: Boolean, modifier: Modifier = Modifier, onBack: () -> Unit) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(if (light) Color(0x40000000) else Color(0x14000000))
            .clickable(onClick = onBack),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.AutoMirrored.Outlined.ArrowBack,
            contentDescription = "返回",
            tint = if (light) Color.White else Ink,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun StatIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: String,
    tint: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(count, fontSize = 12.sp, color = tint)
    }
}

@Composable
private fun BottomCircleIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .size(36.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun RootComment(
    comment: Comment,
    onOpenUser: (Int) -> Unit,
    onLike: (Comment) -> Unit,
    onReply: (Comment, Int?, String) -> Unit,
    isLast: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardWhite)
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        Row {
            Avatar(comment.author, size = 38.dp, onClick = { onOpenUser(comment.author.id) })
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(comment.author.nickname, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                Spacer(Modifier.height(3.dp))
                Text(comment.content, fontSize = 14.sp, color = Color(0xFF33312C), lineHeight = 21.sp)
                Spacer(Modifier.height(7.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(timeAgo(comment.createdAt), fontSize = 11.sp, color = Ink3)
                    Spacer(Modifier.width(14.dp))
                    Text(
                        "回复",
                        fontSize = 11.sp,
                        color = Ink3,
                        modifier = Modifier.clickable {
                            onReply(comment, comment.author.id, comment.author.nickname)
                        }
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onLike(comment) }
                    .padding(4.dp)
            ) {
                Icon(
                    if (comment.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "点赞",
                    tint = if (comment.isLiked) Brand else Ink3,
                    modifier = Modifier.size(16.dp)
                )
                if (comment.likeCount > 0) {
                    Text(compactCount(comment.likeCount), fontSize = 10.sp, color = Ink3)
                }
            }
        }

        // 楼中楼
        if (comment.replies.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .padding(start = 48.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF8F6F1))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                comment.replies.forEach { reply ->
                    ReplyRow(
                        reply = reply,
                        onOpenUser = onOpenUser,
                        onLike = onLike,
                        onReply = {
                            onReply(comment, reply.author.id, reply.author.nickname)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReplyRow(
    reply: Comment,
    onOpenUser: (Int) -> Unit,
    onLike: (Comment) -> Unit,
    onReply: () -> Unit
) {
    Row {
        Column(Modifier.weight(1f)) {
            Row {
                Text(
                    reply.author.nickname,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink2,
                    modifier = Modifier.clickable { onOpenUser(reply.author.id) }
                )
                if (reply.replyToUser != null) {
                    Text(" 回复 ", fontSize = 12.sp, color = Ink3)
                    Text(
                        reply.replyToUser.nickname,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Ink2,
                        modifier = Modifier.clickable { onOpenUser(reply.replyToUser.id) }
                    )
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(reply.content, fontSize = 13.sp, color = Color(0xFF33312C), lineHeight = 19.sp)
            Spacer(Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(timeAgo(reply.createdAt), fontSize = 10.sp, color = Ink3)
                Spacer(Modifier.width(12.dp))
                Text(
                    "回复",
                    fontSize = 10.sp,
                    color = Ink3,
                    modifier = Modifier.clickable(onClick = onReply)
                )
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable { onLike(reply) }
                .padding(4.dp)
        ) {
            Icon(
                if (reply.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "点赞",
                tint = if (reply.isLiked) Brand else Ink3,
                modifier = Modifier.size(14.dp)
            )
            if (reply.likeCount > 0) {
                Text(compactCount(reply.likeCount), fontSize = 10.sp, color = Ink3)
            }
        }
    }
}
