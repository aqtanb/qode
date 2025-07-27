package com.qodein.core.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.pullToRefreshIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.theme.QodeCorners
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens

/**
 * Full screen loading indicator with optional message
 */
@Composable
fun QodeLoadingScreen(
    modifier: Modifier = Modifier,
    message: String? = null
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp,
            )

            message?.let {
                Spacer(modifier = Modifier.height(SpacingTokens.md))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/**
 * Inline loading indicator for content areas
 */
@Composable
fun QodeLoadingContent(
    modifier: Modifier = Modifier,
    message: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(SpacingTokens.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 3.dp,
        )

        message?.let {
            Spacer(modifier = Modifier.height(SpacingTokens.sm))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Skeleton loading placeholder for cards
 */
@Composable
fun QodeCardSkeleton(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "skeleton_alpha",
    )

    QodeCard(
        modifier = modifier.alpha(alpha),
        variant = QodeCardVariant.Elevated,
    ) {
        Column {
            // Title skeleton
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(24.dp)
                    .clip(RoundedCornerShape(QodeCorners.xs))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
            )

            Spacer(modifier = Modifier.height(SpacingTokens.sm))

            // Description skeleton
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(QodeCorners.xs))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
            )

            Spacer(modifier = Modifier.height(SpacingTokens.xs))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(QodeCorners.xs))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
            )

            Spacer(modifier = Modifier.height(SpacingTokens.md))

            // Action skeleton
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(QodeCorners.sm))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
            )
        }
    }
}

/**
 * List item skeleton loading placeholder
 */
@Composable
fun QodeListItemSkeleton(
    modifier: Modifier = Modifier,
    showIcon: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "skeleton_alpha",
    )

    Surface(
        modifier = modifier.alpha(alpha),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showIcon) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(QodeCorners.sm))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                )
                Spacer(modifier = Modifier.width(SpacingTokens.md))
            }

            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(QodeCorners.xs))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                )
                Spacer(modifier = Modifier.height(SpacingTokens.xs))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(QodeCorners.xs))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                )
            }
        }
    }
}

/**
 * Shimmer effect for loading states
 */
@Composable
fun QodeShimmer(
    modifier: Modifier = Modifier,
    width: Float = 0.3f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer_translate",
    )

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            Color.Transparent,
                        ),
                        startX = translateAnim - width,
                        endX = translateAnim + width,
                    ),
                ),
        )
    }
}

/**
 * Pull to refresh indicator
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QodePullRefreshIndicator(
    isRefreshing: Boolean,
    state: PullToRefreshState,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    scale: Boolean = true
) {
    Box(
        modifier = modifier
            .pullToRefreshIndicator(
                isRefreshing = isRefreshing,
                state = state,
                containerColor = backgroundColor,
            ),
    )
}

// Previews
@Preview(name = "Loading States", showBackground = true)
@Composable
private fun QodeLoadingStatesPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
        ) {
            Text("Loading Screen", style = MaterialTheme.typography.titleMedium)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                QodeLoadingScreen(message = "Loading promo codes...")
            }

            Text("Loading Content", style = MaterialTheme.typography.titleMedium)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                QodeLoadingContent(message = "Please wait...")
            }

            Text("Card Skeleton", style = MaterialTheme.typography.titleMedium)
            QodeCardSkeleton()

            Text("List Item Skeleton", style = MaterialTheme.typography.titleMedium)
            Column(verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm)) {
                QodeListItemSkeleton()
                QodeListItemSkeleton(showIcon = false)
            }
        }
    }
}
