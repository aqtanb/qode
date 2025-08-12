package com.qodein.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens

/**
 * Gradient styles for Qode design system
 * Based on the beautiful ProfileScreen gradient + floating decorations pattern
 */
enum class QodeGradientStyle {
    /** ProfileScreen hero style - primary container with decorations */
    Hero,

    /** Primary color gradient with decorations */
    Primary,

    /** Secondary color gradient with decorations */
    Secondary,

    /** Tertiary color gradient with decorations */
    Tertiary
}

/**
 * Beautiful gradient component inspired by ProfileScreen
 * Combines smooth vertical gradient with strategically placed floating decorations
 *
 * @param style The gradient style to use
 * @param height Height of the gradient area
 * @param modifier Modifier to be applied to the component
 */
@Composable
fun QodeGradient(
    style: QodeGradientStyle = QodeGradientStyle.Hero,
    height: Dp = SpacingTokens.xxxl * 5,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Gradient background
        GradientBackground(
            style = style,
            height = height,
        )

        // Floating decorations
        FloatingDecorations(
            style = style,
        )
    }
}

/**
 * Just the gradient part - ProfileScreen inspired vertical fade
 */
@Composable
private fun GradientBackground(
    style: QodeGradientStyle,
    height: Dp,
    modifier: Modifier = Modifier
) {
    val gradientColors = when (style) {
        QodeGradientStyle.Hero -> listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
            Color.Transparent,
        )
        QodeGradientStyle.Primary -> listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            Color.Transparent,
        )
        QodeGradientStyle.Secondary -> listOf(
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
            Color.Transparent,
        )
        QodeGradientStyle.Tertiary -> listOf(
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
            Color.Transparent,
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(
                Brush.verticalGradient(
                    colors = gradientColors,
                    startY = 0f,
                    endY = 800f,
                ),
            ),
    )
}

/**
 * Responsive floating decoration circles that adapt to screen width
 * Uses BoxWithConstraints for reliable preview support
 */
@Composable
private fun FloatingDecorations(
    style: QodeGradientStyle,
    modifier: Modifier = Modifier
) {
    val (primaryColor, secondaryColor, tertiaryColor) = when (style) {
        QodeGradientStyle.Hero -> Triple(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
        )
        QodeGradientStyle.Primary -> Triple(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        )
        QodeGradientStyle.Secondary -> Triple(
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f),
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
        )
        QodeGradientStyle.Tertiary -> Triple(
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.10f),
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
        )
    }

    // Use BoxWithConstraints for reliable size detection in previews
    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
    ) {
        // Only render decorations when we have valid dimensions
        if (maxWidth > 0.dp && maxHeight > 0.dp) {
            // Left side bubbles (avoids 48dp back button area - starts at ~15%)
            ResponsiveFloatingCircle(
                size = SizeTokens.Decoration.sizeSmall,
                color = primaryColor,
                containerWidth = maxWidth,
                containerHeight = maxHeight,
                xFraction = 0.15f,
                yFraction = 0.25f,
            )

            ResponsiveFloatingCircle(
                size = SizeTokens.Decoration.sizeXSmall,
                color = tertiaryColor,
                containerWidth = maxWidth,
                containerHeight = maxHeight,
                xFraction = 0.08f,
                yFraction = 0.32f,
            )

            ResponsiveFloatingCircle(
                size = SizeTokens.Decoration.sizeMedium,
                color = secondaryColor,
                containerWidth = maxWidth,
                containerHeight = maxHeight,
                xFraction = 0.22f,
                yFraction = 0.18f,
            )

            // Center bubbles (around profile area but not interfering)
            ResponsiveFloatingCircle(
                size = SizeTokens.Decoration.sizeXSmall,
                color = tertiaryColor,
                containerWidth = maxWidth,
                containerHeight = maxHeight,
                xFraction = 0.35f,
                yFraction = 0.12f,
            )

            ResponsiveFloatingCircle(
                size = SizeTokens.Decoration.sizeSmall,
                color = primaryColor,
                containerWidth = maxWidth,
                containerHeight = maxHeight,
                xFraction = 0.40f,
                yFraction = 0.28f,
            )

            ResponsiveFloatingCircle(
                size = SizeTokens.Decoration.sizeLarge,
                color = secondaryColor,
                containerWidth = maxWidth,
                containerHeight = maxHeight,
                xFraction = 0.65f,
                yFraction = 0.15f,
            )

            // Right side bubbles (symmetric with left)
            ResponsiveFloatingCircle(
                size = SizeTokens.Decoration.sizeMedium,
                color = tertiaryColor,
                containerWidth = maxWidth,
                containerHeight = maxHeight,
                xFraction = 0.78f,
                yFraction = 0.22f,
            )

            ResponsiveFloatingCircle(
                size = SizeTokens.Decoration.sizeXLarge,
                color = primaryColor,
                containerWidth = maxWidth,
                containerHeight = maxHeight,
                xFraction = 0.85f,
                yFraction = 0.08f,
            )

            ResponsiveFloatingCircle(
                size = SizeTokens.Decoration.sizeSmall,
                color = secondaryColor,
                containerWidth = maxWidth,
                containerHeight = maxHeight,
                xFraction = 0.92f,
                yFraction = 0.18f,
            )

            ResponsiveFloatingCircle(
                size = SizeTokens.Decoration.sizeXSmall,
                color = tertiaryColor,
                containerWidth = maxWidth,
                containerHeight = maxHeight,
                xFraction = 0.88f,
                yFraction = 0.32f,
            )

            ResponsiveFloatingCircle(
                size = SizeTokens.Decoration.sizeMedium,
                color = primaryColor,
                containerWidth = maxWidth,
                containerHeight = maxHeight,
                xFraction = 0.95f,
                yFraction = 0.25f,
            )
        }
    }
}

/**
 * Individual floating circle decoration with responsive positioning
 */
@Composable
private fun ResponsiveFloatingCircle(
    size: Dp,
    color: Color,
    containerWidth: Dp,
    containerHeight: Dp,
    xFraction: Float,
    yFraction: Float,
    modifier: Modifier = Modifier
) {
    // Ensure fractions are within valid range
    val safeXFraction = xFraction.coerceIn(0f, 1f)
    val safeYFraction = yFraction.coerceIn(0f, 1f)

    // Calculate positions with bounds checking to prevent negative offsets
    val halfSize = size / 2
    val xPosition = ((containerWidth * safeXFraction) - halfSize).coerceAtLeast(0.dp)
    val yPosition = ((containerHeight * safeYFraction) - halfSize).coerceAtLeast(0.dp)

    Box(
        modifier = modifier
            .size(size)
            .offset(x = xPosition, y = yPosition)
            .background(color, CircleShape),
    )
}

/**
 * Individual floating circle decoration (legacy hardcoded version)
 */
@Composable
private fun FloatingCircle(
    size: Dp,
    color: Color,
    offset: Offset,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .offset(x = offset.x.dp, y = offset.y.dp)
            .background(color, CircleShape),
    )
}

// MARK: - Convenience Functions

/**
 * Hero gradient - exact ProfileScreen style
 */
@Composable
fun QodeHeroGradient(
    height: Dp = SpacingTokens.xxxl * 5,
    modifier: Modifier = Modifier
) {
    QodeGradient(
        style = QodeGradientStyle.Hero,
        height = height,
        modifier = modifier,
    )
}

/**
 * Primary gradient with decorations
 */
@Composable
fun QodePrimaryGradient(
    height: Dp = SpacingTokens.xxxl * 4,
    modifier: Modifier = Modifier
) {
    QodeGradient(
        style = QodeGradientStyle.Primary,
        height = height,
        modifier = modifier,
    )
}

/**
 * Secondary gradient with decorations
 */
@Composable
fun QodeSecondaryGradient(
    height: Dp = SpacingTokens.xxxl * 4,
    modifier: Modifier = Modifier
) {
    QodeGradient(
        style = QodeGradientStyle.Secondary,
        height = height,
        modifier = modifier,
    )
}

/**
 * Tertiary gradient with decorations
 */
@Composable
fun QodeTertiaryGradient(
    height: Dp = SpacingTokens.xxxl * 4,
    modifier: Modifier = Modifier
) {
    QodeGradient(
        style = QodeGradientStyle.Tertiary,
        height = height,
        modifier = modifier,
    )
}

// MARK: - Enterprise Previews

@Preview(
    name = "Hero Gradient",
    showBackground = true,
    group = "QodeGradient",
)
@Composable
private fun QodeHeroGradientPreview() {
    QodeTheme {
        QodeHeroGradient()
    }
}

@Preview(
    name = "Primary Gradient",
    showBackground = true,
    group = "QodeGradient",
)
@Composable
private fun QodePrimaryGradientPreview() {
    QodeTheme {
        QodePrimaryGradient()
    }
}

@Preview(
    name = "Secondary Gradient",
    showBackground = true,
    group = "QodeGradient",
)
@Composable
private fun QodeSecondaryGradientPreview() {
    QodeTheme {
        QodeSecondaryGradient()
    }
}

@Preview(
    name = "Tertiary Gradient",
    showBackground = true,
    group = "QodeGradient",
)
@Composable
private fun QodeTertiaryGradientPreview() {
    QodeTheme {
        QodeTertiaryGradient()
    }
}

@Preview(
    name = "All Gradient Styles",
    showBackground = true,
    heightDp = 1200,
    group = "QodeGradient",
)
@Composable
private fun AllGradientStylesPreview() {
    QodeTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            // Show all gradient styles stacked
            Box(modifier = Modifier.offset(y = 0.dp)) {
                QodeHeroGradient(height = 200.dp)
            }
            Box(modifier = Modifier.offset(y = 220.dp)) {
                QodePrimaryGradient(height = 200.dp)
            }
            Box(modifier = Modifier.offset(y = 440.dp)) {
                QodeSecondaryGradient(height = 200.dp)
            }
            Box(modifier = Modifier.offset(y = 660.dp)) {
                QodeTertiaryGradient(height = 200.dp)
            }
        }
    }
}

@Preview(
    name = "Dark Theme Gradients",
    showBackground = true,
    heightDp = 800,
    group = "QodeGradient Themes",
)
@Composable
private fun DarkThemeGradientsPreview() {
    QodeTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            QodeHeroGradient()
        }
    }
}

@Preview(
    name = "Responsive Decorations - Phone",
    showBackground = true,
    widthDp = 360,
    heightDp = 640,
    group = "QodeGradient Responsive",
)
@Composable
private fun ResponsiveDecorationsPhonePreview() {
    QodeTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            QodeHeroGradient()
        }
    }
}

@Preview(
    name = "Responsive Decorations - Tablet",
    showBackground = true,
    widthDp = 840,
    heightDp = 600,
    group = "QodeGradient Responsive",
)
@Composable
private fun ResponsiveDecorationsTabletPreview() {
    QodeTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            QodeHeroGradient()
        }
    }
}
