package com.qodein.feature.post.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.feature.post.R
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

@Composable
internal fun FullScreenImageViewer(
    uri: String,
    onDismiss: () -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeEffect(
                    state = hazeState,
                    style = HazeStyle(
                        tint = HazeTint(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
                    ),
                )
                .clickable(onClick = onDismiss),
        )

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(uri.toUri())
                .crossfade(true)
                .build(),
            contentDescription = stringResource(R.string.cd_post_image),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.7f),
            contentScale = ContentScale.Fit,
        )
    }
}

@PreviewLightDark
@Composable
private fun FullScreenImageViewerPreview() {
    QodeTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            FullScreenImageViewer(
                uri = "content://media/image/123",
                onDismiss = {},
                hazeState = remember { HazeState() },
            )
        }
    }
}
