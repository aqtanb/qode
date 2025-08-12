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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.qodein.core.designsystem.R
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens

/**
 * Optimized avatar component for user profile pictures with automatic Google image enhancement
 *
 * @param photoUrl The URL of the profile picture
 * @param size The size of the avatar (uses SizeTokens.Avatar values)
 * @param contentDescription Accessibility description for the image
 * @param modifier Modifier for styling
 */
@Composable
fun QodeAvatar(
    photoUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = SizeTokens.Avatar.sizeLarge,
    contentDescription: String = stringResource(R.string.profile_picture_description)
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size),
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(getOptimizedImageUrl(photoUrl))
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
            error = { QodeAvatarPlaceholder() },
            loading = { QodeAvatarLoading() },
        )
    }
}

/**
 * Placeholder avatar shown when image fails to load or is unavailable
 */
@Composable
fun QodeAvatarPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    ),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = QodeNavigationIcons.Profile,
            contentDescription = stringResource(R.string.profile_picture_description),
            modifier = Modifier.size(SizeTokens.Icon.sizeLarge),
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

/**
 * Loading state avatar with circular progress indicator
 */
@Composable
fun QodeAvatarLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                    ),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(SizeTokens.Icon.sizeXLarge),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = ShapeTokens.Border.medium,
        )
    }
}

/**
 * Optimizes image URLs for better quality, specifically handles Google profile images
 * by replacing low-resolution parameters with high-resolution ones
 *
 * @param url The original image URL
 * @return Optimized URL with better resolution parameters, or null if input is null
 */
private fun getOptimizedImageUrl(url: String?): String? =
    url?.let {
        if (it.contains("googleusercontent.com")) {
            it.replace("=s96-c", "=s400-c")
                .replace("=s100-c", "=s400-c")
        } else {
            it
        }
    }

// Previews
@Preview(name = "Avatar with Image", showBackground = true)
@Composable
private fun QodeAvatarPreview() {
    QodeTheme {
        QodeAvatar(
            photoUrl = "https://www.gravatar.com/avatar/2c7d99fe281ecd3bcd65ab915bac6dd5?s=250",
            size = SizeTokens.Avatar.sizeXLarge,
        )
    }
}

@Preview(name = "Avatar Sizes", showBackground = true)
@Composable
private fun QodeAvatarSizesPreview() {
    QodeTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            QodeAvatar(
                photoUrl = null,
                size = SizeTokens.Avatar.sizeSmall,
            )
            QodeAvatar(
                photoUrl = null,
                size = SizeTokens.Avatar.sizeMedium,
            )
            QodeAvatar(
                photoUrl = null,
                size = SizeTokens.Avatar.sizeLarge,
            )
            QodeAvatar(
                photoUrl = null,
                size = SizeTokens.Avatar.sizeXLarge,
            )
        }
    }
}

@Preview(name = "Avatar States", showBackground = true)
@Composable
private fun QodeAvatarStatesPreview() {
    QodeTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Placeholder:")
            QodeAvatarPlaceholder(
                modifier = Modifier.size(SizeTokens.Avatar.sizeLarge),
            )

            Text("Loading:")
            QodeAvatarLoading(
                modifier = Modifier.size(SizeTokens.Avatar.sizeLarge),
            )
        }
    }
}
