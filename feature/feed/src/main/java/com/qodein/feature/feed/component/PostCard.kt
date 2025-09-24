package com.qodein.feature.feed.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qodein.core.designsystem.component.CircularImage
import com.qodein.core.designsystem.component.QodeButtonSize
import com.qodein.core.designsystem.component.QodeButtonVariant
import com.qodein.core.designsystem.component.QodeCard
import com.qodein.core.designsystem.component.QodeCardVariant
import com.qodein.core.designsystem.component.QodeIconButton
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.theme.AnimationTokens
import com.qodein.core.designsystem.theme.MotionTokens
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

// MARK: - Constants

private object PostCardTokens {
    val minTouchTarget = 48.dp
    val avatarSize = SizeTokens.Avatar.sizeMedium
    val onlineIndicatorSize = SizeTokens.Decoration.sizeXSmall
    val actionButtonSize = SizeTokens.IconButton.sizeMedium
    val hotPostThreshold = 50
    val maxContentLines = 4
    val engagementThreshold = 25
    val contentPadding = PaddingValues(SpacingTokens.lg)
    val tagHeight = SizeTokens.Chip.height
}

/**
 * Beautiful modern post card with clean, readable design
 * Uses QodeCard and design system tokens for consistency
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

    // Subtle animations for engagement
    val likeButtonScale by animateFloatAsState(
        targetValue = if (post.isUpvotedByCurrentUser) MotionTokens.Scale.HOVER else 1.0f,
        animationSpec = MotionTokens.Easing.emphasized,
        label = "like_button_scale",
    )

    val likeButtonColor by animateColorAsState(
        targetValue = if (post.isUpvotedByCurrentUser) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = AnimationTokens.Spec.medium(),
        label = "like_button_color",
    )

    QodeCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = SpacingTokens.sm,
                vertical = SpacingTokens.xs,
            ),
        variant = QodeCardVariant.Elevated,
        shape = RoundedCornerShape(ShapeTokens.Corner.large),
        contentPadding = PostCardTokens.contentPadding,
    ) {
        // Author info with clean design
        PostAuthorInfo(
            post = post,
            onUserClick = onUserClick,
            interactionSource = interactionSource,
        )

        Spacer(modifier = Modifier.height(SpacingTokens.md))

        // Clean, readable post content
        PostContent(
            content = post.content,
            modifier = Modifier.fillMaxWidth(),
        )

        // Clean, readable tags
        if (post.tags.isNotEmpty()) {
            PostTags(
                tags = post.tags,
                onTagClick = onTagClick,
                modifier = Modifier.padding(top = SpacingTokens.md),
            )
        }

        Spacer(modifier = Modifier.height(SpacingTokens.md))

        // Clean action buttons
        PostActions(
            post = post,
            onLikeClick = onLikeClick,
            onCommentClick = onCommentClick,
            onShareClick = onShareClick,
            likeButtonScale = likeButtonScale,
            likeButtonColor = likeButtonColor,
        )
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

// MARK: - Component Parts

@Composable
private fun PostAuthorInfo(
    post: Post,
    onUserClick: (String) -> Unit,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Smart avatar with fallbacks
        Box {
            CircularImage(
                imageUrl = post.authorAvatarUrl,
                fallbackText = post.authorUsername,
                fallbackIcon = QodeNavigationIcons.Profile,
                size = PostCardTokens.avatarSize,
                contentDescription = "Avatar of ${post.authorUsername}",
                modifier = Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                ) { onUserClick(post.authorUsername) },
            )

            // Online indicator
            Box(
                modifier = Modifier
                    .size(PostCardTokens.onlineIndicatorSize)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .align(Alignment.BottomEnd),
            )
        }

        Spacer(modifier = Modifier.width(SpacingTokens.md))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "@${post.authorUsername}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                ) { onUserClick(post.authorUsername) },
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
            ) {
                Text(
                    text = formatTimeAgo(post.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // Hot post indicator
                if (post.voteScore > PostCardTokens.hotPostThreshold) {
                    Surface(
                        shape = RoundedCornerShape(ShapeTokens.Corner.full),
                        color = MaterialTheme.colorScheme.errorContainer,
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = SpacingTokens.sm,
                                vertical = SpacingTokens.xxxs,
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xxxs),
                        ) {
                            Text(
                                text = "🔥",
                                style = MaterialTheme.typography.labelSmall,
                            )
                            Text(
                                text = "Hot",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                ),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PostContent(
    content: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = content,
        style = MaterialTheme.typography.bodyLarge.copy(
            lineHeight = 24.sp,
        ),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier,
        maxLines = PostCardTokens.maxContentLines,
        overflow = TextOverflow.Ellipsis,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PostTags(
    tags: List<Tag>,
    onTagClick: (Tag) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
    ) {
        tags.forEach { tag ->
            Surface(
                onClick = { onTagClick(tag) },
                shape = RoundedCornerShape(ShapeTokens.Corner.full),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.height(PostCardTokens.tagHeight),
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = SpacingTokens.md,
                        vertical = SpacingTokens.xs,
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
                ) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiary),
                    )

                    Text(
                        text = "#${tag.name}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium,
                        ),
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun PostActions(
    post: Post,
    onLikeClick: (PostId) -> Unit,
    onCommentClick: (PostId) -> Unit,
    onShareClick: (PostId) -> Unit,
    likeButtonScale: Float,
    likeButtonColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Like action with animation
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(ShapeTokens.Corner.medium))
                .clickable { onLikeClick(post.id) }
                .padding(SpacingTokens.xs),
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

        // Comment action
        QodeIconButton(
            onClick = { onCommentClick(post.id) },
            icon = QodeActionIcons.Comment,
            contentDescription = "Comments",
            variant = QodeButtonVariant.Text,
            size = QodeButtonSize.Small,
        )

        // Share action with counter
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(ShapeTokens.Corner.medium))
                .clickable { onShareClick(post.id) }
                .padding(SpacingTokens.xs),
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

// MARK: - Previews

@Preview(name = "PostCard - Standard", showBackground = true)
@Composable
private fun PostCardStandardPreview() {
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

@Preview(name = "PostCard - Hot Post", showBackground = true)
@Composable
private fun PostCardHotPreview() {
    QodeTheme {
        val mockPost = Post(
            id = PostId("hot_post"),
            authorId = UserId("tech_hunter"),
            authorUsername = "tech_hunter",
            authorAvatarUrl = "https://picsum.photos/seed/tech_hunter/150/150",
            content = "🚀 This new streaming service is absolutely incredible! The interface is clean, " +
                "the content quality is top-notch, and the pricing beats all competitors. " +
                "Highly recommend checking this out if you're looking to upgrade your streaming setup!",
            tags = listOf(
                Tag.create("streaming"),
                Tag.create("review"),
                Tag.create("hot"),
                Tag.create("recommendation"),
            ),
            upvotes = 127,
            downvotes = 2,
            shares = 15,
            createdAt = Clock.System.now(),
            isUpvotedByCurrentUser = false,
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

@Preview(name = "PostCard - No Avatar", showBackground = true)
@Composable
private fun PostCardNoAvatarPreview() {
    QodeTheme {
        val mockPost = Post(
            id = PostId("no_avatar_post"),
            authorId = UserId("anon_user"),
            authorUsername = "anonymous_deals",
            authorAvatarUrl = null,
            content = "Found a great discount code for mobile subscriptions. " +
                "Anyone know if this service is reliable? Looking for feedback before I commit.",
            tags = listOf(
                Tag.create("mobile"),
                Tag.create("question"),
            ),
            upvotes = 18,
            downvotes = 1,
            shares = 2,
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

@Preview(name = "PostCard - No Tags", showBackground = true)
@Composable
private fun PostCardNoTagsPreview() {
    QodeTheme {
        val mockPost = Post(
            id = PostId("no_tags_post"),
            authorId = UserId("simple_user"),
            authorUsername = "minimalist",
            authorAvatarUrl = "https://picsum.photos/seed/minimalist/150/150",
            content = "Sometimes the best deals are the simplest ones. " +
                "Just sharing a quick tip about finding great subscription offers.",
            tags = emptyList(),
            upvotes = 7,
            downvotes = 0,
            shares = 1,
            createdAt = Clock.System.now(),
            isUpvotedByCurrentUser = false,
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

@Preview(name = "PostCard - Long Content", showBackground = true)
@Composable
private fun PostCardLongContentPreview() {
    QodeTheme {
        val mockPost = Post(
            id = PostId("long_content_post"),
            authorId = UserId("detailed_user"),
            authorUsername = "comprehensive_reviewer",
            authorAvatarUrl = null,
            content = "Here's a comprehensive breakdown of the top subscription services available right now. " +
                "I've been testing these for months and can provide detailed insights on pricing, features, " +
                "user experience, content quality, customer support, and overall value for money. " +
                "The results might surprise you, especially regarding some lesser-known services that " +
                "outperform the major players in specific categories. Let me know if you want more details!",
            tags = listOf(
                Tag.create("comprehensive"),
                Tag.create("review"),
                Tag.create("comparison"),
                Tag.create("detailed"),
                Tag.create("analysis"),
            ),
            upvotes = 89,
            downvotes = 4,
            shares = 12,
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

@PreviewLightDark
@Composable
private fun PostCardDarkThemePreview() {
    QodeTheme {
        val mockPost = Post(
            id = PostId("dark_theme_post"),
            authorId = UserId("theme_tester"),
            authorUsername = "ui_enthusiast",
            authorAvatarUrl = "https://picsum.photos/seed/ui_enthusiast/150/150",
            content = "Testing how the PostCard looks in dark theme. " +
                "The contrast and readability should be excellent across all UI elements.",
            tags = listOf(
                Tag.create("ui"),
                Tag.create("testing"),
                Tag.create("design"),
            ),
            upvotes = 34,
            downvotes = 1,
            shares = 3,
            createdAt = Clock.System.now(),
            isUpvotedByCurrentUser = false,
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
