package com.qodein.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens

/**
 * Reusable circular image component with smart fallback: Image → Initials → Icon
 * Can be used for user profiles, service logos, or any circular image with fallbacks
 */
@Composable
fun CircularImage(
    fallbackIcon: ImageVector,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    fallbackText: String? = null,
    size: Dp = SizeTokens.Avatar.sizeLarge,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    contentDescription: String? = null
) {
    val sizedModifier = modifier.size(size)

    if (imageUrl != null) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .allowHardware(false)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build(),
            contentDescription = contentDescription,
            modifier = sizedModifier
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            error = {
                CircularImageFallback(
                    fallbackText = fallbackText,
                    fallbackIcon = fallbackIcon,
                    size = size,
                    backgroundColor = backgroundColor,
                    contentColor = contentColor,
                    contentDescription = contentDescription,
                )
            },
            loading = {
                CircularImageLoading(
                    size = size,
                    backgroundColor = backgroundColor,
                )
            },
        )
    } else {
        CircularImageFallback(
            fallbackText = fallbackText,
            fallbackIcon = fallbackIcon,
            size = size,
            backgroundColor = backgroundColor,
            contentColor = contentColor,
            contentDescription = contentDescription,
            modifier = sizedModifier,
        )
    }
}

@Composable
private fun CircularImageFallback(
    fallbackText: String?,
    fallbackIcon: ImageVector,
    size: Dp,
    backgroundColor: Color,
    contentColor: Color,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(size).clip(CircleShape),
        color = backgroundColor,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            val displayInitials = fallbackText?.takeIf { it.isNotBlank() }?.take(2)?.uppercase()
            val fontSize = with(LocalDensity.current) { (size * 0.5f).toSp() }

            if (displayInitials != null) {
                Text(
                    text = displayInitials,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    fontSize = fontSize,
                    lineHeight = fontSize,
                    textAlign = TextAlign.Center,
                )
            } else {
                Icon(
                    imageVector = fallbackIcon,
                    contentDescription = contentDescription,
                    tint = contentColor,
                )
            }
        }
    }
}

@Composable
private fun CircularImageLoading(
    size: Dp = SizeTokens.Avatar.sizeLarge,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        backgroundColor.copy(alpha = 0.3f),
                        backgroundColor.copy(alpha = 0.1f),
                    ),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(size * 0.4f),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = ShapeTokens.Border.medium,
        )
    }
}

@Preview(name = "CircularImage States", showBackground = true)
@Composable
private fun CircularImagePreview() {
    QodeTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Image → Initials → Icon fallback:", style = MaterialTheme.typography.labelSmall)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // With image URL
                CircularImage(
                    imageUrl = "https://www.gravatar.com/avatar/2c7d99fe281ecd3bcd65ab915bac6dd5?s=250",
                    fallbackText = "JD",
                    fallbackIcon = QodeNavigationIcons.Profile,
                    contentDescription = "Profile with image",
                )
                // With initials only
                CircularImage(
                    fallbackText = "HB",
                    fallbackIcon = QodeNavigationIcons.Profile,
                    contentDescription = "Halyk Bank initials",
                )
                // With fallback icon only
                CircularImage(
                    fallbackIcon = QodeNavigationIcons.Profile,
                    contentDescription = "Default profile icon",
                )
            }

            Text("Service logos:", style = MaterialTheme.typography.labelSmall)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Service with logo URL
                CircularImage(
                    imageUrl = "https://kaspi.kz/img/kaspi_logo.svg",
                    fallbackText = "KS",
                    fallbackIcon = QodeNavigationIcons.Profile,
                    contentDescription = "Kaspi service",
                )
                // Service with initials
                CircularImage(
                    fallbackText = "HB",
                    fallbackIcon = QodeNavigationIcons.Profile,
                    contentDescription = "Halyk Bank service",
                )
            }
        }
    }
}

@Preview(name = "CircularImage Sizes", showBackground = true)
@Composable
private fun CircularImageSizesPreview() {
    QodeTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularImage(
                fallbackText = "SM",
                fallbackIcon = QodeNavigationIcons.Profile,
                size = SizeTokens.Avatar.sizeSmall,
            )
            CircularImage(
                fallbackText = "MD",
                fallbackIcon = QodeNavigationIcons.Profile,
                size = SizeTokens.Avatar.sizeMedium,
            )
            CircularImage(
                fallbackText = "LG",
                fallbackIcon = QodeNavigationIcons.Profile,
                size = SizeTokens.Avatar.sizeLarge,
            )
            CircularImage(
                fallbackText = "XL",
                fallbackIcon = QodeNavigationIcons.Profile,
                size = SizeTokens.Avatar.sizeXLarge,
            )
        }
    }
}
