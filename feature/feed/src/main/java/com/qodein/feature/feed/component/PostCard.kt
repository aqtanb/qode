package com.qodein.feature.feed.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.qodein.core.designsystem.component.QodeButtonSize
import com.qodein.core.designsystem.component.QodeButtonVariant
import com.qodein.core.designsystem.component.QodeIconButton
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.shared.model.Post
import com.qodein.shared.model.PostId
import com.qodein.shared.model.Tag
import com.qodein.shared.model.UserId
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Beautiful modern post card with glassmorphism and premium animations
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostCard(
    post: Post,
    onLikeClick: (PostId) -> Unit,
    onCommentClick: (PostId) -> Unit,
    onShareClick: (PostId) -> Unit,
    onUserClick: (String) -> Unit,
    onTagClick: (Tag) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    val likeButtonScale by animateFloatAsState(
        targetValue = if (post.isUpvotedByCurrentUser) 1.1f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 800f),
        label = "like_button_scale",
    )

    val likeButtonColor by animateColorAsState(
        targetValue = if (post.isUpvotedByCurrentUser) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        },
        animationSpec = tween(300),
        label = "like_button_color",
    )

    val cardElevation by animateFloatAsState(
        targetValue = if (post.isUpvotedByCurrentUser) 8f else 4f,
        animationSpec = spring(dampingRatio = 0.7f),
        label = "card_elevation",
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SpacingTokens.xs, vertical = SpacingTokens.xs),
        shape = RoundedCornerShape(ShapeTokens.Corner.large),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = cardElevation.dp,
        tonalElevation = 2.dp,
    ) {
        Box {
            // Subtle accent line for visual appeal
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(
                            topStart = ShapeTokens.Corner.large,
                            topEnd = ShapeTokens.Corner.large,
                        ),
                    ),
            )

            Column(
                modifier = Modifier.padding(SpacingTokens.lg),
            ) {
                // Author info with enhanced design
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Enhanced avatar with border
                    Box {
                        AsyncImage(
                            model = post.authorAvatarUrl ?: "https://picsum.photos/seed/${post.authorUsername}/150/150",
                            contentDescription = "Avatar of ${post.authorUsername}",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    shape = CircleShape,
                                )
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null,
                                ) { onUserClick(post.authorUsername) },
                            contentScale = ContentScale.Crop,
                        )

                        // Online indicator
                        Box(
                            modifier = Modifier
                                .size(SizeTokens.Decoration.sizeSmall)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                .align(Alignment.BottomEnd),
                        )
                    }

                    Spacer(modifier = Modifier.width(SpacingTokens.md))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "@${post.authorUsername}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.clickable(
                                interactionSource = interactionSource,
                                indication = null,
                            ) { onUserClick(post.authorUsername) },
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                        ) {
                            Text(
                                text = formatTimeAgo(post.createdAt),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            )

                            // Engagement indicator
                            if (post.voteScore > 50) {
                                Surface(
                                    shape = RoundedCornerShape(ShapeTokens.Corner.medium),
                                    color = MaterialTheme.colorScheme.errorContainer,
                                ) {
                                    Text(
                                        text = "🔥 Hot",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.padding(
                                            horizontal = SpacingTokens.xs + 2.dp,
                                            vertical = 2.dp,
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(SpacingTokens.md))

                // Enhanced post content
                Text(
                    text = post.content,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 24.sp,
                        letterSpacing = 0.25.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )

                // Beautiful enhanced tags
                if (post.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(SpacingTokens.md))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.Chip.spacing),
                        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                    ) {
                        post.tags.forEach { tag ->
                            Surface(
                                onClick = { onTagClick(tag) },
                                shape = RoundedCornerShape(SpacingTokens.Chip.spacing + 8.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                ),
                                modifier = Modifier.height(28.dp),
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        horizontal = SpacingTokens.Chip.horizontalPadding,
                                        vertical = SpacingTokens.xs,
                                    ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                                ) {
                                    // Tag indicator
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.tertiary),
                                    )

                                    Text(
                                        text = "#${tag.name}",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Medium,
                                            letterSpacing = 0.3.sp,
                                        ),
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        maxLines = 1,
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(SpacingTokens.md))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Like button with animation
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onLikeClick(post.id) },
                    ) {
                        QodeIconButton(
                            onClick = { onLikeClick(post.id) },
                            icon = QodeActionIcons.Like,
                            contentDescription = if (post.isUpvotedByCurrentUser) "Unlike" else "Like",
                            variant = QodeButtonVariant.Text,
                            size = QodeButtonSize.Small,
                            modifier = Modifier.scale(likeButtonScale),
                        )

                        AnimatedVisibility(
                            visible = post.upvotes > 0,
                            enter = expandHorizontally() + fadeIn(),
                            exit = shrinkHorizontally() + fadeOut(),
                        ) {
                            Text(
                                text = formatCount(post.upvotes),
                                style = MaterialTheme.typography.bodySmall,
                                color = likeButtonColor,
                                modifier = Modifier.padding(start = SpacingTokens.xs),
                            )
                        }
                    }

                    // Comment button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onCommentClick(post.id) },
                    ) {
                        QodeIconButton(
                            onClick = { onCommentClick(post.id) },
                            icon = QodeActionIcons.Comment,
                            contentDescription = "Comments",
                            variant = QodeButtonVariant.Text,
                            size = QodeButtonSize.Small,
                        )

                        // Comments count removed - handled by separate system
                    }

                    // Share button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onShareClick(post.id) },
                    ) {
                        QodeIconButton(
                            onClick = { onShareClick(post.id) },
                            icon = QodeActionIcons.Share,
                            contentDescription = "Share",
                            variant = QodeButtonVariant.Text,
                            size = QodeButtonSize.Small,
                        )

                        if (post.shares > 0) {
                            Text(
                                text = formatCount(post.shares),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = SpacingTokens.xs),
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatTimeAgo(createdAt: Instant): String {
    val now = Clock.System.now()
    val diff = now.epochSeconds - createdAt.epochSeconds

    return when {
        diff < 60 -> "now"
        diff < 3600 -> "${diff / 60}m"
        diff < 86400 -> "${diff / 3600}h"
        else -> "${diff / 86400}d"
    }
}

private fun formatCount(count: Int): String =
    when {
        count < 1000 -> count.toString()
        count < 1000000 -> "${count / 1000}k"
        else -> "${count / 1000000}m"
    }

// Preview
@Preview(showBackground = true)
@Composable
private fun PostCardPreview() {
    QodeTheme {
        val mockPost = Post(
            id = PostId("preview_post"),
            authorId = UserId("user_1"),
            authorUsername = "deal_finder",
            authorAvatarUrl = null,
            content = "🔥 Found an amazing subscription deal! Anyone tried this service before? " +
                "The savings are incredible and I'm curious about the user experience.",
            tags = listOf(
                Tag.create("streaming"),
                Tag.create("deals"),
                Tag.create("music"),
            ),
            upvotes = 42,
            downvotes = 3,
            shares = 3,
            createdAt = Clock.System.now(),
            isUpvotedByCurrentUser = true,
        )

        PostCard(
            post = mockPost,
            onLikeClick = {},
            onCommentClick = {},
            onShareClick = {},
            onUserClick = {},
            onTagClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
