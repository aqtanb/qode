package com.qodein.core.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.R
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens

enum class QodeLogoSize {
    Small, // 32dp - for small UI elements
    Medium, // 48dp - for headers, cards
    Large, // 64dp - for hero sections
    XLarge // 96dp - for splash, onboarding
}

enum class QodeLogoStyle {
    Default, // Full color vector logo
    Monochrome, // Single color version
    Inverse // For dark backgrounds
}

@Composable
fun QodeLogo(
    modifier: Modifier = Modifier,
    size: QodeLogoSize = QodeLogoSize.Medium,
    style: QodeLogoStyle = QodeLogoStyle.Default,
    backgroundColor: Color? = null,
    contentDescription: String? = "Qode logo"
) {
    val logoSize = when (size) {
        QodeLogoSize.Small -> SizeTokens.Icon.sizeLarge // 32dp
        QodeLogoSize.Medium -> 48.dp // 48dp
        QodeLogoSize.Large -> 64.dp // 64dp
        QodeLogoSize.XLarge -> 96.dp // 96dp
    }

    val cornerRadius = when (size) {
        QodeLogoSize.Small -> ShapeTokens.Corner.small
        QodeLogoSize.Medium -> ShapeTokens.Corner.medium
        QodeLogoSize.Large -> ShapeTokens.Corner.large
        QodeLogoSize.XLarge -> ShapeTokens.Corner.extraLarge
    }

    val (logoRes, colorFilter, backgroundModifier) = when (style) {
        QodeLogoStyle.Default -> Triple(
            R.drawable.ic_qode, // Your vector logo - crisp at any size!
            null, // No tint - keep original orange colors
            backgroundColor?.let {
                Modifier.background(it, RoundedCornerShape(cornerRadius))
            } ?: Modifier, // No background needed for vectors - they're transparent!
        )

        QodeLogoStyle.Monochrome -> Triple(
            R.drawable.ic_qode, // Same vector but tinted
            ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
            backgroundColor?.let {
                Modifier.background(it, RoundedCornerShape(cornerRadius))
            } ?: Modifier.background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(cornerRadius),
            ),
        )

        QodeLogoStyle.Inverse -> Triple(
            R.drawable.ic_qode, // Vector tinted for dark backgrounds
            ColorFilter.tint(MaterialTheme.colorScheme.surface),
            backgroundColor?.let {
                Modifier.background(it, RoundedCornerShape(cornerRadius))
            } ?: Modifier.background(
                MaterialTheme.colorScheme.onSurface,
                RoundedCornerShape(cornerRadius),
            ),
        )
    }

    Box(
        modifier = modifier
            .size(logoSize)
            .clip(RoundedCornerShape(cornerRadius))
            .then(backgroundModifier),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = logoRes),
            contentDescription = contentDescription,
            colorFilter = colorFilter,
            modifier = Modifier.size(logoSize), // Full size for vectors - they scale perfectly!
        )
    }
}

// Convenience composables for common use cases
@Composable
fun QodeAppIcon(
    modifier: Modifier = Modifier,
    size: QodeLogoSize = QodeLogoSize.Medium
) {
    QodeLogo(
        modifier = modifier,
        size = size,
        style = QodeLogoStyle.Default,
    )
}

@Composable
fun QodeLogoMinimal(
    modifier: Modifier = Modifier,
    size: QodeLogoSize = QodeLogoSize.Small,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    val logoSize = when (size) {
        QodeLogoSize.Small -> SizeTokens.Icon.sizeLarge
        QodeLogoSize.Medium -> 48.dp
        QodeLogoSize.Large -> 64.dp
        QodeLogoSize.XLarge -> 96.dp
    }

    Image(
        painter = painterResource(id = R.drawable.ic_qode),
        contentDescription = "Qode",
        colorFilter = ColorFilter.tint(tint),
        modifier = modifier.size(logoSize),
    )
}

// MARK: - Previews

@Preview(name = "Default Logos", showBackground = true)
@Composable
private fun QodeLogoDefaultPreview() {
    QodeTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            QodeLogo(size = QodeLogoSize.Small)
            QodeLogo(size = QodeLogoSize.Medium)
            QodeLogo(size = QodeLogoSize.Large)
            QodeLogo(size = QodeLogoSize.XLarge)
        }
    }
}

@Preview(name = "Logo Styles", showBackground = true)
@Composable
private fun QodeLogoStylesPreview() {
    QodeTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            QodeLogo(style = QodeLogoStyle.Default)
            QodeLogo(style = QodeLogoStyle.Monochrome)
            QodeLogo(style = QodeLogoStyle.Inverse)
        }
    }
}

@Preview(name = "App Icon", showBackground = true)
@Composable
private fun QodeAppIconPreview() {
    QodeTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            // Like in your auth screen - CRISP at any size!
            QodeAppIcon(size = QodeLogoSize.XLarge)

            // Minimal versions
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                QodeLogoMinimal(size = QodeLogoSize.Small)
                QodeLogoMinimal(size = QodeLogoSize.Medium)
                QodeLogoMinimal(
                    size = QodeLogoSize.Large,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Preview(name = "Dark Theme", showBackground = true)
@Composable
private fun QodeLogoDarkPreview() {
    QodeTheme(darkTheme = true) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            QodeLogo(style = QodeLogoStyle.Default)
            QodeLogo(style = QodeLogoStyle.Monochrome)
            QodeLogo(style = QodeLogoStyle.Inverse)
        }
    }
}
