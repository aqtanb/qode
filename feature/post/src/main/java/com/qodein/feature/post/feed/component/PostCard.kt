package com.qodein.feature.post.feed.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.component.CircularImage
import com.qodein.core.designsystem.component.QodeinElevatedCard
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.preview.PostPreviewData
import com.qodein.core.ui.util.rememberFormattedRelativeTime
import com.qodein.feature.post.component.PostImage
import com.qodein.shared.model.Post
import kotlin.time.Instant

@Composable
fun PostCard(
    post: Post,
    onPostClick: (String) -> Unit,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    QodeinElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ShapeTokens.Corner.small),
        onClick = { onPostClick(post.id.value) },
    ) {
        Column(
            modifier = Modifier.padding(SpacingTokens.md),
        ) {
            PostHeader(
                authorName = post.authorName,
                authorAvatarUrl = post.authorAvatarUrl,
                createdAt = post.createdAt,
                tags = post.tags.map { it.value },
                voteScore = post.voteScore,
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                modifier = Modifier.padding(top = SpacingTokens.sm),
            ) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )

                if (post.imageUrls.isNotEmpty()) {
                    HorizontalPager(
                        state = rememberPagerState { post.imageUrls.size },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp),
                    ) { page ->
                        PostImage(
                            uri = post.imageUrls[page],
                            currentPage = page + 1,
                            totalPages = post.imageUrls.size,
                            ratio = null,
                            onClick = { onImageClick(post.imageUrls[page]) },
                        )
                    }
                } else if (!post.content.isNullOrBlank()) {
                    Text(
                        text = post.content!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun PostHeader(
    authorName: String,
    authorAvatarUrl: String?,
    createdAt: Instant,
    voteScore: Int,
    tags: List<String>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularImage(
            imageUrl = authorAvatarUrl,
            fallbackText = authorName,
            fallbackIcon = QodeNavigationIcons.Profile,
            size = SizeTokens.Avatar.sizeSmall,
            contentDescription = authorName,
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.xxs),
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = authorName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            PostTagsRow(tags = tags)
        }

        Column(
            horizontalAlignment = Alignment.End,
        ) {
            Text(
                text = if (voteScore > 0) "+$voteScore" else voteScore.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier.align(Alignment.End),
            )
            Text(
                text = rememberFormattedRelativeTime(createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PostTagsRow(
    tags: List<String>,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xxs),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xxs),
        maxLines = 2,
    ) {
        tags.forEach { tag ->
            Text(
                text = "#$tag",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@ThemePreviews
@Composable
private fun PostCardPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            PostCard(
                post = PostPreviewData.postWithLongEverything,
                onPostClick = {},
                onImageClick = {},
            )

            PostCard(
                post = PostPreviewData.postWithoutImages,
                onPostClick = {},
                onImageClick = {},
            )
        }
    }
}
