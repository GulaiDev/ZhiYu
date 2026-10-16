package com.rdbb.zhiyu.core.ui.components

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

/**
 * 屏幕宽度适配的 Density 提供者。
 *
 * 以设计稿宽度 [designWidthDp]（默认 375dp）为基准，根据当前设备屏幕的实际宽度 dp
 * 等比缩放 [LocalDensity]，使子组件中以 dp 标注的尺寸在不同屏幕宽度下呈现一致的
 * 视觉比例（类似今日头条屏幕适配方案）。需在界面根部包裹一次，内部所有 Composable
 * 经由 LocalDensity 读取到的均为缩放后的密度。
 *
 * @param designWidthDp 设计稿基准宽度（单位 dp），实际缩放比例 = 屏幕宽度 dp / 该值
 * @param followSystemFontScale 是否跟随系统字体缩放；
 *        false（默认）时强制 fontScale = 1f，避免系统大字造成布局错乱
 * @param content 被适配的子组件内容
 */

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun AppDensityProvider(
    designWidthDp: Float = 375f,
    followSystemFontScale: Boolean = false,
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val baseDensity = LocalDensity.current

    val screenWidthDp = configuration.screenWidthDp.toFloat()

    val scale = screenWidthDp / designWidthDp

    val density = baseDensity.density * scale

    val fontScale =
        if (followSystemFontScale) {
            baseDensity.fontScale
        } else {
            1f
        }

    CompositionLocalProvider(
        LocalDensity provides Density(
            density = density,
            fontScale = fontScale
        ),
        content = content
    )
}