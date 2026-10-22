package com.rdbb.zhiyu.feature.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Search
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rdbb.zhiyu.core.model.Community
import com.rdbb.zhiyu.core.model.Post
import com.rdbb.zhiyu.core.model.UserBrief
import com.rdbb.zhiyu.core.ui.components.Avatar
import com.rdbb.zhiyu.core.ui.components.CommunityTile
import com.rdbb.zhiyu.core.ui.components.EmptyBox
import com.rdbb.zhiyu.core.ui.components.ErrorBox
import com.rdbb.zhiyu.core.ui.components.FollowPill
import com.rdbb.zhiyu.core.ui.components.LoadingBox
import com.rdbb.zhiyu.core.ui.components.PostCard
import com.rdbb.zhiyu.core.ui.theme.Brand
import com.rdbb.zhiyu.core.ui.theme.BrandSoft
import com.rdbb.zhiyu.core.ui.theme.CardWhite
import com.rdbb.zhiyu.core.ui.theme.Ink
import com.rdbb.zhiyu.core.ui.theme.Ink2
import com.rdbb.zhiyu.core.ui.theme.Ink3
import com.rdbb.zhiyu.core.ui.theme.Line
import com.rdbb.zhiyu.core.ui.theme.Paper
import com.rdbb.zhiyu.core.util.compactCount

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onOpenPost: (Int) -> Unit,
    onOpenUser: (Int) -> Unit,
    onOpenCommunity: (Int) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state = viewModel.state
    var input by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
    ) {
        // 顶部搜索栏
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "返回",
                tint = Ink,
                modifier = Modifier
                    .size(38.dp)
                    .clickable(onClick = onBack)
            )
            Spacer(Modifier.width(4.dp))
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardWhite)
                    .border(1.5.dp, Brand.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 13.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Search, contentDescription = null, tint = Brand, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(7.dp))
                androidx.compose.material3.TextField(
                    value = input,
                    onValueChange = {
                        input = it
                        viewModel.onQueryChange(it)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .focusRequester(focusRequester),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = Ink),
                    placeholder = { Text("搜索帖子、用户或圈子", fontSize = 13.5.sp, color = Ink3) },
                    shape = RoundedCornerShape(20.dp),
                    colors = androidx.compose.material3.TextFieldDefaults.colors(
                        focusedContainerColor = CardWhite,
                        unfocusedContainerColor = CardWhite,
                        disabledContainerColor = CardWhite,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedPlaceholderColor = Ink3,
                        unfocusedPlaceholderColor = Ink3,
                        cursorColor = Brand
                    )
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                "搜索",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Brand,
                modifier = Modifier.clickable { viewModel.submit(input) }
            )
        }

        if (!state.searched) {
            // 热门搜索
            Column(Modifier.padding(horizontal = 18.dp, vertical = 10.dp)) {
                Text("热门搜索", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink)
                Spacer(Modifier.height(14.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    viewModel.hotKeywords.forEach { kw ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(CardWhite)
                                .border(1.dp, Line, RoundedCornerShape(16.dp))
                                .clickable {
                                    input = kw
                                    viewModel.submit(kw)
                                }
                                .padding(horizontal = 15.dp, vertical = 7.dp)
                        ) {
                            Text(kw, fontSize = 13.sp, color = Ink2)
                        }
                    }
                }
                Spacer(Modifier.height(26.dp))
                Text("试试搜索", fontSize = 13.sp, color = Ink3)
                Spacer(Modifier.height(8.dp))
                Text("校园生活相关的关键词，找到同频的同学～", fontSize = 12.5.sp, color = Ink3)
            }
            return@Column
        }

        // Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            SearchTab.entries.forEach { tab ->
                val selected = state.tab == tab
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.selectTab(tab) }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        tab.label,
                        fontSize = if (selected) 15.sp else 13.5.sp,
                        fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Normal,
                        color = if (selected) Brand else Ink2
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(
                        Modifier
                            .width(18.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (selected) Brand else Color.Transparent)
                    )
                }
            }
        }

        when {
            state.loading -> LoadingBox()
            state.error != null -> ErrorBox(
                state.error ?: "搜索失败",
                onRetry = { viewModel.submit(state.query) }
            )
            else -> {
                val empty = when (state.tab) {
                    SearchTab.ALL -> state.all?.let { it.users.isEmpty() && it.posts.isEmpty() && it.communities.isEmpty() } != false
                    SearchTab.USER -> state.users.isEmpty()
                    SearchTab.COMMUNITY -> state.communities.isEmpty()
                    SearchTab.POST -> state.posts.isEmpty()
                }
                if (empty) {
                    EmptyBox("没有找到相关内容，换个关键词试试")
                    return@Column
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 14.dp, vertical = 6.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    when (state.tab) {
                        SearchTab.ALL -> {
                            val all = state.all
                            if (all != null) {
                                if (all.users.isNotEmpty()) {
                                    item {
                                        SectionHeader("相关用户", count = all.users.size)
                                    }
                                    item {
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            items(all.users.size) { i ->
                                                UserCard(all.users[i], onOpen = onOpenUser)
                                            }
                                        }
                                    }
                                }
                                if (all.communities.isNotEmpty()) {
                                    item { SectionHeader("相关圈子", count = all.communities.size) }
                                    items(all.communities.size) { i ->
                                        CommunityRow(all.communities[i], onClick = { onOpenCommunity(it) })
                                    }
                                }
                                if (all.posts.isNotEmpty()) {
                                    item { SectionHeader("相关帖子", count = all.posts.size) }
                                    items(all.posts.size) { i ->
                                        PostCard(
                                            post = all.posts[i],
                                            onOpen = { onOpenPost(all.posts[i].id) },
                                            onOpenAuthor = { onOpenUser(all.posts[i].author.id) }
                                        )
                                    }
                                }
                            }
                        }
                        SearchTab.USER -> items(state.users.size) { i ->
                            UserRow(
                                user = state.users[i],
                                onOpen = { onOpenUser(state.users[i].id) },
                                onFollow = { viewModel.toggleFollow(state.users[i].id) }
                            )
                        }
                        SearchTab.COMMUNITY -> items(state.communities.size) { i ->
                            CommunityRow(state.communities[i]) { onOpenCommunity(state.communities[i].id) }
                        }
                        SearchTab.POST -> items(state.posts.size) { i ->
                            val post = state.posts[i]
                            PostCard(
                                post = post,
                                onOpen = { onOpenPost(post.id) },
                                onOpenAuthor = { onOpenUser(post.author.id) }
                            )
                        }
                    }
                    item { Spacer(Modifier.height(12.dp)) }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, top = 8.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink)
        Spacer(Modifier.width(6.dp))
        Text("$count", fontSize = 12.sp, color = Ink3)
    }
}

@Composable
private fun UserCard(user: UserBrief, onOpen: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .width(76.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CardWhite)
            .clickable { onOpen(user.id) }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Avatar(user, size = 48.dp)
        Spacer(Modifier.height(7.dp))
        Text(
            user.nickname,
            fontSize = 12.sp,
            color = Ink,
            maxLines = 1,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun UserRow(user: UserBrief, onOpen: () -> Unit, onFollow: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardWhite)
            .clickable(onClick = onOpen)
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(user, size = 46.dp)
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text(user.nickname, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Ink)
            Text(
                user.bio ?: "这个人很神秘",
                fontSize = 12.sp,
                color = Ink3,
                maxLines = 1
            )
        }
        FollowPill(following = user.isFollowing, onClick = onFollow)
    }
}

@Composable
private fun CommunityRow(community: Community, onClick: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardWhite)
            .clickable { onClick(community.id) }
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CommunityTile(community, size = 46.dp)
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Text(community.name, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Ink)
            Text(
                "${compactCount(community.memberCount)} 成员 · ${compactCount(community.postCount)} 帖子",
                fontSize = 12.sp,
                color = Ink3
            )
        }
        if (community.isJoined) {
            Box(
                Modifier
                    .clip(RoundedCornerShape(13.dp))
                    .background(BrandSoft)
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text("已加入", fontSize = 12.sp, color = Brand, fontWeight = FontWeight.Medium)
            }
        }
    }
}
