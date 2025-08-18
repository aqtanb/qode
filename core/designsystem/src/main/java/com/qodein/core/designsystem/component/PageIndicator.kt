package com.qodein.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SpacingTokens

/**
 * Modern dot-style page indicator for 2024 design trends
 * Works perfectly with bi-directional swiping
 */
@Composable
fun ModernPageIndicator(
    currentPage: Int,
    totalPages: Int,
    modifier: Modifier = Modifier,
    inactiveColor: Color = Color.White.copy(alpha = 0.3f),
    activeColor: Color = Color.White,
    dotSize: Dp = 6.dp,
    activeDotWidth: Dp = 20.dp,
    spacing: Dp = 6.dp
) {
    if (totalPages <= 1) return

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
        repeat(totalPages) { index ->
            val isActive = index == currentPage
            val animatedWidth by animateFloatAsState(
                targetValue = if (isActive) activeDotWidth.value else dotSize.value,
                animationSpec = tween(durationMillis = 300),
                label = "dot_width",
            )

            Box(
                modifier = Modifier
                    .width(animatedWidth.dp)
                    .height(dotSize)
                    .clip(RoundedCornerShape(ShapeTokens.Corner.full))
                    .background(if (isActive) activeColor else inactiveColor),
            )
        }
    }
}

// MARK: - Preview Functions

@Preview(name = "Modern Page Indicator - Beginning", showBackground = true)
@Composable
private fun ModernPageIndicatorBeginningPreview() {
    QodeTheme {
        Box(
            modifier = Modifier
                .background(Color.Gray)
                .padding(SpacingTokens.lg),
        ) {
            ModernPageIndicator(
                currentPage = 0,
                totalPages = 4,
            )
        }
    }
}

@Preview(name = "Modern Page Indicator - Middle", showBackground = true)
@Composable
private fun ModernPageIndicatorMiddlePreview() {
    QodeTheme {
        Box(
            modifier = Modifier
                .background(Color.Gray)
                .padding(SpacingTokens.lg),
        ) {
            ModernPageIndicator(
                currentPage = 2,
                totalPages = 4,
            )
        }
    }
}

@Preview(name = "Modern Page Indicator - End", showBackground = true)
@Composable
private fun ModernPageIndicatorEndPreview() {
    QodeTheme {
        Box(
            modifier = Modifier
                .background(Color.Gray)
                .padding(SpacingTokens.lg),
        ) {
            ModernPageIndicator(
                currentPage = 3,
                totalPages = 4,
            )
        }
    }
}
