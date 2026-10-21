package com.rdbb.zhiyu.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ZhiYuColorScheme = lightColorScheme(
    primary = Brand,
    onPrimary = CardWhite,
    primaryContainer = BrandSoft,
    onPrimaryContainer = BrandDark,
    secondary = SuccessGreen,
    onSecondary = CardWhite,
    tertiary = LinkBlue,
    background = Paper,
    onBackground = Ink,
    surface = CardWhite,
    onSurface = Ink,
    surfaceVariant = Paper,
    onSurfaceVariant = Ink2,
    outline = Line,
    outlineVariant = Line,
    error = Brand
)

@Composable
fun ZhiYuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // 产品为暖色浅色设计，不跟随系统深色模式
    MaterialTheme(
        colorScheme = ZhiYuColorScheme,
        typography = ZhiYuTypography,
        content = content
    )
}