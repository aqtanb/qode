package com.qodein.core.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.component.QodeAvatar
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.component.QodeButtonSize
import com.qodein.core.designsystem.component.QodeButtonVariant
import com.qodein.core.designsystem.component.QodeEmptyState
import com.qodein.core.designsystem.component.QodeTextField
import com.qodein.core.designsystem.component.QodeTextFieldVariant
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.theme.QodeAnimation
import com.qodein.core.designsystem.theme.QodeCorners
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.model.Comment
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * CommentCard component for displaying threaded comments
 *
 * @param comment The comment data to display
 * @param onUpvoteClick Called when upvote button is clicked
 * @param onReplyClick Called when reply button is clicked
 * @param onMoreClick Called when more options menu is clicked
 * @param modifier Modifier to be applied to the card
 * @param isLoggedIn Whether the user is logged in
 * @param depth The nesting depth for threading (0 = top level)
 * @param maxDepth Maximum nesting depth to display
 */
@Composable
fun CommentCard(
    comment: Comment,
    onUpvoteClick: (Comment) -> Unit,
    onReplyClick: (Comment) -> Unit,
    onMoreClick: (Comment) -> Unit,
    modifier: Modifier = Modifier,
    isLoggedIn: Boolean = false,
    depth: Int = 0,
    maxDepth: Int = 3
) {
    var showReplies by remember { mutableStateOf(true) }
    var showMoreMenu by remember { mutableStateOf(false) }

    val isNested = depth > 0
    val hasReplies = comment.replies.isNotEmpty()
    val canNestFurther = depth < maxDepth

    Column(
        modifier = modifier
            .animateContentSize(
                animationSpec = tween(durationMillis = QodeAnimation.MEDIUM),
            ),
    ) {
        // Main comment
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isNested) {
                        Modifier
                            .padding(start = (depth * 16).dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                RoundedCornerShape(QodeCorners.sm),
                            )
                            .padding(SpacingTokens.sm)
                    } else {
                        Modifier.padding(SpacingTokens.sm)
                    },
                ),
        ) {
            // Avatar
            QodeAvatar(
                text = comment.username.take(2),
                size = if (isNested) 32.dp else 40.dp,
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            Spacer(modifier = Modifier.width(SpacingTokens.sm))

            // Comment content
            Column(modifier = Modifier.weight(1f)) {
                // User info and timestamp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = comment.username,
                            style = if (isNested) {
                                MaterialTheme.typography.bodyMedium
                            } else {
                                MaterialTheme.typography.titleSmall
                            },
                            fontWeight = FontWeight.SemiBold,
                        )

                        Spacer(modifier = Modifier.width(SpacingTokens.sm))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(modifier = Modifier.width(SpacingTokens.xs))
                            Text(
                                text = formatTimeAgo(comment.createdAt),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    // More options menu
                    Box {
                        IconButton(
                            onClick = { showMoreMenu = true },
                            modifier = Modifier.size(24.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options",
                                modifier = Modifier.size(16.dp),
                            )
                        }

                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Report") },
                                onClick = {
                                    onMoreClick(comment)
                                    showMoreMenu = false
                                },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(SpacingTokens.xs))

                // Comment text
                Text(
                    text = comment.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(SpacingTokens.sm))

                // Action buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.md),
                ) {
                    // Upvote button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = if (isLoggedIn) {
                            Modifier
                                .clip(RoundedCornerShape(QodeCorners.sm))
                                .clickable { onUpvoteClick(comment) }
                                .background(
                                    if (comment.isUpvoted) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    },
                                )
                                .padding(horizontal = SpacingTokens.sm, vertical = SpacingTokens.xs)
                        } else {
                            Modifier
                        },
                    ) {
                        Icon(
                            imageVector = if (comment.isUpvoted) {
                                QodeActionIcons.Thumbs
                            } else {
                                QodeActionIcons.Thumbs
                            },
                            contentDescription = "Upvote",
                            tint = if (comment.isUpvoted) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(16.dp),
                        )

                        if (comment.upvotes > 0) {
                            Spacer(modifier = Modifier.width(SpacingTokens.xs))
                            Text(
                                text = comment.upvotes.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (comment.isUpvoted) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                    }

                    // Reply button
                    if (isLoggedIn && canNestFurther) {
                        TextButton(
                            onClick = { onReplyClick(comment) },
                            contentPadding = PaddingValues(
                                horizontal = SpacingTokens.sm,
                                vertical = SpacingTokens.xs,
                            ),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Reply,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(SpacingTokens.xs))
                            Text(
                                text = "Reply",
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }

                    // Show/hide replies button
                    if (hasReplies) {
                        TextButton(
                            onClick = { showReplies = !showReplies },
                            contentPadding = PaddingValues(
                                horizontal = SpacingTokens.sm,
                                vertical = SpacingTokens.xs,
                            ),
                        ) {
                            Icon(
                                imageVector = if (showReplies) {
                                    Icons.Default.ExpandLess
                                } else {
                                    Icons.Default.ExpandMore
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(SpacingTokens.xs))
                            Text(
                                text = "${comment.replies.size} ${if (comment.replies.size == 1) "reply" else "replies"}",
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                }
            }
        }

        // Replies
        if (hasReplies && canNestFurther) {
            AnimatedVisibility(
                visible = showReplies,
                enter = expandVertically(animationSpec = tween(QodeAnimation.MEDIUM)),
                exit = shrinkVertically(animationSpec = tween(QodeAnimation.MEDIUM)),
            ) {
                Column {
                    comment.replies.forEach { reply ->
                        CommentCard(
                            comment = reply,
                            onUpvoteClick = onUpvoteClick,
                            onReplyClick = onReplyClick,
                            onMoreClick = onMoreClick,
                            isLoggedIn = isLoggedIn,
                            depth = depth + 1,
                            maxDepth = maxDepth,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Comment input component for writing new comments
 */
@Composable
fun CommentInput(
    onSubmitComment: (String) -> Unit,
    modifier: Modifier = Modifier,
    isLoggedIn: Boolean = false,
    placeholder: String = "Write a comment...",
    replyingTo: String? = null,
    onCancelReply: (() -> Unit)? = null
) {
    var commentText by remember { mutableStateOf("") }

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = if (replyingTo != null) 2.dp else 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(SpacingTokens.md),
        ) {
            // Replying to indicator
            replyingTo?.let { username ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Replying to @$username",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )

                    onCancelReply?.let { cancel ->
                        TextButton(onClick = cancel) {
                            Text("Cancel")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(SpacingTokens.sm))
            }

            if (isLoggedIn) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
                ) {
                    QodeAvatar(
                        text = "ME",
                        size = 40.dp,
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        QodeTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            variant = QodeTextFieldVariant.Multiline,
                            placeholder = placeholder,
                            maxLines = 4,
                        )

                        Spacer(modifier = Modifier.height(SpacingTokens.sm))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            QodeButton(
                                onClick = {
                                    if (commentText.isNotBlank()) {
                                        onSubmitComment(commentText.trim())
                                        commentText = ""
                                    }
                                },
                                text = "Post Comment",
                                variant = QodeButtonVariant.Primary,
                                size = QodeButtonSize.Small,
                                enabled = commentText.isNotBlank(),
                            )
                        }
                    }
                }
            } else {
                // Login prompt
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(QodeCorners.md),
                        )
                        .padding(SpacingTokens.md),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "Join the conversation",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                        )
                        Spacer(modifier = Modifier.height(SpacingTokens.xs))
                        Text(
                            text = "Log in to comment and upvote",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(SpacingTokens.sm))
                        QodeButton(
                            onClick = { /* Navigate to login */ },
                            text = "Log In",
                            variant = QodeButtonVariant.Primary,
                            size = QodeButtonSize.Small,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Comments list component
 */
@Composable
fun CommentsList(
    comments: List<Comment>,
    onUpvoteClick: (Comment) -> Unit,
    onReplyClick: (Comment) -> Unit,
    onMoreClick: (Comment) -> Unit,
    modifier: Modifier = Modifier,
    isLoggedIn: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    if (comments.isEmpty()) {
        QodeEmptyState(
            icon = Icons.Default.Reply,
            title = "No comments yet",
            description = "Be the first to share your thoughts!",
        )
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        ) {
            items(comments) { comment ->
                CommentCard(
                    comment = comment,
                    onUpvoteClick = onUpvoteClick,
                    onReplyClick = onReplyClick,
                    onMoreClick = onMoreClick,
                    isLoggedIn = isLoggedIn,
                )
            }
        }
    }
}

// Helper function
private fun formatTimeAgo(dateTime: LocalDateTime): String {
    val now = LocalDateTime.now()
    val minutes = ChronoUnit.MINUTES.between(dateTime, now)
    val hours = ChronoUnit.HOURS.between(dateTime, now)
    val days = ChronoUnit.DAYS.between(dateTime, now)

    return when {
        minutes < 1 -> "now"
        minutes < 60 -> "${minutes}m"
        hours < 24 -> "${hours}h"
        days < 7 -> "${days}d"
        days < 30 -> "${days / 7}w"
        else -> "${days / 30}mo"
    }
}

// Sample data for preview
private fun getSampleComments(): List<Comment> =
    listOf(
        Comment(
            id = "1",
            promoCodeId = "promo1",
            userId = "user1",
            username = "AkezhanB",
            content = "This code worked perfectly! Saved 15,000 KZT on my laptop purchase. Thanks for sharing!",
            upvotes = 12,
            isUpvoted = true,
            createdAt = LocalDateTime.now().minusHours(2),
            replies = listOf(
                Comment(
                    id = "2",
                    promoCodeId = "promo1",
                    userId = "user2",
                    username = "MaratK",
                    content = "Which laptop did you get? I'm looking for a good deal too.",
                    upvotes = 3,
                    createdAt = LocalDateTime.now().minusHours(1),
                    replies = listOf(
                        Comment(
                            id = "3",
                            promoCodeId = "promo1",
                            userId = "user1",
                            username = "AkezhanB",
                            content = "Got the MacBook Air M2. Check their electronics section!",
                            upvotes = 1,
                            createdAt = LocalDateTime.now().minusMinutes(30),
                        ),
                    ),
                ),
            ),
        ),
        Comment(
            id = "4",
            promoCodeId = "promo1",
            userId = "user3",
            username = "DinaraS",
            content = "Code expired yesterday, but the store support helped me get the discount anyway. Great customer service!",
            upvotes = 8,
            createdAt = LocalDateTime.now().minusHours(6),
        ),
    )

// Preview
@Preview(name = "CommentCard", showBackground = true)
@Composable
private fun CommentCardPreview() {
    QodeTheme {
        Column(
            modifier = Modifier.padding(SpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        ) {
            val sampleComments = getSampleComments()

            CommentsList(
                comments = sampleComments,
                onUpvoteClick = {},
                onReplyClick = {},
                onMoreClick = {},
                isLoggedIn = true,
            )

            Spacer(modifier = Modifier.height(SpacingTokens.md))

            CommentInput(
                onSubmitComment = {},
                isLoggedIn = true,
            )
        }
    }
}
