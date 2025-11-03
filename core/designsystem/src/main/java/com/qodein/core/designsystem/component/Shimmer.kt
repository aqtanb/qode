package com.qodein.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens

/**
 * Shimmer effect for skeleton loading states.
 *
 * Provides a smooth animated gradient that simulates content loading.
 */
@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(4.dp)
) {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer",
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim, translateAnim),
        end = Offset(translateAnim + 200f, translateAnim + 200f),
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(brush),
    )
}

/**
 * Common shimmer shapes for skeleton loading
 */
@Composable
fun ShimmerBox(
    width: Dp,
    height: Dp,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(4.dp)
) {
    ShimmerEffect(
        modifier = modifier.size(width = width, height = height),
        shape = shape,
    )
}

@Composable
fun ShimmerLine(
    width: Dp = 200.dp,
    height: Dp = 16.dp,
    modifier: Modifier = Modifier
) {
    ShimmerEffect(
        modifier = modifier.size(width = width, height = height),
        shape = RoundedCornerShape(4.dp),
    )
}

@Composable
fun ShimmerCircle(
    size: Dp,
    modifier: Modifier = Modifier
) {
    ShimmerEffect(
        modifier = modifier.size(size),
        shape = CircleShape,
    )
}

@ThemePreviews
@Composable
private fun ShimmerEffectPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(SpacingTokens.md),
        ) {
            ShimmerLine(width = 200.dp, height = 20.dp)
            Spacer(modifier = Modifier.height(SpacingTokens.sm))
            ShimmerLine(width = 150.dp, height = 16.dp)
            Spacer(modifier = Modifier.height(SpacingTokens.sm))
            ShimmerBox(width = 300.dp, height = 200.dp, shape = RoundedCornerShape(12.dp))
            Spacer(modifier = Modifier.height(SpacingTokens.sm))
            ShimmerCircle(size = 48.dp)
        }
    }
}
