package com.rdbb.zhiyu.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val ZhiYuTypography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 25.sp, lineHeight = 32.sp, color = Ink),
    headlineMedium = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 21.sp, lineHeight = 28.sp, color = Ink),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 24.sp, color = Ink),
    titleMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 23.sp, color = Ink),
    titleSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp, color = Ink),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 26.sp, color = Ink),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 22.sp, color = Ink),
    bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.5.sp, lineHeight = 18.sp, color = Ink2),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Ink),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, color = Ink2),
    labelSmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 11.sp, color = Ink3)
)
