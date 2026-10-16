package com.rdbb.zhiyu.feature.community

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rdbb.zhiyu.R
import com.rdbb.zhiyu.core.network.Community
import com.rdbb.zhiyu.core.ui.components.CommunityTile
import com.rdbb.zhiyu.core.ui.components.EmptyBox
import com.rdbb.zhiyu.core.ui.components.ErrorBox
import com.rdbb.zhiyu.core.ui.components.JoinButton
import com.rdbb.zhiyu.core.ui.components.LoadingBox
import com.rdbb.zhiyu.core.ui.theme.CardWhite
import com.rdbb.zhiyu.core.ui.theme.Ink
import com.rdbb.zhiyu.core.ui.theme.Ink3
import com.rdbb.zhiyu.core.ui.theme.Paper
import com.rdbb.zhiyu.core.util.compactCount

@Composable
fun CommunitiesScreen(
    onOpenSearch: () -> Unit,
    onOpenCommunity: (Int) -> Unit,
    viewModel: CommunitiesViewModel = hiltViewModel()
) {
    val state = viewModel.state

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
            Text("圈子", fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clickable(onClick = onOpenSearch),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Search, contentDescription = "搜索", tint = Ink, modifier = Modifier.size(22.dp))
            }
        }

        when {
            state.loading && state.communities.isEmpty() -> LoadingBox()
            state.error != null && state.communities.isEmpty() ->
                ErrorBox(state.error!!, onRetry = { viewModel.refresh() })
            state.communities.isEmpty() -> EmptyBox("还没有圈子")
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 12.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 18.dp)
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                state.communities.firstOrNull { it.name.contains("摄影") }
                                    ?.let { onOpenCommunity(it.id) }
                            }
                    ) {
                        Image(
                            painter = painterResource(R.drawable.community_banner),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0x9E1E1912), Color(0x141E1912))
                                    )
                                ),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Column(Modifier.padding(horizontal = 20.dp)) {
                                Text("摄影社秋季招新", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(6.dp))
                                Text("本周六学生活动中心 203，带上你的作品", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }

                val joined = state.communities.filter { it.isJoined }
                if (joined.isNotEmpty()) {
                    item { SectionHeader("我的圈子", "") }
                    item {
                        LazyRow(
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                start = 16.dp, end = 16.dp, bottom = 14.dp
                            ),
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
                        ) {
                            items(joined) { c ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.width(52.dp).clickable { onOpenCommunity(c.id) }
                                ) {
                                    CommunityTile(c, size = 48.dp, cornerRadius = 16.dp)
                                    Spacer(Modifier.height(5.dp))
                                    Text(c.name, fontSize = 11.sp, color = com.rdbb.zhiyu.core.ui.theme.Ink2, maxLines = 1)
                                }
                            }
                        }
                    }
                }

                item { SectionHeader("发现圈子", "${state.communities.size} 个圈子") }
                items(state.communities, key = { it.id }) { community ->
                    CommunityRow(
                        community = community,
                        onOpen = { onOpenCommunity(community.id) },
                        onToggleJoin = { viewModel.toggleJoin(community) },
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, more: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .padding(bottom = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ink)
        Spacer(Modifier.weight(1f))
        if (more.isNotBlank()) Text("$more ›", fontSize = 12.sp, color = Ink3)
    }
}

@Composable
private fun CommunityRow(
    community: Community,
    onOpen: () -> Unit,
    onToggleJoin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardWhite)
            .clickable(onClick = onOpen)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CommunityTile(community, size = 54.dp, cornerRadius = 14.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(community.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink)
            Spacer(Modifier.height(3.dp))
            Text(
                "${compactCount(community.memberCount)} 成员 · ${compactCount(community.postCount)} 帖子",
                fontSize = 11.5.sp,
                color = Ink3
            )
        }
        JoinButton(joined = community.isJoined, onClick = onToggleJoin)
    }
}
