package com.rdbb.zhiyu.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rdbb.zhiyu.core.network.Post
import com.rdbb.zhiyu.core.ui.theme.Amber
import com.rdbb.zhiyu.core.ui.theme.Brand
import com.rdbb.zhiyu.core.ui.theme.CardWhite
import com.rdbb.zhiyu.core.ui.theme.Ink
import com.rdbb.zhiyu.core.ui.theme.Ink2
import com.rdbb.zhiyu.core.ui.theme.Ink3
import com.rdbb.zhiyu.core.ui.theme.Line
import com.rdbb.zhiyu.core.util.compactCount
import com.rdbb.zhiyu.core.util.timeAgo

/** 信息流帖子卡片（首页/搜索/圈子详情复用）。 */
@Composable
fun PostCard(
    post: Post,
    modifier: Modifier = Modifier,
    onOpen: () -> Unit = {},
    onOpenAuthor: () -> Unit = {},
    onToggleLike: (() -> Unit)? = null,
    onToggleFavorite: (() -> Unit)? = null,
    onFollow: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardWhite)
            .clickable(onClick = onOpen)
            .padding(14.dp)
    ) {
        // 作者行
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(post.author, size = 34.dp, onClick = onOpenAuthor)
            Spacer(Modifier.width(9.dp))
            Column(Modifier.weight(1f)) {
                Text(post.author.nickname, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Ink)
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    post.community?.let { SoftTag(it.name) }
                    if (post.community != null) {
                        Text(" · ", fontSize = 11.sp, color = Ink3)
                    }
                    Text(timeAgo(post.createdAt), fontSize = 11.sp, color = Ink3)
                }
            }
            if (onFollow != null && !post.author.isFollowing) {
                FollowPill(following = false, onClick = onFollow)
            }
        }

        Spacer(Modifier.height(10.dp))
        Text(
            text = post.title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 23.sp,
            color = Ink
        )

        if (post.images.isEmpty() && post.content.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = post.content,
                fontSize = 13.5.sp,
                lineHeight = 21.sp,
                color = Ink2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (post.images.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            if (post.images.size == 1) {
                NetworkImage(
                    url = post.images.first(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            } else {
                val rows = post.images.take(9).chunked(3)
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    rows.forEach { rowImgs ->
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            rowImgs.forEach { img ->
                                NetworkImage(
                                    url = img,
                                    modifier = Modifier.weight(1f).aspectRatio(1f)
                                )
                            }
                            repeat(3 - rowImgs.size) {
                                Spacer(Modifier.weight(1f).aspectRatio(1f))
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(11.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            ActionIcon(
                filled = post.isLiked,
                count = post.likeCount,
                onClick = onToggleLike
            )
            Spacer(Modifier.width(22.dp))
            ActionIcon(
                icon = Icons.Outlined.ChatBubbleOutline,
                tint = Ink2,
                count = post.commentCount
            )
            Spacer(Modifier.weight(1f))
            ActionIcon(
                icon = if (post.isFavorited) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                tint = if (post.isFavorited) Amber else Ink2,
                count = post.favoriteCount,
                onClick = onToggleFavorite
            )
        }
    }
}

@Composable
private fun ActionIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    count: Int,
    onClick: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(5.dp))
        Text(compactCount(count), fontSize = 12.5.sp, color = if (tint == Ink2) Ink2 else tint)
    }
}

@Composable
private fun ActionIcon(
    filled: Boolean,
    count: Int,
    onClick: (() -> Unit)? = null
) {
    ActionIcon(
        icon = if (filled) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
        tint = if (filled) Brand else Ink2,
        count = count,
        onClick = onClick
    )
}

/** “+ 关注 / 已关注”小胶囊。 */
@Composable
fun FollowPill(following: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val (text, color) = if (following) "已关注" to Ink3 else "+ 关注" to Brand
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, color, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 3.dp)
    ) {
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}

/** 圈子页“加入 / 已加入”按钮。 */
@Composable
fun JoinButton(joined: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val color = if (joined) Ink3 else Brand
    val bg = if (joined) Color(0xFFF6F3EC) else CardWhite
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(1.dp, if (joined) Line else Brand, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 6.dp)
    ) {
        Text(
            text = if (joined) "已加入" else "+ 加入",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}
