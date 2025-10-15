package com.qodein.feature.post.submission.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.post.R

@Composable
internal fun PostSubmissionImage(
    uri: String,
    currentPage: Int,
    totalPages: Int,
    onRemove: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f / 1f)
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

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(SpacingTokens.sm)
                .background(
                    MaterialTheme.colorScheme.inverseSurface,
                    RoundedCornerShape(ShapeTokens.Corner.medium),
                )
                .padding(horizontal = SpacingTokens.xs, vertical = SpacingTokens.xxxs),
        ) {
            Text(
                text = "$currentPage/$totalPages",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.inverseOnSurface,
            )
        }

        Icon(
            imageVector = QodeActionIcons.Close,
            contentDescription = stringResource(R.string.cd_remove_image),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(SpacingTokens.sm)
                .size(SizeTokens.Icon.sizeLarge)
                .background(
                    MaterialTheme.colorScheme.inverseSurface,
                    CircleShape,
                )
                .clickable(onClick = onRemove)
                .padding(SpacingTokens.xxxs),
            tint = MaterialTheme.colorScheme.inverseOnSurface,
        )
    }
}

@PreviewLightDark
@Composable
private fun ImageCarouselItemPreview() {
    QodeTheme {
        PostSubmissionImage(
            uri = "content://media/image/123",
            currentPage = 2,
            totalPages = 5,
            onRemove = {},
            onClick = {},
        )
    }
}
