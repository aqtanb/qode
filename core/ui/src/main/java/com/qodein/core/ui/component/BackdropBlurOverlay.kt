package com.qodein.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.theme.QodeTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeEffect

/**
 * Enterprise-level backdrop blur overlay for creating "liquid glass" effects
 * on content areas while maintaining text readability.
 *
 * Uses Haze library for hardware-accelerated blur with graceful fallbacks.
 * Designed for reuse across different UI components.
 *
 * @param hazeState The state controlling the blur effect
 * @param bottomAlpha Alpha value for bottom gradient overlay (0.0-1.0)
 * @param blurRadius Radius of the blur effect
 * @param unblurredAreaWeight Weight of the crystal clear middle area (0.0-1.0)
 * @param bottomBlurAreaWeight Weight of the bottom blurred area (0.0-1.0)
 * @param overlayAlpha Alpha value for subtle black overlay for text contrast (0.0-1.0)
 * @param modifier Modifier to be applied to the root component
 */
@Composable
fun BackdropBlurOverlay(
    hazeState: HazeState,
    bottomAlpha: Float = 0.8f,
    blurRadius: Dp = 8.dp,
    unblurredAreaWeight: Float,
    bottomBlurAreaWeight: Float,
    modifier: Modifier = Modifier
) {
    BlurOverlayContent(
        hazeState = hazeState,
        blurRadius = blurRadius,
        middleAreaWeight = unblurredAreaWeight,
        bottomAreaWeight = bottomBlurAreaWeight,
        modifier = modifier.fillMaxSize(),
    )
}

@Composable
private fun BlurOverlayContent(
    hazeState: HazeState,
    blurRadius: Dp,
    middleAreaWeight: Float,
    bottomAreaWeight: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Spacer(modifier = Modifier.weight(middleAreaWeight))

        // Bottom blurred section for content readability
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(bottomAreaWeight),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeEffect(
                        state = hazeState,
                        style = HazeStyle(
                            backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.05f),
                            tint = null,
                            blurRadius = blurRadius,
                        ),
                    ),
            )
        }
    }
}

/**
 * Convenience function for creating a HazeState for BackdropBlurOverlay
 */
@Composable
fun rememberBackdropBlurState(): HazeState = remember { HazeState() }

// MARK: - Previews

@Preview(name = "Backdrop Blur Overlay", showBackground = true, heightDp = 400)
@Composable
private fun BackdropBlurOverlayPreview() {
    QodeTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary),
        ) {
            val hazeState = rememberBackdropBlurState()

            // Simulate background content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .haze(hazeState)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary,
                                MaterialTheme.colorScheme.tertiary,
                            ),
                        ),
                    ),
            )

            // The blur overlay
            BackdropBlurOverlay(
                hazeState = hazeState,
                unblurredAreaWeight = 0.85f,
                bottomBlurAreaWeight = 0.15f,
            )
        }
    }
}
