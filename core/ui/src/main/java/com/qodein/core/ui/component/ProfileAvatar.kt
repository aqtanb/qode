package com.qodein.core.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.qodein.core.designsystem.component.QodeinAsyncImage
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.ui.R
import com.qodein.shared.model.User

/**
 * User-specific wrapper around CircularImage for profile pictures
 */
@Composable
fun ProfileAvatar(
    user: User?,
    modifier: Modifier = Modifier,
    size: Dp = SizeTokens.Avatar.sizeLarge,
    contentDescription: String = stringResource(R.string.profile_picture_description)
) {
    QodeinAsyncImage(
        imageUrl = optimizeImageUrl(user?.profile?.photoUrl) ?: "",
        fallbackText = user?.profile?.displayName ?: "",
        modifier = modifier,
        size = size,
        contentDescription = contentDescription,
    )
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
