package com.qodein.core.ui.component

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import com.qodein.core.ui.PreviewParameterData
import com.qodein.shared.model.User

/**
 * Profile avatar with smart fallback: Image → Initials → Icon
 */
@Composable
fun ProfileAvatar(
    user: User?,
    modifier: Modifier = Modifier,
    size: Dp = SizeTokens.Avatar.sizeLarge,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    contentDescription: String = stringResource(R.string.profile_picture_description)
) {
    ProfileAvatar(
        photoUrl = user?.profile?.photoUrl,
        initials = user?.let { extractInitials("${it.profile.firstName} ${it.profile.lastName ?: ""}".trim()) },
        modifier = modifier,
        size = size,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        contentDescription = contentDescription,
    )
}

/**
 * Profile avatar with manual parameters
 */
@Composable
fun ProfileAvatar(
    photoUrl: String? = null,
    initials: String? = null,
    modifier: Modifier = Modifier,
    size: Dp = SizeTokens.Avatar.sizeLarge,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    contentDescription: String = stringResource(R.string.profile_picture_description)
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size),
    ) {
        if (photoUrl != null) {
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
                error = {
                    AvatarFallback(
                        initials = initials,
                        size = size,
                        backgroundColor = backgroundColor,
                        contentColor = contentColor,
                        contentDescription = contentDescription,
                    )
                },
                loading = {
                    AvatarLoading(
                        size = size,
                        backgroundColor = backgroundColor,
                    )
                },
            )
        } else {
            AvatarFallback(
                initials = initials,
                size = size,
                backgroundColor = backgroundColor,
                contentColor = contentColor,
                contentDescription = contentDescription,
            )
        }
    }
}

@Composable
private fun AvatarFallback(
    initials: String?,
    size: Dp,
    backgroundColor: Color,
    contentColor: Color,
    contentDescription: String,
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
                    imageVector = QodeNavigationIcons.Profile,
                    contentDescription = contentDescription,
                    modifier = Modifier.size(size * 0.6f),
                    tint = contentColor,
                )
            }
        }
    }
}

@Composable
fun AvatarLoading(
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

private fun getOptimizedImageUrl(url: String?): String? =
    url?.let {
        when {
            it.contains("googleusercontent.com") -> {
                it.replace("=s96-c", "=s400-c")
                    .replace("=s100-c", "=s400-c")
                    .replace("=s150-c", "=s400-c")
            }
            it.contains("graph.facebook.com") -> {
                "$it?width=400&height=400"
            }
            else -> it
        }
    }

fun extractInitials(fullName: String?): String? =
    fullName?.trim()?.takeIf { it.isNotBlank() }?.let { name ->
        name.split("\\s+".toRegex())
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")
            .takeIf { it.isNotEmpty() }
    }

@Preview(name = "Avatar States", showBackground = true)
@Composable
private fun ProfileAvatarPreview() {
    QodeTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("With User object:", style = MaterialTheme.typography.labelSmall)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ProfileAvatar(user = PreviewParameterData.sampleUser)
                ProfileAvatar(user = PreviewParameterData.powerUser)
                ProfileAvatar(user = null)
            }

            Text("Manual parameters:", style = MaterialTheme.typography.labelSmall)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ProfileAvatar(photoUrl = "https://www.gravatar.com/avatar/2c7d99fe281ecd3bcd65ab915bac6dd5?s=250")
                ProfileAvatar(initials = "JD")
                ProfileAvatar()
            }
        }
    }
}

@Preview(name = "Avatar Sizes", showBackground = true)
@Composable
private fun ProfileAvatarSizesPreview() {
    QodeTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProfileAvatar(
                user = PreviewParameterData.sampleUser,
                size = SizeTokens.Avatar.sizeSmall,
            )
            ProfileAvatar(
                user = PreviewParameterData.sampleUser,
                size = SizeTokens.Avatar.sizeMedium,
            )
            ProfileAvatar(
                user = PreviewParameterData.sampleUser,
                size = SizeTokens.Avatar.sizeLarge,
            )
            ProfileAvatar(
                user = PreviewParameterData.sampleUser,
                size = SizeTokens.Avatar.sizeXLarge,
            )
        }
    }
}
