package com.qodein.feature.promocode.detail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.component.CircularImage
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.preview.PromocodePreviewData
import com.qodein.core.ui.util.formatNumber
import com.qodein.core.ui.util.rememberFormattedRelativeTime
import com.qodein.feature.promocode.R
import com.qodein.shared.model.Discount
import com.qodein.shared.model.Promocode

@Composable
fun PromocodeInfo(
    promocode: Promocode,
    modifier: Modifier = Modifier,
    voteScoreOverride: Int? = null
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
    ) {
        PromocodeHeader(promocode, voteScoreOverride = voteScoreOverride)
        PromocodeDescription(promocode, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun PromocodeHeader(
    promocode: Promocode,
    modifier: Modifier = Modifier,
    voteScoreOverride: Int? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularImage(
            imageUrl = promocode.authorAvatarUrl,
            fallbackText = promocode.authorUsername ?: "",
            fallbackIcon = QodeNavigationIcons.Profile,
            size = SizeTokens.Avatar.sizeSmall,
            contentDescription = promocode.authorUsername ?: "",
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.xxs),
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = promocode.authorUsername ?: "",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
        ) {
            val voteScore = voteScoreOverride ?: promocode.voteScore
            Text(
                text = if (voteScore > 0) "+$voteScore" else voteScore.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
            )
            Text(
                text = rememberFormattedRelativeTime(promocode.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
            )
        }
    }
}

@Composable
private fun PromocodeDescription(
    promocode: Promocode,
    modifier: Modifier = Modifier
) {
    val discountText = when (val discount = promocode.discount) {
        is Discount.FixedAmount -> stringResource(
            id = R.string.promocode_fixed_amount_discount,
            formatNumber(discount.value),
            formatNumber(promocode.minimumOrderAmount),
        )
        is Discount.Percentage -> stringResource(
            id = R.string.promocode_percentage_discount,
            formatNumber(discount.value),
            formatNumber(promocode.minimumOrderAmount),
        )
    }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs)) {
        Text(
            text = discountText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        promocode.description?.takeIf { it.isNotBlank() }?.let { description ->
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@ThemePreviews
@Composable
private fun PromocodeHeaderPreview() {
    QodeTheme {
        val samplePromoCode = PromocodePreviewData.percentagePromocode

        PromocodeInfo(
            promocode = samplePromoCode,
            voteScoreOverride = 5,
        )
    }
}
