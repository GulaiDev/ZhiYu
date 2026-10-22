package com.rdbb.zhiyu.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rdbb.zhiyu.core.model.Community
import com.rdbb.zhiyu.core.ui.components.CommunityTile
import com.rdbb.zhiyu.core.ui.components.EmptyBox
import com.rdbb.zhiyu.core.ui.components.ErrorBox
import com.rdbb.zhiyu.core.ui.components.LoadingBox
import com.rdbb.zhiyu.core.ui.components.PostCard
import com.rdbb.zhiyu.core.ui.theme.Brand
import com.rdbb.zhiyu.core.ui.theme.CardWhite
import com.rdbb.zhiyu.core.ui.theme.CommunityGradients
import com.rdbb.zhiyu.core.ui.theme.Ink
import com.rdbb.zhiyu.core.ui.theme.Ink2
import com.rdbb.zhiyu.core.ui.theme.Ink3
import com.rdbb.zhiyu.core.ui.theme.Line
import com.rdbb.zhiyu.core.ui.theme.Paper

@Composable
fun HomeScreen(
    onOpenPost: (Int) -> Unit,
    onOpenUser: (Int) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenCommunity: (Int) -> Unit,
    onOpenCommunitiesTab: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state = viewModel.state
    val listState = rememberLazyListState()
    val reachEnd by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            last >= state.posts.size - 3
        }
    }
    LaunchedEffect(reachEnd, state.tab) {
        if (reachEnd && state.posts.isNotEmpty()) viewModel.loadMore()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
    ) {
        // 顶栏
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "知遇",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Ink
            )
            Spacer(Modifier.width(12.dp))
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(19.dp))
                    .background(CardWhite)
                    .clickable(onClick = onOpenSearch)
                    .padding(horizontal = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Search, contentDescription = null, tint = Ink3, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(7.dp))
                Text("樱花 / Compose / 二手自行车", fontSize = 13.sp, color = Ink3, maxLines = 1)
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clickable(onClick = onOpenNotifications),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Notifications, contentDescription = "消息", tint = Ink, modifier = Modifier.size(22.dp))
                if (state.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 8.dp, end = 8.dp)
                            .size(8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Brand)
                    )
                }
            }
        }

        // 排序 Tab
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(40.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            FeedTab.entries.forEach { tab ->
                val selected = state.tab == tab
                Column(
                    modifier = Modifier
                        .padding(end = 24.dp)
                        .clickable { viewModel.switchTab(tab) }
                        .padding(bottom = 7.dp)
                ) {
                    Text(
                        tab.label,
                        fontSize = if (selected) 18.sp else 16.sp,
                        fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                        color = if (selected) Ink else Ink2
                    )
                    Box(
                        modifier = Modifier
                            .padding(top = 5.dp)
                            .align(Alignment.CenterHorizontally)
                            .width(22.dp)
                            .height(3.5.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (selected) Brand else Color.Transparent)
                    )
                }
            }
        }

        // 圈子横滑
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 6.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
        ) {
            items(state.communities.take(5)) { community ->
                CommunityStripItem(community) { onOpenCommunity(community.id) }
            }
            item {
                CommunityStripItem(community = null, label = "更多") { onOpenCommunitiesTab() }
            }
        }

        // 信息流
        when {
            state.loading && state.posts.isEmpty() -> LoadingBox()
            state.error != null && state.posts.isEmpty() ->
                ErrorBox(state.error!!, onRetry = { viewModel.refresh() })
            state.posts.isEmpty() -> EmptyBox(
                if (state.tab == FeedTab.FOLLOWING) "还没有关注的人发动态\n去发现页看看吧" else "暂无内容"
            )
            else -> LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    top = 4.dp, bottom = 12.dp
                )
            ) {
                items(state.posts, key = { it.id }) { post ->
                    PostCard(
                        post = post,
                        modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                        onOpen = { onOpenPost(post.id) },
                        onOpenAuthor = { onOpenUser(post.author.id) },
                        onToggleLike = { viewModel.toggleLike(post) },
                        onToggleFavorite = { viewModel.toggleFavorite(post) },
                        onFollow = { viewModel.follow(post.author.id, post.id) }
                    )
                }
                if (state.loadingMore) {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            com.rdbb.zhiyu.core.ui.components.LoadingBox(
                                modifier = Modifier.height(30.dp).fillMaxWidth()
                            )
                        }
                    }
                } else if (!state.hasMore) {
                    item {
                        Text(
                            "— 没有更多了 —",
                            fontSize = 12.sp,
                            color = Ink3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CommunityStripItem(community: Community?, label: String? = null, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(52.dp).clickable(onClick = onClick)
    ) {
        if (community != null) {
            CommunityTile(community = community, size = 48.dp, cornerRadius = 16.dp)
            Spacer(Modifier.height(5.dp))
            Text(
                label ?: community.name,
                fontSize = 11.sp,
                color = Ink2,
                maxLines = 1
            )
        } else {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFEFEAE0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null, tint = Ink3)
            }
            Spacer(Modifier.height(5.dp))
            Text(label ?: "更多", fontSize = 11.sp, color = Ink2)
        }
    }
}
