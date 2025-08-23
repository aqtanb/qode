package com.qodein.core.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.qodein.shared.model.Comment
import com.qodein.shared.model.CommentId
import com.qodein.shared.model.CommentParentType
import com.qodein.shared.model.UserId
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Modern comment card component following design system
 */
@Composable
fun CommentCard(
    comment: Comment,
    onLikeClick: (CommentId) -> Unit,
    onDislikeClick: (CommentId) -> Unit,
    onUserClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    val likeButtonScale by animateFloatAsState(
        targetValue = if (comment.isUpvotedByCurrentUser) 1.1f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 800f),
        label = "like_button_scale",
    )

    val dislikeButtonScale by animateFloatAsState(
        targetValue = if (comment.isDownvotedByCurrentUser) 1.1f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 800f),
        label = "dislike_button_scale",
    )

    val likeButtonColor by animateColorAsState(
        targetValue = if (comment.isUpvotedByCurrentUser) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        },
        animationSpec = tween(300),
        label = "like_button_color",
    )

    val dislikeButtonColor by animateColorAsState(
        targetValue = if (comment.isDownvotedByCurrentUser) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        },
        animationSpec = tween(300),
        label = "dislike_button_color",
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = SpacingTokens.xs),
        shape = RoundedCornerShape(ShapeTokens.Corner.medium),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(
            width = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(SpacingTokens.md),
        ) {
            // Author info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Avatar
                AsyncImage(
                    model = comment.authorAvatarUrl ?: "https://picsum.photos/seed/${comment.authorUsername}/150/150",
                    contentDescription = "Avatar of ${comment.authorUsername}",
                    modifier = Modifier
                        .size(SizeTokens.Avatar.sizeSmall)
                        .clip(CircleShape)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            shape = CircleShape,
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                        ) { onUserClick(comment.authorUsername) },
                    contentScale = ContentScale.Crop,
                )

                Spacer(modifier = Modifier.width(SpacingTokens.sm))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "@${comment.authorUsername}",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.3.sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.clickable(
                            interactionSource = interactionSource,
                            indication = null,
                        ) { onUserClick(comment.authorUsername) },
                    )

                    Text(
                        text = formatTimeAgo(comment.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }

                // Vote score indicator
                if (comment.voteScore > 3) {
                    Surface(
                        shape = RoundedCornerShape(SpacingTokens.xs),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    ) {
                        Text(
                            text = "+${comment.voteScore}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(
                                horizontal = SpacingTokens.xs,
                                vertical = 2.dp,
                            ),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(SpacingTokens.sm))

            // Comment content
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 20.sp,
                    letterSpacing = 0.2.sp,
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                modifier = Modifier.fillMaxWidth(),
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(SpacingTokens.sm))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Like button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onLikeClick(comment.id) },
                ) {
                    QodeIconButton(
                        onClick = { onLikeClick(comment.id) },
                        icon = QodeActionIcons.Like,
                        contentDescription = if (comment.isUpvotedByCurrentUser) "Unlike" else "Like",
                        variant = QodeButtonVariant.Text,
                        size = QodeButtonSize.Small,
                        modifier = Modifier.scale(likeButtonScale),
                    )

                    if (comment.upvotes > 0) {
                        Text(
                            text = formatCount(comment.upvotes),
                            style = MaterialTheme.typography.bodySmall,
                            color = likeButtonColor,
                            modifier = Modifier.padding(start = SpacingTokens.xs),
                        )
                    }
                }

                // Dislike button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onDislikeClick(comment.id) },
                ) {
                    QodeIconButton(
                        onClick = { onDislikeClick(comment.id) },
                        icon = QodeActionIcons.ThumbsDown,
                        contentDescription = if (comment.isDownvotedByCurrentUser) "Remove dislike" else "Dislike",
                        variant = QodeButtonVariant.Text,
                        size = QodeButtonSize.Small,
                        modifier = Modifier.scale(dislikeButtonScale),
                    )

                    if (comment.downvotes > 0) {
                        Text(
                            text = formatCount(comment.downvotes),
                            style = MaterialTheme.typography.bodySmall,
                            color = dislikeButtonColor,
                            modifier = Modifier.padding(start = SpacingTokens.xs),
                        )
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
        diff < 86400 * 7 -> "${diff / 86400}d"
        else -> "${diff / (86400 * 7)}w"
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
private fun CommentCardPreview() {
    QodeTheme {
        val mockComment = Comment(
            id = CommentId("preview_comment"),
            parentId = "post_123",
            parentType = CommentParentType.POST,
            authorId = UserId("user_1"),
            authorUsername = "deal_hunter",
            authorAvatarUrl = null,
            content = "This is an excellent deal! I've been waiting for this discount for months. " +
                "Thanks for sharing, definitely going to use this promo code.",
            upvotes = 12,
            downvotes = 1,
            createdAt = Clock.System.now(),
            isUpvotedByCurrentUser = true,
            isDownvotedByCurrentUser = false,
        )

        CommentCard(
            comment = mockComment,
            onLikeClick = {},
            onDislikeClick = {},
            onUserClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
