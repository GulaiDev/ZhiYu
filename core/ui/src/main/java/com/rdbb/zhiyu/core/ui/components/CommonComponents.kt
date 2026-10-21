//package com.rdbb.zhiyu.core.ui.components
//
//import android.R.id.message
//import android.app.Activity
//import android.content.Context
//import android.content.ContextWrapper
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.interaction.MutableInteractionSource
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.SideEffect
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.platform.LocalView
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.core.view.WindowCompat
//import coil.compose.AsyncImage
//import coil.request.ImageRequest
//import com.rdbb.zhiyu.core.ui.theme.Amber
//import com.rdbb.zhiyu.core.ui.theme.AvatarGradients
//import com.rdbb.zhiyu.core.ui.theme.Brand
//import com.rdbb.zhiyu.core.ui.theme.BrandSoft
//import com.rdbb.zhiyu.core.ui.theme.Ink3
//import com.rdbb.zhiyu.core.ui.theme.Line
//
///** 网络图片，自带圆角裁剪与占位色。 */
//@Composable
//fun NetworkImage(
//    url: String?,
//    modifier: Modifier = Modifier,
//    contentDescription: String? = null,
//    cornerRadius: Dp = 10.dp
//) {
//    val context = LocalContext.current
//    AsyncImage(
//        model = ImageRequest.Builder(context)
//            .data(url?.absoluteUrl())
//            .crossfade(true)
//            .build(),
//        contentDescription = contentDescription,
//        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
//        modifier = modifier
//            .clip(RoundedCornerShape(cornerRadius))
//            .background(Line)
//    )
//}
//
///**
// * 头像：有 URL 用网络图，否则按 id 取渐变色块 + 昵称首字。
// */
//@Composable
//fun Avatar(
//    user: UserBrief?,
//    size: Dp = 42.dp,
//    modifier: Modifier = Modifier,
//    onClick: (() -> Unit)? = null
//) {
//    val colors = AvatarGradients[(user?.id ?: 0).let { if (it < 0) -it else it } % AvatarGradients.size]
//    val base = modifier
//        .size(size)
//        .clip(CircleShape)
//        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
//
//    if (!user?.avatar.isNullOrBlank()) {
//        AsyncImage(
//            model = user!!.avatar!!.absoluteUrl(),
//            contentDescription = user.nickname,
//            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
//            modifier = base.background(Line)
//        )
//    } else {
//        Box(
//            modifier = base.background(Brush.linearGradient(colors)),
//            contentAlignment = Alignment.Center
//        ) {
//            Text(
//                text = firstChar(user?.nickname ?: "知"),
//                color = Color.White,
//                fontWeight = FontWeight.Bold,
//                fontSize = (size.value * 0.4f).sp
//            )
//        }
//    }
//}
//
///** UserDetail 版本头像。 */
//@Composable
//fun Avatar(
//    user: UserDetail?,
//    size: Dp = 42.dp,
//    modifier: Modifier = Modifier,
//    onClick: (() -> Unit)? = null
//) {
//    val colors = AvatarGradients[(user?.id ?: 0).let { if (it < 0) -it else it } % AvatarGradients.size]
//    val base = modifier
//        .size(size)
//        .clip(CircleShape)
//        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
//
//    if (!user?.avatar.isNullOrBlank()) {
//        AsyncImage(
//            model = user!!.avatar!!.absoluteUrl(),
//            contentDescription = user.nickname,
//            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
//            modifier = base.background(Line)
//        )
//    } else {
//        Box(
//            modifier = base.background(Brush.linearGradient(colors)),
//            contentAlignment = Alignment.Center
//        ) {
//            Text(
//                text = firstChar(user?.nickname ?: "知"),
//                color = Color.White,
//                fontWeight = FontWeight.Bold,
//                fontSize = (size.value * 0.4f).sp
//            )
//        }
//    }
//}
//
///** 系统通知头像（知遇小助手）。 */
//@Composable
//fun SystemAvatar(size: Dp = 42.dp, modifier: Modifier = Modifier) {
//    Box(
//        modifier = modifier
//            .size(size)
//            .clip(CircleShape)
//            .background(Brush.linearGradient(listOf(Color(0xFFFFB347), Amber))),
//        contentAlignment = Alignment.Center
//    ) {
//        Text("知", color = Color.White, fontWeight = FontWeight.Bold, fontSize = (size.value * 0.4f).sp)
//    }
//}
//
///** 小标签（圈子/话题）。 */
//@Composable
//fun SoftTag(text: String, color: Color = Brand, background: Color = BrandSoft) {
//    Box(
//        modifier = Modifier
//            .clip(RoundedCornerShape(6.dp))
//            .background(background)
//            .padding(horizontal = 7.dp, vertical = 1.dp)
//    ) {
//        Text(text = text, color = color, fontSize = 11.sp, fontWeight = FontWeight.Medium)
//    }
//}
//
//@Composable
//fun LoadingBox(modifier: Modifier = Modifier.fillMaxSize()) {
//    Box(modifier, contentAlignment = Alignment.Center) {
//        CircularProgressIndicator(color = Brand, strokeWidth = 2.dp)
//    }
//}
//
//@Composable
//fun EmptyBox(text: String, modifier: Modifier = Modifier.fillMaxSize()) {
//    Box(modifier, contentAlignment = Alignment.Center) {
//        Text(text = text, color = Ink3, fontSize = 13.sp)
//    }
//}
//
//@Composable
//fun ErrorBox(message: String, onRetry: (() -> Unit)? = null, modifier: Modifier = Modifier.fillMaxSize()) {
//    val interactionSource = remember { MutableInteractionSource() }
//
//    Box(modifier, contentAlignment = Alignment.Center) {
//        Text(
//            text = message + if (onRetry != null) "\n点击重试" else "",
//            color = Ink3,
//            fontSize = 13.sp,
//            modifier = if (onRetry != null)
//                Modifier
//                    .clickable(
//                        interactionSource = interactionSource,
//                        indication = null,
//                        onClick = onRetry
//                    )
//                    .padding(24.dp)
//            else Modifier.padding(24.dp)
//        )
//    }
//}
//
///** 状态栏图标明暗：lightIcons=true 时为白色图标（用于深色顶图）。 */
//@Composable
//fun StatusBarIconStyle(lightIcons: Boolean) {
//    val view = LocalView.current
//    if (!view.isInEditMode) {
//        SideEffect {
//            val window = view.context.findActivity()?.window ?: return@SideEffect
//            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !lightIcons
//        }
//    }
//}
//
//private tailrec fun Context.findActivity(): Activity? = when (this) {
//    is Activity -> this
//    is ContextWrapper -> baseContext.findActivity()
//    else -> null
//}
