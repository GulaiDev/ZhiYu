package com.rdbb.zhiyu.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rdbb.zhiyu.core.ui.theme.BrandBrush
import com.rdbb.zhiyu.core.ui.theme.CardWhite

/** 主按钮：柿子橙渐变 + 阴影胶囊。 */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(vertical = 14.dp)
) {
    val clickable = enabled && !loading
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (clickable) Brush.linearGradient(BrandBrush)
                else Brush.linearGradient(listOf(Color(0xFFFFB8A8), Color(0xFFFFA99B)))
            )
            .clickable(
                enabled = clickable,
                interactionSource = MutableInteractionSource(),
                indication = null,
                onClick = onClick
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = CardWhite,
                strokeWidth = 2.dp,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        } else {
            Text(
                text = text,
                color = CardWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
