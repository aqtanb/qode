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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.designsystem.theme.extendedColorScheme
import com.qodein.shared.model.PromoCode
import com.qodein.shared.model.VoteState

@Composable
fun ActionButtonsSection(
    promoCode: PromoCode,
    isUpvotedByCurrentUser: Boolean,
    isDownvotedByCurrentUser: Boolean,
    showVoteAnimation: Boolean,
    lastVoteType: VoteState?,
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
            ActionButton(
                type = ActionButtonType.Upvote,
                icon = QodeActionIcons.Thumbs,
                count = promoCode.upvotes,
                isUpvoted = isUpvotedByCurrentUser,
                showPositiveFeedback = showVoteAnimation && lastVoteType == VoteState.UPVOTE,
                activeColor = MaterialTheme.extendedColorScheme.success,
                onClick = onUpvoteClicked,
                modifier = Modifier.weight(1f),
            )
            ActionButton(
                type = ActionButtonType.Downvote,
                icon = QodeActionIcons.ThumbsDown,
                count = promoCode.downvotes,
                isDownvoted = isDownvotedByCurrentUser,
                showPositiveFeedback = showVoteAnimation && lastVoteType == VoteState.DOWNVOTE,
                activeColor = MaterialTheme.colorScheme.error,
                onClick = onDownvoteClicked,
                modifier = Modifier.weight(1f),
            )

            // Comment Button
            ActionButton(
                type = ActionButtonType.Comment,
                icon = QodeActionIcons.Comment,
                count = null,
                activeColor = MaterialTheme.colorScheme.tertiary,
                onClick = onCommentsClicked,
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
    count: Int? = null,
    isUpvoted: Boolean = false,
    isDownvoted: Boolean = false,
    showPositiveFeedback: Boolean = false,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current

    // Clear, simple states
    val isActive = when (type) {
        ActionButtonType.Upvote -> isUpvoted
        ActionButtonType.Downvote -> isDownvoted
        else -> false
    }

    // Clear labels
    val label = when (type) {
        ActionButtonType.Upvote -> "Like"
        ActionButtonType.Downvote -> "Dislike"
        ActionButtonType.Comment -> "Comment"
        ActionButtonType.Share -> "Share"
    }

    // Status descriptions
    val statusText = when (type) {
        ActionButtonType.Upvote -> if (isUpvoted) "Liked" else "Like"
        ActionButtonType.Downvote -> if (isDownvoted) "Disliked" else "Dislike"
        ActionButtonType.Comment -> "Comment"
        ActionButtonType.Share -> "Share"
    }

    // Satisfying positive feedback animation
    val scale by animateFloatAsState(
        targetValue = if (showPositiveFeedback) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "positive_feedback_scale",
    )

    val iconColor by animateColorAsState(
        targetValue = when {
            showPositiveFeedback -> activeColor // Bright during positive feedback
            isActive -> activeColor // Bright when active
            else -> MaterialTheme.colorScheme.onSurfaceVariant // Muted when inactive
        },
        animationSpec = spring(dampingRatio = 0.7f),
        label = "icon_color",
    )

    val backgroundColor by animateColorAsState(
        targetValue = when {
            showPositiveFeedback -> activeColor.copy(alpha = 0.3f) // Vibrant during positive feedback
            isActive -> activeColor.copy(alpha = 0.2f) // Colored when active
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) // Neutral when inactive
        },
        animationSpec = spring(dampingRatio = 0.7f),
        label = "background_color",
    )

    val textColor by animateColorAsState(
        targetValue = when {
            showPositiveFeedback -> activeColor // Bright during positive feedback
            isActive -> activeColor // Bright when active
            else -> MaterialTheme.colorScheme.onSurfaceVariant // Muted when inactive
        },
        animationSpec = spring(dampingRatio = 0.7f),
        label = "text_color",
    )

    Surface(
        onClick = {
            // Satisfying haptic feedback
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        shape = RoundedCornerShape(SpacingTokens.md),
        color = backgroundColor,
        modifier = modifier
            .scale(scale)
            .semantics {
                role = Role.Button
                contentDescription = "$statusText${count?.let { ", $it" } ?: ""}"
            },
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(SpacingTokens.md),
        ) {
            // Clear, always-visible icon
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp),
            )

            // Clear count display
            Text(
                text = when {
                    count != null && count > 0 -> formatCount(count)
                    else -> "0"
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
