package com.qodein.feature.post.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.component.ButtonSize
import com.qodein.core.designsystem.component.PageIndicator
import com.qodein.core.designsystem.component.QodeinIconButton
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeUIIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.post.R

@Composable
internal fun PostImage(
    uri: String,
    currentPage: Int,
    totalPages: Int,
    modifier: Modifier = Modifier,
    ratio: Float? = null,
    onRemove: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val aspectModifier = ratio?.let { Modifier.aspectRatio(it) } ?: Modifier

    Box(
        modifier = modifier
            .then(aspectModifier)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(uri.toUri())
                .crossfade(true)
                .build(),
            contentDescription = stringResource(R.string.cd_post_image),
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (onClick != null) {
                        Modifier.clickable(onClick = onClick)
                    } else {
                        Modifier
                    },
                ),
            contentScale = ContentScale.Crop,
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            },
            error = {
                PostImageErrorStateContent()
            },
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

@Composable
private fun PostImageErrorStateContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        ) {
            Icon(
                imageVector = QodeUIIcons.Error,
                contentDescription = stringResource(R.string.cd_image_load_failed),
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(SizeTokens.Icon.sizeXLarge),
            )
            Text(
                text = stringResource(R.string.image_load_failed),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
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
            ratio = 1f,
        )
    }
}

@ThemePreviews
@Composable
private fun PostImageErrorStatePreview() {
    QodeTheme {
        PostImageErrorStateContent()
    }
}
