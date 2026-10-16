package com.rdbb.zhiyu.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

/** 底部导航 Tab（中间凸起的发布按钮单独处理，不在此列）。 */
enum class TabRoute(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    HOME(Routes.HOME, "首页", Icons.Outlined.Home),
    COMMUNITY(Routes.COMMUNITIES, "圈子", Icons.Outlined.Groups),
    NOTIFICATION(Routes.NOTIFICATIONS, "消息", Icons.Outlined.Notifications),
    MINE(Routes.MINE, "我的", Icons.Outlined.Person);

    companion object {
        fun fromRoute(route: String?): TabRoute? = entries.firstOrNull { it.route == route }
    }
}
