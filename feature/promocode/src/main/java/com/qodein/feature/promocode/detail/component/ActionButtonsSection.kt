package com.qodein.feature.promocode.detail.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.component.QodeButtonSize
import com.qodein.core.designsystem.component.QodeButtonVariant
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.detail.VoteType
import com.qodein.shared.model.PromoCode
import com.qodein.shared.model.PromoCodeId
import kotlinx.datetime.Clock
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
    val voteScore = promoCode.upvotes - promoCode.downvotes

    // Animation scales
    val upvoteScale by animateFloatAsState(
        targetValue = if (showVoteAnimation && lastVoteType == VoteType.UPVOTE) 1.2f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "upvote_scale",
    )

    val downvoteScale by animateFloatAsState(
        targetValue = if (showVoteAnimation && lastVoteType == VoteType.DOWNVOTE) 1.2f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "downvote_scale",
    )

    val shareScale by animateFloatAsState(
        targetValue = if (isSharing) 0.9f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "share_scale",
    )

    // Vote score color
    val voteScoreColor by animateColorAsState(
        targetValue = when {
            voteScore > 0 -> MaterialTheme.colorScheme.primary // Green
            voteScore < 0 -> MaterialTheme.colorScheme.error // Red
            else -> MaterialTheme.colorScheme.onSurfaceVariant // Neutral
        },
        animationSpec = tween(durationMillis = 300),
        label = "vote_score_color",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(SpacingTokens.md),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
    ) {
        // Vote Buttons Row - flat design
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        ) {
            // Upvote Button
            VoteButton(
                icon = QodeActionIcons.Thumbs,
                label = "Upvote",
                count = promoCode.upvotes,
                isActive = promoCode.isUpvotedByCurrentUser,
                isLoading = isVoting && lastVoteType == VoteType.UPVOTE,
                color = MaterialTheme.colorScheme.primary,
                onClick = onUpvoteClicked,
                modifier = Modifier
                    .weight(1f)
                    .scale(upvoteScale),
            )

            // Downvote Button
            VoteButton(
                icon = QodeActionIcons.ThumbsDown,
                label = "Downvote",
                count = promoCode.downvotes,
                isActive = promoCode.isDownvotedByCurrentUser,
                isLoading = isVoting && lastVoteType == VoteType.DOWNVOTE,
                color = MaterialTheme.colorScheme.error,
                onClick = onDownvoteClicked,
                modifier = Modifier
                    .weight(1f)
                    .scale(downvoteScale),
            )
        }

        // Share and Comments Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        ) {
            // Share Button
            QodeButton(
                text = "Share",
                onClick = onShareClicked,
                variant = QodeButtonVariant.Secondary,
                size = QodeButtonSize.Medium,
                leadingIcon = QodeActionIcons.Share,
                loading = isSharing,
                modifier = Modifier
                    .weight(1f)
                    .scale(shareScale),
            )

            // Comments Button
            QodeButton(
                text = "Comments",
                onClick = onCommentsClicked,
                variant = QodeButtonVariant.Secondary,
                size = QodeButtonSize.Medium,
                leadingIcon = QodeActionIcons.Comment,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun VoteButton(
    icon: ImageVector,
    label: String,
    count: Int,
    isActive: Boolean,
    isLoading: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonColor = if (isActive) color else MaterialTheme.colorScheme.onSurfaceVariant
    val backgroundColor = if (isActive) color.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(ShapeTokens.Corner.medium),
        color = backgroundColor,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(SpacingTokens.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = buttonColor,
                modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
            )

            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = buttonColor,
            )
        }
    }
}

@Preview
@Composable
private fun ActionButtonsSectionPreview() {
    QodeTheme {
        val samplePromoCode = PromoCode.PercentagePromoCode(
            id = PromoCodeId("SAMPLE_ID"),
            code = "FALL60",
            serviceName = "Food Delivery Pro",
            category = "Food",
            title = "51% Off Food Orders",
            discountPercentage = 51.0,
            minimumOrderAmount = 5000.0,
            startDate = Clock.System.now(),
            endDate = Clock.System.now().plus(7.days),
            upvotes = 51,
            downvotes = 28,
            shares = 26,
            isUpvotedByCurrentUser = true,
            isDownvotedByCurrentUser = false,
        )

        Surface {
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
        }
    }
}
