package com.qodein.core.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.qodein.core.designsystem.R
import com.qodein.core.designsystem.component.CircularImage
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.shared.model.User

/**
 * User-specific wrapper around CircularImage for profile pictures
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
    CircularImage(
        imageUrl = optimizeImageUrl(user?.profile?.photoUrl),
        fallbackText = user?.let { extractInitials("${it.profile.firstName} ${it.profile.lastName ?: ""}".trim()) },
        fallbackIcon = QodeNavigationIcons.Profile,
        modifier = modifier,
        size = size,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        contentDescription = contentDescription,
    )
}

fun extractInitials(fullName: String?): String? =
    fullName?.trim()?.takeIf { it.isNotBlank() }?.let { name ->
        name.split("\\s+".toRegex())
            .mapNotNull { it.firstOrNull()?.uppercase() }
            .take(2)
            .joinToString("")
            .takeIf { it.isNotEmpty() }
    }

/**
 * Optimizes image URLs for high quality display, specifically Google Photos URLs
 */
private fun optimizeImageUrl(url: String?): String? =
    url?.let {
        when {
            it.contains("googleusercontent.com") -> {
                // Replace low-res Google Photos parameters with high-res
                it.replace("=s96-c", "=s400-c")
                    .replace("=s100-c", "=s400-c")
                    .replace("=s150-c", "=s400-c")
                    .replace("=s200-c", "=s400-c")
            }
            else -> it
        }
    }
