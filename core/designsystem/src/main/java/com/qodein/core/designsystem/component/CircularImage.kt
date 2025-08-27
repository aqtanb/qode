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
    imageUrl: String? = null,
    initials: String? = null,
    fallbackIcon: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = SizeTokens.Avatar.sizeLarge,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    contentDescription: String? = null
) {
    val density = LocalDensity.current
    val pixelSize = with(density) { (size * 2).roundToPx() } // 2x for crisp quality

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size),
    ) {
        if (imageUrl != null) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .size(pixelSize) // Dynamic size based on actual display size
                    .crossfade(true)
                    .allowHardware(false)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = contentDescription,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                error = {
                    CircularImageFallback(
                        initials = initials,
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
                initials = initials,
                fallbackIcon = fallbackIcon,
                size = size,
                backgroundColor = backgroundColor,
                contentColor = contentColor,
                contentDescription = contentDescription,
            )
        }
    }
}

@Composable
private fun CircularImageFallback(
    initials: String?,
    fallbackIcon: ImageVector,
    size: Dp,
    backgroundColor: Color,
    contentColor: Color,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = backgroundColor,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            val displayInitials = initials?.takeIf { it.isNotBlank() }?.take(2)?.uppercase()

            if (displayInitials != null) {
                Text(
                    text = displayInitials,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = contentColor,
                )
            } else {
                Icon(
                    imageVector = fallbackIcon,
                    contentDescription = contentDescription,
                    modifier = Modifier.size(size * 0.6f),
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
                    initials = "JD",
                    fallbackIcon = QodeNavigationIcons.Profile,
                    contentDescription = "Profile with image",
                )
                // With initials only
                CircularImage(
                    initials = "HB",
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
                    initials = "KS",
                    fallbackIcon = QodeNavigationIcons.Profile,
                    contentDescription = "Kaspi service",
                )
                // Service with initials
                CircularImage(
                    initials = "HB",
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
                initials = "SM",
                fallbackIcon = QodeNavigationIcons.Profile,
                size = SizeTokens.Avatar.sizeSmall,
            )
            CircularImage(
                initials = "MD",
                fallbackIcon = QodeNavigationIcons.Profile,
                size = SizeTokens.Avatar.sizeMedium,
            )
            CircularImage(
                initials = "LG",
                fallbackIcon = QodeNavigationIcons.Profile,
                size = SizeTokens.Avatar.sizeLarge,
            )
            CircularImage(
                initials = "XL",
                fallbackIcon = QodeNavigationIcons.Profile,
                size = SizeTokens.Avatar.sizeXLarge,
            )
        }
    }
}
