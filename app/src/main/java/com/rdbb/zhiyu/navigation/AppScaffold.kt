package com.rdbb.zhiyu.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import com.rdbb.zhiyu.core.ui.theme.Brand
import com.rdbb.zhiyu.core.ui.theme.CardWhite
import com.rdbb.zhiyu.core.ui.theme.Ink3
import com.rdbb.zhiyu.core.ui.theme.Line
import com.rdbb.zhiyu.core.ui.theme.PublishBrush

/**
 * 底部导航：首页 / 圈子 / [发布] / 消息 / 我的。
 */
@Composable
fun AppBottomBar(
    currentRoute: String?,
    unreadCount: Int,
    onTab: (TabRoute) -> Unit,
    onPublish: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(CardWhite)
            .border(width = 0.8.dp, color = Line)
            .navigationBarsPadding()
            .height(62.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TabItem(TabRoute.HOME, currentRoute == Routes.HOME) { onTab(TabRoute.HOME) }
        TabItem(TabRoute.COMMUNITY, currentRoute == Routes.COMMUNITIES) { onTab(TabRoute.COMMUNITY) }

        // 中间发布按钮
        Box(
            modifier = Modifier
                .weight(1f)
                .clickable(
                    interactionSource = MutableInteractionSource(),
                    indication = null,
                    onClick = onPublish
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(30.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.linearGradient(PublishBrush)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "发布",
                    tint = CardWhite,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        TabItem(
            TabRoute.NOTIFICATION,
            currentRoute == Routes.NOTIFICATIONS,
            badge = unreadCount
        ) { onTab(TabRoute.NOTIFICATION) }
        TabItem(TabRoute.MINE, currentRoute == Routes.MINE) { onTab(TabRoute.MINE) }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.TabItem(
    tab: TabRoute,
    selected: Boolean,
    badge: Int = 0,
    onClick: () -> Unit
) {
    val tint = if (selected) Brand else Ink3
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(
                interactionSource = MutableInteractionSource(),
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box {
            Icon(
                tab.icon,
                contentDescription = tab.label,
                tint = tint,
                modifier = Modifier
                    .size(23.dp)
                    .padding(bottom = 0.dp)
            )
            if (badge > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-4).dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Brand)
                        .border(1.5.dp, CardWhite, RoundedCornerShape(9.dp))
                        .padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = if (badge > 99) "99+" else badge.toString(),
                        color = CardWhite,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Spacer(Modifier.height(3.dp))
        Text(
            tab.label,
            fontSize = 10.5.sp,
            color = tint,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
