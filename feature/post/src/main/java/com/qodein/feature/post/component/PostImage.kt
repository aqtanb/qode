package com.qodein.feature.post.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.component.ButtonSize
import com.qodein.core.designsystem.component.PageIndicator
import com.qodein.core.designsystem.component.QodeinIconButton
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.post.R

@Composable
internal fun PostImage(
    uri: String,
    currentPage: Int,
    totalPages: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onRemove: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f / 1f)
            .clip(RoundedCornerShape(ShapeTokens.Corner.large))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(uri.toUri())
                .crossfade(true)
                .build(),
            contentDescription = stringResource(R.string.cd_post_image),
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick),
            contentScale = ContentScale.Crop,
        )

        if (onRemove != null) {
            QodeinIconButton(
                onClick = onRemove,
                icon = QodeActionIcons.Close,
                contentDescription = stringResource(R.string.cd_remove_image),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                size = ButtonSize.Small,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(SpacingTokens.sm),
            )
        }

        PageIndicator(
            currentIndex = currentPage - 1,
            totalPages = totalPages,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(SpacingTokens.sm),
        )
    }
}

@ThemePreviews
@Composable
private fun ImageCarouselItemPreview() {
    QodeTheme {
        PostImage(
            uri = "content://media/image/123",
            currentPage = 2,
            totalPages = 3,
            onRemove = {},
            onClick = {},
        )
    }
}
