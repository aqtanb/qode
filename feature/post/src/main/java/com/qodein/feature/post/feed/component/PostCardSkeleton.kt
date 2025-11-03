package com.qodein.feature.post.feed.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.component.QodeinElevatedCard
import com.qodein.core.designsystem.component.ShimmerBox
import com.qodein.core.designsystem.component.ShimmerLine
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SpacingTokens

/**
 * Skeleton loading state for PostCard.
 *
 * Mimics the structure of PostCard with shimmer effects:
 * - Author header (avatar, name, tags, vote score, timestamp)
 * - Title (3 lines)
 * - Image placeholder OR content text
 */
@Composable
fun PostCardSkeleton(
    modifier: Modifier = Modifier,
    showImage: Boolean = true
) {
    QodeinElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ShapeTokens.Corner.small),
        onClick = { },
    ) {
        Column(
            modifier = Modifier.padding(SpacingTokens.md),
        ) {
            // Author header skeleton
            Row(
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Avatar
                ShimmerBox(
                    width = 32.dp,
                    height = 32.dp,
                )

                // Name + tags column
                Column(
                    verticalArrangement = Arrangement.spacedBy(SpacingTokens.xxs),
                    modifier = Modifier.weight(1f),
                ) {
                    ShimmerLine(width = 100.dp, height = 16.dp)
                    Row(horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xxs)) {
                        ShimmerLine(width = 50.dp, height = 14.dp)
                        ShimmerLine(width = 60.dp, height = 14.dp)
                    }
                }

                // Vote score + timestamp
                Column(
                    verticalArrangement = Arrangement.spacedBy(SpacingTokens.xxs),
                ) {
                    ShimmerLine(width = 30.dp, height = 14.dp)
                    ShimmerLine(width = 40.dp, height = 14.dp)
                }
            }

            Spacer(modifier = Modifier.height(SpacingTokens.sm))

            // Title skeleton (3 lines)
            Column(
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.xxs),
            ) {
                ShimmerLine(width = 300.dp, height = 20.dp)
                ShimmerLine(width = 280.dp, height = 20.dp)
                ShimmerLine(width = 200.dp, height = 20.dp)
            }

            Spacer(modifier = Modifier.height(SpacingTokens.sm))

            // Image or content skeleton
            if (showImage) {
                ShimmerBox(
                    width = 400.dp,
                    height = 320.dp,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                // Content text skeleton (5 lines)
                Column(
                    verticalArrangement = Arrangement.spacedBy(SpacingTokens.xxs),
                ) {
                    ShimmerLine(width = 350.dp, height = 16.dp)
                    ShimmerLine(width = 340.dp, height = 16.dp)
                    ShimmerLine(width = 360.dp, height = 16.dp)
                    ShimmerLine(width = 320.dp, height = 16.dp)
                    ShimmerLine(width = 180.dp, height = 16.dp)
                }
            }
        }
    }
}

@ThemePreviews
@Composable
private fun PostCardSkeletonPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            PostCardSkeleton(showImage = true)
            Spacer(modifier = Modifier.height(SpacingTokens.sm))
            PostCardSkeleton(showImage = false)
        }
    }
}
