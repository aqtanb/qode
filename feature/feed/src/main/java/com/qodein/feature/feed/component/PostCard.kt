package com.qodein.feature.feed.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.qodein.core.data.model.PostSummaryDto
import com.qodein.core.designsystem.component.CircularImage
import com.qodein.core.designsystem.component.QodeCard
import com.qodein.core.designsystem.component.QodeCardVariant
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens

@Composable
fun PostCard(
    post: PostSummaryDto,
    onPostClick: (String) -> Unit,
    onCommentClick: (String) -> Unit,
    onShareClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    QodeCard(
        modifier = modifier.fillMaxWidth(),
        variant = QodeCardVariant.Elevated,
        shape = RoundedCornerShape(ShapeTokens.Corner.extraLarge),
        onClick = { onPostClick(post.id) },
        contentPadding = PaddingValues(SpacingTokens.lg),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            // Author Info Header
            PostAuthorHeader(
                authorName = post.authorName,
                authorAvatarUrl = post.authorAvatarUrl,
                timeAgo = post.createdAt?.let { "2h" },
            )

            // Post Title
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            // Post Content Preview
            Text(
                text = post.contentPreview,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )

            // Images Grid (if available)
            if (post.imageUrls.isNotEmpty()) {
                PostImagesGrid(
                    imageUrls = post.imageUrls,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Tags Row (if available)
            if (post.tags.isNotEmpty()) {
                PostTagsRow(tags = post.tags)
            }

            // Action Buttons Row
            PostActionRow(
                voteScore = post.voteScore,
                commentCount = post.commentCount,
                userVoteState = post.userVoteState,
                onCommentClick = { onCommentClick(post.id) },
                onShareClick = { onShareClick(post.id) },
            )
        }
    }
}

@Composable
private fun PostAuthorHeader(
    authorName: String,
    authorAvatarUrl: String?,
    timeAgo: String?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularImage(
            imageUrl = authorAvatarUrl,
            fallbackText = authorName,
            fallbackIcon = QodeNavigationIcons.Profile,
            size = SizeTokens.Avatar.sizeMedium,
            contentDescription = "$authorName profile picture",
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = authorName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (timeAgo != null) {
                Text(
                    text = timeAgo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PostImagesGrid(
    imageUrls: List<String>,
    modifier: Modifier = Modifier
) {
    when (imageUrls.size) {
        1 -> {
            // Single large image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrls[0])
                    .crossfade(true)
                    .build(),
                contentDescription = "Post image",
                modifier = modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(ShapeTokens.Corner.large)),
                contentScale = ContentScale.Crop,
            )
        }

        in 2..5 -> {
            // Horizontal scrollable row for multiple images
            LazyRow(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
            ) {
                items(imageUrls) { imageUrl ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Post image",
                        modifier = Modifier
                            .size(120.dp)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(ShapeTokens.Corner.medium)),
                        contentScale = ContentScale.Crop,
                    )
                }
            }
        }
    }
}

@Composable
private fun PostTagsRow(
    tags: List<String>,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
    ) {
        items(tags.take(5)) { tag ->
            // Limit to 5 tags
            Surface(
                shape = RoundedCornerShape(ShapeTokens.Corner.full),
                color = MaterialTheme.colorScheme.secondaryContainer,
            ) {
                Text(
                    text = "#$tag",
                    modifier = Modifier.padding(
                        horizontal = SpacingTokens.sm,
                        vertical = SpacingTokens.xxs,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
    }
}

@Composable
private fun PostActionRow(
    voteScore: Int,
    commentCount: Int,
    userVoteState: String,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Vote Score (read-only for feed)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xxs),
        ) {
            Icon(
                imageVector = when (userVoteState) {
                    "UPVOTE" -> QodeActionIcons.Thumbs
                    "DOWNVOTE" -> QodeActionIcons.ThumbsDown
                    else -> QodeActionIcons.Thumbs
                },
                contentDescription = "Vote score",
                modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                tint = when (userVoteState) {
                    "UPVOTE" -> MaterialTheme.colorScheme.primary
                    "DOWNVOTE" -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            Text(
                text = formatCount(voteScore),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
        ) {
            // Comment Button
            IconButton(onClick = onCommentClick) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xxs),
                ) {
                    Icon(
                        imageVector = QodeActionIcons.Comment,
                        contentDescription = "Comments",
                        modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = formatCount(commentCount),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Share Button
            IconButton(onClick = onShareClick) {
                Icon(
                    imageVector = QodeActionIcons.Share,
                    contentDescription = "Share",
                    modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun formatCount(count: Int): String =
    when {
        count < 1000 -> count.toString()
        count < 1000000 -> {
            val thousands = count / 1000.0
            if (thousands % 1 == 0.0) {
                "${thousands.toInt()}K"
            } else {
                String.format("%.1fK", thousands)
            }
        }
        else -> {
            val millions = count / 1000000.0
            if (millions % 1 == 0.0) {
                "${millions.toInt()}M"
            } else {
                String.format("%.1fM", millions)
            }
        }
    }

@Preview(showBackground = true)
@Composable
private fun PostCardPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            // Post with multiple images
            PostCard(
                post = PostSummaryDto(
                    id = "1",
                    authorName = "John Doe",
                    authorAvatarUrl = null,
                    title = "Amazing new features in Android 14 that will change everything",
                    contentPreview = "Android 14 brings incredible new " +
                        "features that will revolutionize how we develop mobile apps. " +
                        "From enhanced privacy controls to better performance optimizations...",
                    imageUrls = listOf(
                        "https://picsum.photos/400/300?random=1",
                        "https://picsum.photos/400/300?random=2",
                        "https://picsum.photos/400/300?random=3",
                    ),
                    tags = listOf("android", "mobile", "development", "features"),
                    upvotes = 142,
                    downvotes = 12,
                    commentCount = 28,
                    voteScore = 130,
                    createdAt = null,
                    userVoteState = "UPVOTE",
                ),
                onPostClick = {},
                onCommentClick = {},
                onShareClick = {},
            )

            // Post without images
            PostCard(
                post = PostSummaryDto(
                    id = "2",
                    authorName = "Sarah Smith",
                    authorAvatarUrl = "https://picsum.photos/100/100?random=10",
                    title = "Best coding practices for clean architecture",
                    contentPreview = "Clean architecture is essential for maintainable code. " +
                        "Here are the top practices that every developer should follow to write better, more organized code...",
                    imageUrls = emptyList(),
                    tags = listOf("coding", "architecture", "best-practices"),
                    upvotes = 89,
                    downvotes = 3,
                    commentCount = 15,
                    voteScore = 86,
                    createdAt = null,
                    userVoteState = "NONE",
                ),
                onPostClick = {},
                onCommentClick = {},
                onShareClick = {},
            )
        }
    }
}
