package com.rdbb.zhiyu.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rdbb.zhiyu.core.network.Community
import com.rdbb.zhiyu.core.ui.theme.CommunityGradients
import com.rdbb.zhiyu.core.ui.theme.Ink
import com.rdbb.zhiyu.core.util.firstChar

/** 圈子图标：有 icon URL 用图，否则渐变色块 + 名称首字。 */
@Composable
fun CommunityTile(
    community: Community?,
    size: Dp = 48.dp,
    cornerRadius: Dp = 16.dp,
    fallbackText: String? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val icon = community?.icon
    val base = modifier
        .size(size)
        .clip(RoundedCornerShape(cornerRadius))
        .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)

    if (!icon.isNullOrBlank() && (icon.startsWith("http") || icon.startsWith("/"))) {
        NetworkImage(url = icon, modifier = base, cornerRadius = cornerRadius)
    } else {
        val colors = CommunityGradients[
            (community?.id ?: 0).let { if (it < 0) -it else it } % CommunityGradients.size
        ]
        Box(
            modifier = base.background(Brush.linearGradient(colors)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = fallbackText ?: firstChar(community?.name),
                color = Ink,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.42f).sp
            )
        }
    }
}
