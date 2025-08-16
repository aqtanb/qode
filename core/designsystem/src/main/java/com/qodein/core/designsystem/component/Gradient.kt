package com.qodein.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
 * Gradient patterns for different visual effects
 */
enum class QodeGradientPattern {
    /** Vertical fade - ProfileScreen style */
    Vertical,

    /** Horizontal left to right */
    Horizontal,

    /** Diagonal - banner style (0,0 to Infinite) */
    Diagonal,

    /** Radial - center outward */
    Radial,

    /** Diagonal reverse - bottom-left to top-right */
    DiagonalReverse
}

/**
 * Color schemes for gradients
 */
enum class QodeColorScheme {
    /** Material 3 primary container colors */
    ThemePrimary,

    /** Material 3 secondary colors */
    ThemeSecondary,

    /** Material 3 tertiary colors */
    ThemeTertiary,

    /** Banner: Indigo to Purple */
    BannerIndigo,

    /** Banner: Pink to Orange */
    BannerPink,

    /** Banner: Green shades */
    BannerGreen,

    /** Banner: Orange to Red */
    BannerOrange,

    /** Banner: Purple to Blue */
    BannerPurple,

    /** Banner: Cyan to Purple */
    BannerCyan
}

/**
 * Decoration styles for gradients
 */
enum class QodeDecorationStyle {
    /** No decorations */
    None,

    /** ProfileScreen floating circles */
    FloatingCircles
}

/**
 * Beautiful gradient component with separated concerns
 * Combines gradient patterns, color schemes, and decorative elements
 *
 * @param pattern The gradient pattern (direction/shape)
 * @param colorScheme The color scheme to use
 * @param decorations The decoration style
 * @param height Height of the gradient area
 * @param modifier Modifier to be applied to the component
 */
@Composable
fun QodeGradient(
    pattern: QodeGradientPattern = QodeGradientPattern.Vertical,
    colorScheme: QodeColorScheme = QodeColorScheme.ThemePrimary,
    decorations: QodeDecorationStyle = QodeDecorationStyle.None,
    height: Dp = SpacingTokens.xxxl * 5,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Gradient background
        GradientBackground(
            pattern = pattern,
            colorScheme = colorScheme,
            height = height,
        )

        // Decorations (if any)
        when (decorations) {
            QodeDecorationStyle.None -> { /* No decorations */ }
            QodeDecorationStyle.FloatingCircles -> {
                FloatingDecorations(
                    colorScheme = colorScheme,
                )
            }
        }
    }
}

/**
 * Gradient background with support for multiple patterns and color schemes
 */
@Composable
private fun GradientBackground(
    pattern: QodeGradientPattern,
    colorScheme: QodeColorScheme,
    height: Dp,
    modifier: Modifier = Modifier
) {
    val gradientColors = getGradientColors(colorScheme)
    val brush = getGradientBrush(pattern, gradientColors)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(brush),
    )
}

/**
 * Get colors for the specified color scheme
 */
@Composable
private fun getGradientColors(colorScheme: QodeColorScheme): List<Color> =
    when (colorScheme) {
        QodeColorScheme.ThemePrimary -> listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
            Color.Transparent,
        )
        QodeColorScheme.ThemeSecondary -> listOf(
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
            Color.Transparent,
        )
        QodeColorScheme.ThemeTertiary -> listOf(
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
            Color.Transparent,
        )
        QodeColorScheme.BannerIndigo -> listOf(
            Color(0xFF6366F1), // Indigo
            Color(0xFF8B5CF6), // Purple
        )
        QodeColorScheme.BannerPink -> listOf(
            Color(0xFFEC4899), // Pink
            Color(0xFFF97316), // Orange
        )
        QodeColorScheme.BannerGreen -> listOf(
            Color(0xFF10B981), // Emerald
            Color(0xFF059669), // Emerald dark
        )
        QodeColorScheme.BannerOrange -> listOf(
            Color(0xFFF59E0B), // Amber
            Color(0xFFEF4444), // Red
        )
        QodeColorScheme.BannerPurple -> listOf(
            Color(0xFF8B5CF6), // Purple
            Color(0xFF3B82F6), // Blue
        )
        QodeColorScheme.BannerCyan -> listOf(
            Color(0xFF06B6D4), // Cyan
            Color(0xFF8B5CF6), // Purple
        )
    }

/**
 * Get brush for the specified pattern
 */
private fun getGradientBrush(
    pattern: QodeGradientPattern,
    colors: List<Color>
): Brush =
    when (pattern) {
        QodeGradientPattern.Vertical -> Brush.verticalGradient(
            colors = colors,
            startY = 0f,
            endY = 800f,
        )
        QodeGradientPattern.Horizontal -> Brush.horizontalGradient(
            colors = colors,
            startX = 0f,
            endX = 1000f,
        )
        QodeGradientPattern.Diagonal -> Brush.linearGradient(
            colors = colors,
            start = Offset(0f, 0f),
            end = Offset.Infinite,
        )
        QodeGradientPattern.Radial -> Brush.radialGradient(
            colors = colors,
            radius = 800f,
        )
        QodeGradientPattern.DiagonalReverse -> Brush.linearGradient(
            colors = colors,
            start = Offset(0f, Float.POSITIVE_INFINITY),
            end = Offset(Float.POSITIVE_INFINITY, 0f),
        )
    }

/**
 * Responsive floating decoration circles that adapt to screen width
 * Uses BoxWithConstraints for reliable preview support
 */
@Composable
private fun FloatingDecorations(
    colorScheme: QodeColorScheme,
    modifier: Modifier = Modifier
) {
    val (primaryColor, secondaryColor, tertiaryColor) = when (colorScheme) {
        QodeColorScheme.ThemePrimary -> Triple(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
        )
        QodeColorScheme.ThemeSecondary -> Triple(
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f),
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
        )
        QodeColorScheme.ThemeTertiary -> Triple(
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.10f),
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
        )
        // Banner colors use subtle theme colors for decorations
        QodeColorScheme.BannerIndigo,
        QodeColorScheme.BannerPink,
        QodeColorScheme.BannerGreen,
        QodeColorScheme.BannerOrange,
        QodeColorScheme.BannerPurple,
        QodeColorScheme.BannerCyan -> Triple(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.06f),
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.10f),
        )
    }

    // Use BoxWithConstraints for reliable size detection in previews
    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
    ) {
        // Only render decorations when we have valid dimensions
        if (this.maxWidth > 0.dp && this.maxHeight > 0.dp) {
            // Left side bubbles (avoids 48dp back button area - starts at ~15%)
            ResponsiveFloatingCircle(
                size = SizeTokens.Decoration.sizeSmall,
                color = primaryColor,
                containerWidth = this.maxWidth,
                containerHeight = this.maxHeight,
                xFraction = 0.15f,
                yFraction = 0.25f,
            )

            ResponsiveFloatingCircle(
                size = SizeTokens.Decoration.sizeXSmall,
                color = tertiaryColor,
                containerWidth = this.maxWidth,
                containerHeight = this.maxHeight,
                xFraction = 0.08f,
                yFraction = 0.32f,
            )

            ResponsiveFloatingCircle(
                size = SizeTokens.Decoration.sizeMedium,
                color = secondaryColor,
                containerWidth = this.maxWidth,
                containerHeight = this.maxHeight,
                xFraction = 0.22f,
                yFraction = 0.18f,
            )

            // Center bubbles (around profile area but not interfering)
            ResponsiveFloatingCircle(
                size = SizeTokens.Decoration.sizeXSmall,
                color = tertiaryColor,
                containerWidth = this.maxWidth,
                containerHeight = this.maxHeight,
                xFraction = 0.35f,
                yFraction = 0.12f,
            )

            ResponsiveFloatingCircle(
                size = SizeTokens.Decoration.sizeSmall,
                color = primaryColor,
                containerWidth = this.maxWidth,
                containerHeight = this.maxHeight,
                xFraction = 0.40f,
                yFraction = 0.28f,
            )

            ResponsiveFloatingCircle(
                size = SizeTokens.Decoration.sizeLarge,
                color = secondaryColor,
                containerWidth = this.maxWidth,
                containerHeight = this.maxHeight,
                xFraction = 0.65f,
                yFraction = 0.15f,
            )

            // Right side bubbles (symmetric with left)
            ResponsiveFloatingCircle(
                size = SizeTokens.Decoration.sizeMedium,
                color = tertiaryColor,
                containerWidth = this.maxWidth,
                containerHeight = this.maxHeight,
                xFraction = 0.78f,
                yFraction = 0.22f,
            )

            ResponsiveFloatingCircle(
                size = SizeTokens.Decoration.sizeXLarge,
                color = primaryColor,
                containerWidth = this.maxWidth,
                containerHeight = this.maxHeight,
                xFraction = 0.85f,
                yFraction = 0.08f,
            )

            ResponsiveFloatingCircle(
                size = SizeTokens.Decoration.sizeSmall,
                color = secondaryColor,
                containerWidth = this.maxWidth,
                containerHeight = this.maxHeight,
                xFraction = 0.92f,
                yFraction = 0.18f,
            )

            ResponsiveFloatingCircle(
                size = SizeTokens.Decoration.sizeXSmall,
                color = tertiaryColor,
                containerWidth = this.maxWidth,
                containerHeight = this.maxHeight,
                xFraction = 0.88f,
                yFraction = 0.32f,
            )

            ResponsiveFloatingCircle(
                size = SizeTokens.Decoration.sizeMedium,
                color = primaryColor,
                containerWidth = this.maxWidth,
                containerHeight = this.maxHeight,
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
 * Hero gradient - exact ProfileScreen style with floating decorations
 */
@Composable
fun QodeHeroGradient(
    height: Dp = SpacingTokens.xxxl * 5,
    modifier: Modifier = Modifier
) {
    QodeGradient(
        pattern = QodeGradientPattern.Vertical,
        colorScheme = QodeColorScheme.ThemePrimary,
        decorations = QodeDecorationStyle.FloatingCircles,
        height = height,
        modifier = modifier,
    )
}

/**
 * Banner gradient - vibrant diagonal gradients for hero banners
 */
@Composable
fun QodeBannerGradient(
    colors: QodeColorScheme = QodeColorScheme.BannerIndigo,
    height: Dp = SpacingTokens.xxxl * 4,
    modifier: Modifier = Modifier
) {
    QodeGradient(
        pattern = QodeGradientPattern.Diagonal,
        colorScheme = colors,
        decorations = QodeDecorationStyle.None,
        height = height,
        modifier = modifier,
    )
}

/**
 * Primary theme gradient with decorations
 */
@Composable
fun QodePrimaryGradient(
    height: Dp = SpacingTokens.xxxl * 4,
    modifier: Modifier = Modifier
) {
    QodeGradient(
        pattern = QodeGradientPattern.Vertical,
        colorScheme = QodeColorScheme.ThemeSecondary,
        decorations = QodeDecorationStyle.FloatingCircles,
        height = height,
        modifier = modifier,
    )
}

/**
 * Secondary theme gradient with decorations
 */
@Composable
fun QodeSecondaryGradient(
    height: Dp = SpacingTokens.xxxl * 4,
    modifier: Modifier = Modifier
) {
    QodeGradient(
        pattern = QodeGradientPattern.Vertical,
        colorScheme = QodeColorScheme.ThemeSecondary,
        decorations = QodeDecorationStyle.FloatingCircles,
        height = height,
        modifier = modifier,
    )
}

/**
 * Tertiary theme gradient with decorations
 */
@Composable
fun QodeTertiaryGradient(
    height: Dp = SpacingTokens.xxxl * 4,
    modifier: Modifier = Modifier
) {
    QodeGradient(
        pattern = QodeGradientPattern.Vertical,
        colorScheme = QodeColorScheme.ThemeTertiary,
        decorations = QodeDecorationStyle.FloatingCircles,
        height = height,
        modifier = modifier,
    )
}

// MARK: - Enterprise Previews

@Preview(
    name = "Hero Gradient",
    showBackground = true,
    group = "QodeGradient - Convenience",
)
@Composable
private fun QodeHeroGradientPreview() {
    QodeTheme {
        QodeHeroGradient(height = 200.dp)
    }
}

@Preview(
    name = "Banner Gradients",
    showBackground = true,
    heightDp = 600,
    group = "QodeGradient - Banner",
)
@Composable
private fun QodeBannerGradientsPreview() {
    QodeTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            QodeBannerGradient(QodeColorScheme.BannerIndigo, height = 80.dp)
            QodeBannerGradient(QodeColorScheme.BannerPink, height = 80.dp)
            QodeBannerGradient(QodeColorScheme.BannerGreen, height = 80.dp)
            QodeBannerGradient(QodeColorScheme.BannerOrange, height = 80.dp)
            QodeBannerGradient(QodeColorScheme.BannerPurple, height = 80.dp)
            QodeBannerGradient(QodeColorScheme.BannerCyan, height = 80.dp)
        }
    }
}

@Preview(
    name = "Gradient Patterns",
    showBackground = true,
    heightDp = 600,
    group = "QodeGradient - Patterns",
)
@Composable
private fun GradientPatternsPreview() {
    QodeTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            QodeGradient(
                pattern = QodeGradientPattern.Vertical,
                colorScheme = QodeColorScheme.BannerIndigo,
                height = 60.dp,
            )
            QodeGradient(
                pattern = QodeGradientPattern.Horizontal,
                colorScheme = QodeColorScheme.BannerPink,
                height = 60.dp,
            )

            Text(
                "Diagonal",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            QodeGradient(
                pattern = QodeGradientPattern.Diagonal,
                colorScheme = QodeColorScheme.BannerGreen,
                height = 60.dp,
            )

            Text(
                "Radial",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            QodeGradient(
                pattern = QodeGradientPattern.Radial,
                colorScheme = QodeColorScheme.BannerOrange,
                height = 60.dp,
            )

            Text(
                "Diagonal Reverse",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            QodeGradient(
                pattern = QodeGradientPattern.DiagonalReverse,
                colorScheme = QodeColorScheme.BannerPurple,
                height = 60.dp,
            )
        }
    }
}

@Preview(
    name = "Decoration Styles",
    showBackground = true,
    heightDp = 400,
    group = "QodeGradient - Decorations",
)
@Composable
private fun DecorationStylesPreview() {
    QodeTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            QodeGradient(
                pattern = QodeGradientPattern.Vertical,
                colorScheme = QodeColorScheme.ThemePrimary,
                decorations = QodeDecorationStyle.None,
                height = 120.dp,
            )
            QodeGradient(
                pattern = QodeGradientPattern.Vertical,
                colorScheme = QodeColorScheme.ThemePrimary,
                decorations = QodeDecorationStyle.FloatingCircles,
                height = 120.dp,
            )
        }
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
