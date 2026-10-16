package com.rdbb.zhiyu.core.ui.theme

import androidx.compose.ui.graphics.Color

// 设计令牌（与 docs/ui-design.html 一一对应）
val Brand = Color(0xFFFF5A47)        // 柿子橙 主色
val BrandDark = Color(0xFFE8442F)
val BrandSoft = Color(0xFFFFF0EC)    // 主色浅底
val Ink = Color(0xFF20232B)          // 墨色 主文字
val Ink2 = Color(0xFF5E5B52)         // 次要文字
val Ink3 = Color(0xFFA39E92)         // 辅助文字
val Paper = Color(0xFFF7F4EE)        // 暖米白 页面底
val CardWhite = Color(0xFFFFFFFF)
val Line = Color(0xFFEDE8DF)         // 分割线
val SuccessGreen = Color(0xFF2E9E7A)
val LinkBlue = Color(0xFF3D7EFF)
val Amber = Color(0xFFF5A623)

// 头像/圈子渐变
val AvatarGradients = listOf(
    listOf(Color(0xFFFF8A65), Color(0xFFF4502C)),
    listOf(Color(0xFF7C9CFF), Color(0xFF4A63E8)),
    listOf(Color(0xFF4ECCA3), Color(0xFF1E8F6E)),
    listOf(Color(0xFFFF9F6B), Color(0xFFF5702B)),
    listOf(Color(0xFFB094FF), Color(0xFF7A52E0)),
    listOf(Color(0xFF5FC8E8), Color(0xFF2E86C9)),
    listOf(Color(0xFFF58FB0), Color(0xFFE05687))
)

// 圈子图标底色
val CommunityGradients = listOf(
    listOf(Color(0xFFFFE3C7), Color(0xFFFFC38F)),
    listOf(Color(0xFFDDE6FF), Color(0xFFAEBFFF)),
    listOf(Color(0xFFD9F5E8), Color(0xFFA4E2C9)),
    listOf(Color(0xFFFFE0DA), Color(0xFFFFB4A6)),
    listOf(Color(0xFFECE2FF), Color(0xFFCBB3FA))
)

val BrandBrush get() = listOf(Color(0xFFFF7050), Color(0xFFF5462E))
val PublishBrush get() = listOf(Color(0xFFFF7A5C), Color(0xFFFF4B33))
