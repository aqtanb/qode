package com.qodein.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SizeTokens

/**
 * Async image component with fallback to initials text
 */
@Composable
fun QodeinAsyncImage(
    imageUrl: String,
    fallbackText: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: Dp = SizeTokens.Avatar.sizeLarge,
    shape: Shape = CircleShape
) {
    Box(
        modifier = modifier.clip(shape).size(size).background(color = MaterialTheme.colorScheme.inverseSurface),
        contentAlignment = Alignment.Center,
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .allowHardware(false)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build(),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            error = {
                AsyncImageFallback(
                    fallbackText = fallbackText,
                    size = size,
                )
            },
            loading = {
                AsyncImageLoading(
                    size = size,
                )
            },
            modifier = Modifier.matchParentSize(),
        )
    }
}

@Composable
private fun AsyncImageFallback(
    fallbackText: String,
    size: Dp
) {
    val displayInitials = fallbackText.take(2).uppercase()
    val fontSize = with(LocalDensity.current) { (size * 0.5f).toSp() }

    Text(
        text = displayInitials,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.inverseOnSurface,
        fontSize = fontSize,
        lineHeight = fontSize,
        textAlign = TextAlign.Center,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AsyncImageLoading(size: Dp) {
    ContainedLoadingIndicator(
        modifier = Modifier.size(size),
        polygons = listOf(
            MaterialShapes.Circle,
            MaterialShapes.Pentagon,
            MaterialShapes.Cookie9Sided,
            MaterialShapes.Ghostish,
        ),
    )
}

@PreviewLightDark
@Composable
private fun QodeinAsyncImageStatesPreview() {
    QodeTheme {
        Surface {
            Column {
                QodeinAsyncImage("https://example.com/avatar.jpg", "JD", "Avatar")

                AsyncImageFallback("John Doe", SizeTokens.Avatar.sizeLarge)

                AsyncImageLoading(SizeTokens.Avatar.sizeLarge)
            }
        }
    }
}
