package com.qodein.feature.promocode.detail.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.preview.PromoCodePreviewData
import com.qodein.shared.model.PromoCode

@Composable
fun StatisticsSection(
    promoCode: PromoCode,
    showVoteAnimation: Boolean,
    modifier: Modifier = Modifier
) {
    // Calculate vote score
    val voteScore = promoCode.upvotes - promoCode.downvotes
    val voteScoreColor by animateColorAsState(
        targetValue = when {
            voteScore > 0 -> MaterialTheme.colorScheme.primary // Green for positive
            voteScore < 0 -> MaterialTheme.colorScheme.error // Red for negative
            else -> MaterialTheme.colorScheme.onSurfaceVariant // Neutral
        },
        animationSpec = tween(durationMillis = 300),
        label = "vote_score_color",
    )

    // Animate numbers when voting
    val animatedUpvotes by animateIntAsState(
        targetValue = promoCode.upvotes,
        animationSpec = tween(durationMillis = 500),
        label = "animated_upvotes",
    )

    val animatedDownvotes by animateIntAsState(
        targetValue = promoCode.downvotes,
        animationSpec = tween(durationMillis = 500),
        label = "animated_downvotes",
    )

    // Horizontal statistics row like in reference image
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SpacingTokens.md, vertical = SpacingTokens.sm),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Upvotes
        StatColumn(
            value = formatNumber(animatedUpvotes),
            label = "Upvotes",
        )

        // Downvotes
        StatColumn(
            value = formatNumber(animatedDownvotes),
            label = "Downvotes",
        )

        // Shares
        StatColumn(
            value = formatNumber(promoCode.shares),
            label = "Shares",
        )
    }
}

@Composable
private fun StatColumn(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

// Helper function to format numbers
private fun formatNumber(number: Int): String =
    when {
        number >= 1_000_000 -> "${(number / 1_000_000.0).let {
            if (it == it.toInt().toDouble()) it.toInt().toString() else "%.1f".format(it)
        }}M"
        number >= 1_000 -> "${(number / 1_000.0).let {
            if (it == it.toInt().toDouble()) it.toInt().toString() else "%.1f".format(it)
        }}K"
        else -> number.toString()
    }

@Preview
@Composable
private fun StatisticsSectionPreview() {
    QodeTheme {
        val samplePromoCode = PromoCodePreviewData.percentagePromoCode

        Surface {
            StatisticsSection(
                promoCode = samplePromoCode,
                showVoteAnimation = false,
            )
        }
    }
}
