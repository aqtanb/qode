package com.qodein.feature.promocode.detail.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.designsystem.theme.extendedColorScheme
import com.qodein.feature.promocode.detail.VoteType
import com.qodein.shared.model.PromoCode
import com.qodein.shared.model.PromoCodeId
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

@Composable
fun ActionButtonsSection(
    promoCode: PromoCode,
    isVoting: Boolean,
    showVoteAnimation: Boolean,
    lastVoteType: VoteType?,
    isSharing: Boolean,
    onUpvoteClicked: () -> Unit,
    onDownvoteClicked: () -> Unit,
    onShareClicked: () -> Unit,
    onCommentsClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SpacingTokens.lg),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(SpacingTokens.md),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        ) {
            // Upvote Button
            ActionButton(
                type = ActionButtonType.Upvote,
                icon = QodeActionIcons.Thumbs,
                count = promoCode.upvotes,
                isActive = promoCode.isUpvotedByCurrentUser,
                isLoading = isVoting && lastVoteType == VoteType.UPVOTE,
                showAnimation = showVoteAnimation && lastVoteType == VoteType.UPVOTE,
                activeColor = MaterialTheme.extendedColorScheme.success,
                onClick = onUpvoteClicked,
                modifier = Modifier.weight(1f),
            )

            // Downvote Button
            ActionButton(
                type = ActionButtonType.Downvote,
                icon = QodeActionIcons.ThumbsDown,
                count = promoCode.downvotes,
                isActive = promoCode.isDownvotedByCurrentUser,
                isLoading = isVoting && lastVoteType == VoteType.DOWNVOTE,
                showAnimation = showVoteAnimation && lastVoteType == VoteType.DOWNVOTE,
                activeColor = MaterialTheme.colorScheme.error,
                onClick = onDownvoteClicked,
                modifier = Modifier.weight(1f),
            )

            // Comment Button
            ActionButton(
                type = ActionButtonType.Comment,
                icon = QodeActionIcons.Comment,
                count = null,
                isActive = false,
                isLoading = false,
                showAnimation = false,
                activeColor = MaterialTheme.colorScheme.tertiary,
                onClick = onCommentsClicked,
                modifier = Modifier.weight(1f),
            )

            // Share Button
            ActionButton(
                type = ActionButtonType.Share,
                icon = QodeActionIcons.Share,
                count = promoCode.shares,
                isActive = false,
                isLoading = isSharing,
                showAnimation = isSharing,
                activeColor = MaterialTheme.colorScheme.primary,
                onClick = onShareClicked,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private enum class ActionButtonType {
    Upvote,
    Downvote,
    Comment,
    Share
}

@Composable
private fun ActionButton(
    type: ActionButtonType,
    icon: ImageVector,
    count: Int?,
    isActive: Boolean,
    isLoading: Boolean,
    showAnimation: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    val label = when (type) {
        ActionButtonType.Upvote -> "Like"
        ActionButtonType.Downvote -> "Dislike"
        ActionButtonType.Comment -> "Comment"
        ActionButtonType.Share -> "Share"
    }

    // Enhanced animations with spring physics
    val scale by animateFloatAsState(
        targetValue = when {
            showAnimation -> 1.1f
            isLoading -> 0.95f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "button_scale",
    )

    val iconColor by animateColorAsState(
        targetValue = if (isActive) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = spring(dampingRatio = 0.8f),
        label = "icon_color",
    )

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isActive -> activeColor.copy(alpha = 0.15f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        },
        animationSpec = spring(dampingRatio = 0.8f),
        label = "background_color",
    )

    val textColor by animateColorAsState(
        targetValue = if (isActive) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = spring(dampingRatio = 0.8f),
        label = "text_color",
    )

    Surface(
        onClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        shape = RoundedCornerShape(SpacingTokens.md),
        color = backgroundColor,
        modifier = modifier
            .scale(scale)
            .alpha(if (isLoading) 0.7f else 1f)
            .semantics {
                role = Role.Button
                contentDescription = "$label${count?.let { " ($it)" } ?: ""}"
            },
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(SpacingTokens.md),
        ) {
            // Icon with loading state
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = iconColor,
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp),
                )
            }

            // Always show count-style text for consistent height
            Text(
                text = when {
                    count != null && count > 0 -> formatCount(count)
                    else -> "0" // Show "0" instead of full labels for consistency
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                color = textColor,
                modifier = Modifier.padding(top = SpacingTokens.xs),
            )
        }
    }
}

// Helper function to format large numbers
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
private fun ActionButtonsPreview() {
    QodeTheme {
        val samplePromoCode = PromoCode.PercentagePromoCode(
            id = PromoCodeId("SAMPLE_ID"),
            code = "FALL60",
            serviceName = "Food Delivery Pro",
            category = "Food",
            discountPercentage = 51.0,
            minimumOrderAmount = 5000.0,
            startDate = Clock.System.now(),
            endDate = Clock.System.now().plus(7.days),
            upvotes = 1250,
            downvotes = 28,
            shares = 326,
            views = 5420,
            isUpvotedByCurrentUser = true,
            isDownvotedByCurrentUser = false,
            isBookmarkedByCurrentUser = false,
        )

        Column(
            modifier = Modifier.padding(SpacingTokens.lg),
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
        ) {
            Text(
                "Action Buttons Examples",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            Text(
                "Active State (Upvoted):",
                style = MaterialTheme.typography.titleMedium,
            )
            ActionButtonsSection(
                promoCode = samplePromoCode,
                isVoting = false,
                showVoteAnimation = false,
                lastVoteType = null,
                isSharing = false,
                onUpvoteClicked = {},
                onDownvoteClicked = {},
                onShareClicked = {},
                onCommentsClicked = {},
            )

            Text(
                "Neutral State:",
                style = MaterialTheme.typography.titleMedium,
            )
            ActionButtonsSection(
                promoCode = samplePromoCode.copy(
                    isUpvotedByCurrentUser = false,
                    isDownvotedByCurrentUser = false,
                    upvotes = 42,
                    shares = 8,
                ),
                isVoting = false,
                showVoteAnimation = false,
                lastVoteType = null,
                isSharing = false,
                onUpvoteClicked = {},
                onDownvoteClicked = {},
                onShareClicked = {},
                onCommentsClicked = {},
            )

            Text(
                "Loading State:",
                style = MaterialTheme.typography.titleMedium,
            )
            ActionButtonsSection(
                promoCode = samplePromoCode.copy(
                    isUpvotedByCurrentUser = false,
                    isDownvotedByCurrentUser = false,
                ),
                isVoting = true,
                showVoteAnimation = false,
                lastVoteType = VoteType.UPVOTE,
                isSharing = true,
                onUpvoteClicked = {},
                onDownvoteClicked = {},
                onShareClicked = {},
                onCommentsClicked = {},
            )
        }
    }
}
