package com.qodein.core.designsystem.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.theme.AnimationTokens
import com.qodein.core.designsystem.theme.OpacityTokens
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SpacingTokens

/**
 * Modern dot-style page indicator with background container
 * Uses Material Design inverse surface colors for high contrast
 */
@Composable
fun PageIndicator(
    currentIndex: Int,
    totalPages: Int,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    inactiveColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = OpacityTokens.OVERLAY_DARK),
    activeColor: Color = MaterialTheme.colorScheme.onSurface
) {
    if (totalPages <= 1) return

    Box(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(ShapeTokens.Corner.full),
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        ) {
            repeat(totalPages) { index ->
                val isActive = index == currentIndex
                val animatedWidth by animateDpAsState(
                    targetValue = if (isActive) 16.dp else 4.dp,
                    animationSpec = AnimationTokens.Spec.medium(),
                    label = "page_indicator_dot_width",
                )

                Box(
                    modifier = Modifier
                        .width(animatedWidth)
                        .height(4.dp)
                        .clip(RoundedCornerShape(ShapeTokens.Corner.full))
                        .background(if (isActive) activeColor else inactiveColor),
                )
            }
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
            PageIndicator(
                currentIndex = 0,
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
            PageIndicator(
                currentIndex = 2,
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
            PageIndicator(
                currentIndex = 3,
                totalPages = 9,
            )
        }
    }
}
