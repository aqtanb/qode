package com.qodein.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.theme.QodeCorners
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens

enum class QodeGradientStyle {
    /** Main brand gradient for primary screens */
    Primary,

    /** Secondary brand gradient for variety */
    Secondary,

    /** Accent gradient for CTAs and highlights */
    Accent,

    /** Subtle surface gradient for backgrounds */
    Surface,

    /** Custom colors for complete control */
    Custom
}

enum class QodeGradientDirection {
    Vertical,
    Horizontal,
    DiagonalTopLeft,
    DiagonalTopRight,
    Radial
}

@Composable
fun QodeGradientBackground(
    modifier: Modifier = Modifier,
    style: QodeGradientStyle = QodeGradientStyle.Primary,
    direction: QodeGradientDirection = QodeGradientDirection.Vertical,
    customColors: List<Color> = emptyList(),
    content: @Composable () -> Unit
) {
    val colors = when (style) {
        QodeGradientStyle.Primary -> listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.inversePrimary,
            MaterialTheme.colorScheme.primaryContainer,
        )

        QodeGradientStyle.Secondary -> listOf(
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.surfaceContainerHigh,
        )

        QodeGradientStyle.Accent -> listOf(
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.surfaceContainerHighest,
        )

        QodeGradientStyle.Surface -> listOf(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.surfaceContainer,
            MaterialTheme.colorScheme.surfaceContainerLowest,
        )

        QodeGradientStyle.Custom -> customColors.takeIf { it.isNotEmpty() } ?: listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer,
        )
    }

    val brush = when (direction) {
        QodeGradientDirection.Vertical -> Brush.verticalGradient(
            colors = colors,
            tileMode = TileMode.Clamp,
        )
        QodeGradientDirection.Horizontal -> Brush.horizontalGradient(
            colors = colors,
            tileMode = TileMode.Clamp,
        )
        QodeGradientDirection.DiagonalTopLeft -> Brush.linearGradient(
            colors = colors,
            tileMode = TileMode.Clamp,
        )
        QodeGradientDirection.DiagonalTopRight -> Brush.linearGradient(
            colors = colors,
            tileMode = TileMode.Clamp,
        )
        QodeGradientDirection.Radial -> Brush.radialGradient(
            colors = colors,
            tileMode = TileMode.Clamp,
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush),
    ) {
        content()
    }
}

// Convenience composables for common use cases
@Composable
fun QodePrimaryGradient(
    modifier: Modifier = Modifier,
    direction: QodeGradientDirection = QodeGradientDirection.Vertical,
    content: @Composable () -> Unit
) {
    QodeGradientBackground(
        modifier = modifier,
        style = QodeGradientStyle.Primary,
        direction = direction,
        content = content,
    )
}

@Composable
fun QodeSecondaryGradient(
    modifier: Modifier = Modifier,
    direction: QodeGradientDirection = QodeGradientDirection.Vertical,
    content: @Composable () -> Unit
) {
    QodeGradientBackground(
        modifier = modifier,
        style = QodeGradientStyle.Secondary,
        direction = direction,
        content = content,
    )
}

@Composable
fun QodeAccentGradient(
    modifier: Modifier = Modifier,
    direction: QodeGradientDirection = QodeGradientDirection.Vertical,
    content: @Composable () -> Unit
) {
    QodeGradientBackground(
        modifier = modifier,
        style = QodeGradientStyle.Accent,
        direction = direction,
        content = content,
    )
}

@Composable
fun QodeSurfaceGradient(
    modifier: Modifier = Modifier,
    direction: QodeGradientDirection = QodeGradientDirection.Vertical,
    content: @Composable () -> Unit
) {
    QodeGradientBackground(
        modifier = modifier,
        style = QodeGradientStyle.Surface,
        direction = direction,
        content = content,
    )
}

// MARK: - Previews

@Preview(name = "Primary Gradient", showBackground = true)
@Composable
private fun QodePrimaryGradientPreview() {
    QodeTheme {
        QodePrimaryGradient {
            PreviewContent(
                title = "Primary Gradient",
                subtitle = "Main brand gradient",
            )
        }
    }
}

@Preview(name = "Secondary Gradient", showBackground = true)
@Composable
private fun QodeSecondaryGradientPreview() {
    QodeTheme {
        QodeSecondaryGradient {
            PreviewContent(
                title = "Secondary Gradient",
                subtitle = "Alternative brand style",
            )
        }
    }
}

@Preview(name = "Accent Gradient", showBackground = true)
@Composable
private fun QodeAccentGradientPreview() {
    QodeTheme {
        QodeAccentGradient {
            PreviewContent(
                title = "Accent Gradient",
                subtitle = "For CTAs and highlights",
            )
        }
    }
}

@Preview(name = "Surface Gradient", showBackground = true)
@Composable
private fun QodeSurfaceGradientPreview() {
    QodeTheme {
        QodeSurfaceGradient {
            PreviewContent(
                title = "Surface Gradient",
                subtitle = "Subtle background gradient",
            )
        }
    }
}

@Preview(name = "Radial Gradient", showBackground = true)
@Composable
private fun QodeRadialGradientPreview() {
    QodeTheme {
        QodeGradientBackground(
            style = QodeGradientStyle.Primary,
            direction = QodeGradientDirection.Radial,
        ) {
            PreviewContent(
                title = "Radial Gradient",
                subtitle = "Centered effect",
            )
        }
    }
}

@Composable
private fun PreviewContent(
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier.padding(SpacingTokens.lg),
            shape = RoundedCornerShape(QodeCorners.lg),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            tonalElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier.padding(SpacingTokens.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
