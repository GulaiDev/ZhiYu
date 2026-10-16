package com.rdbb.zhiyu.feature.community

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardBackspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rdbb.zhiyu.core.ui.components.CommunityTile
import com.rdbb.zhiyu.core.ui.components.ErrorBox
import com.rdbb.zhiyu.core.ui.components.JoinButton
import com.rdbb.zhiyu.core.ui.components.LoadingBox
import com.rdbb.zhiyu.core.ui.components.NetworkImage
import com.rdbb.zhiyu.core.ui.components.PostCard
import com.rdbb.zhiyu.core.ui.theme.Brand
import com.rdbb.zhiyu.core.ui.theme.CardWhite
import com.rdbb.zhiyu.core.ui.theme.Ink
import com.rdbb.zhiyu.core.ui.theme.Ink2
import com.rdbb.zhiyu.core.ui.theme.Ink3
import com.rdbb.zhiyu.core.ui.theme.Line
import com.rdbb.zhiyu.core.ui.theme.Paper
import com.rdbb.zhiyu.core.util.compactCount

@Composable
fun CommunityDetailScreen(
    onBack: () -> Unit,
    onOpenPost: (Int) -> Unit,
    onOpenUser: (Int) -> Unit,
    viewModel: CommunityDetailViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val listState = rememberLazyListState()
    val reachEnd by remember {
        derivedStateOf {
            (listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) >= state.posts.size - 2
        }
    }
    LaunchedEffect(reachEnd) { if (reachEnd && state.posts.isNotEmpty()) viewModel.loadMore() }

    Scaffold(
        containerColor = Paper,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0)
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(38.dp).clip(CircleShape)
                        .clickableNoRipple(onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Outlined.KeyboardBackspace, contentDescription = "返回", tint = Ink)
                }
                Text(
                    state.community?.name ?: "圈子详情",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
            }

            when {
                state.loading && state.community == null -> LoadingBox()
                state.error != null && state.community == null ->
                    ErrorBox(state.error!!, onRetry = { viewModel.load() })
                else -> LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp)
                ) {
                    item {
                        val c = state.community
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(16.dp))
                        ) {
                            if (!c?.cover.isNullOrBlank()) {
                                NetworkImage(c!!.cover!!, modifier = Modifier.fillMaxSize(), cornerRadius = 16.dp)
                            } else {
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Brand.copy(alpha = 0.85f), Ink3)
                                            )
                                        )
                                )
                            }
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .background(Brush.verticalGradient(listOf(androidx.compose.ui.graphics.Color.Transparent, androidx.compose.ui.graphics.Color(0x66000000))))
                                    .padding(16.dp),
                                contentAlignment = Alignment.BottomStart
                            ) {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    CommunityTile(c, size = 46.dp, cornerRadius = 14.dp)
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(c?.name ?: "", color = CardWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                        Text(
                                            "${compactCount(c?.memberCount ?: 0)} 成员 · ${compactCount(c?.postCount ?: 0)} 帖子",
                                            color = CardWhite.copy(alpha = 0.9f),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                state.community?.description ?: "暂无简介",
                                fontSize = 13.sp,
                                color = Ink2,
                                modifier = Modifier.weight(1f)
                            )
                            state.community?.let {
                                JoinButton(joined = it.isJoined, onClick = { viewModel.toggleJoin() })
                            }
                        }
                        Box(Modifier.fillMaxWidth().height(0.8.dp).background(Line))
                    }
                    items(state.posts.size) { index ->
                        val post = state.posts[index]
                        PostCard(
                            post = post,
                            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp),
                            onOpen = { onOpenPost(post.id) },
                            onOpenAuthor = { onOpenUser(post.author.id) },
                            onToggleLike = { viewModel.toggleLike(post) },
                            onToggleFavorite = { viewModel.toggleFavorite(post) },
                            onFollow = { viewModel.follow(post.author.id, post.id) }
                        )
                    }
                    if (state.loadingMore) {
                        item {
                            Text(
                                "加载中…",
                                fontSize = 12.sp,
                                color = Ink3,
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    clickable(
        interactionSource = MutableInteractionSource(),
        indication = null,
        onClick = onClick
    )
