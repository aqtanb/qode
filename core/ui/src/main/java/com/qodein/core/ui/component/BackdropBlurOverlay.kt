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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.theme.QodeTheme
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

/**
 * Enterprise-level backdrop blur overlay for creating "liquid glass" effects
 * on content areas while maintaining text readability.
 *
 * Uses Haze library for hardware-accelerated blur with graceful fallbacks.
 * Designed for reuse across different UI components.
 *
 * @param hazeState The state controlling the blur effect
 * @param topAlpha Alpha value for top gradient overlay (0.0-1.0)
 * @param bottomAlpha Alpha value for bottom gradient overlay (0.0-1.0)
 * @param blurRadius Radius of the blur effect
 * @param topAreaWeight Weight of the top blurred area (0.0-1.0)
 * @param middleAreaWeight Weight of the crystal clear middle area (0.0-1.0)
 * @param bottomAreaWeight Weight of the bottom blurred area (0.0-1.0)
 * @param overlayAlpha Alpha value for subtle black overlay for text contrast (0.0-1.0)
 * @param modifier Modifier to be applied to the root component
 */
@Composable
fun BackdropBlurOverlay(
    hazeState: HazeState,
    topAlpha: Float = 0.4f,
    bottomAlpha: Float = 0.8f,
    blurRadius: Dp = 8.dp,
    topAreaWeight: Float = 0.1f,
    middleAreaWeight: Float = 0.75f,
    bottomAreaWeight: Float = 0.15f,
    overlayAlpha: Float = 0.7f,
    modifier: Modifier = Modifier
) {
    BlurOverlayContent(
        hazeState = hazeState,
        topAlpha = topAlpha,
        bottomAlpha = bottomAlpha,
        blurRadius = blurRadius,
        topAreaWeight = topAreaWeight,
        middleAreaWeight = middleAreaWeight,
        bottomAreaWeight = bottomAreaWeight,
        overlayAlpha = overlayAlpha,
        modifier = modifier.fillMaxSize(),
    )
}

@Composable
private fun BlurOverlayContent(
    hazeState: HazeState,
    topAlpha: Float,
    bottomAlpha: Float,
    blurRadius: Dp,
    topAreaWeight: Float,
    middleAreaWeight: Float,
    bottomAreaWeight: Float,
    overlayAlpha: Float = 0f,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Top blurred section for content readability
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(topAreaWeight),
        ) {
            // Subtle black overlay for text contrast
            if (overlayAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = overlayAlpha)),
                )
            }

            // Blur overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeChild(
                        state = hazeState,
                        style = HazeStyle(
                            backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.05f),
                            tint = null,
                            blurRadius = blurRadius,
                        ),
                    ),
            )
        }

        // Middle area - crystal clear content visibility
        Spacer(modifier = Modifier.weight(middleAreaWeight))

        // Bottom blurred section for content readability
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(bottomAreaWeight),
        ) {
            // Subtle black overlay for text contrast
            if (overlayAlpha > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = overlayAlpha)),
                )
            }

            // Blur overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeChild(
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
            BackdropBlurOverlay(hazeState = hazeState)
        }
    }
}
