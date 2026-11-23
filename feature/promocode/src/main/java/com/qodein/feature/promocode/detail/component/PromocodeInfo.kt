package com.qodein.feature.promocode.detail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.qodein.core.designsystem.component.QodeinAsyncImage
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.component.ContentImage
import com.qodein.core.ui.preview.PromocodePreviewData
import com.qodein.core.ui.util.formatNumber
import com.qodein.core.ui.util.rememberFormattedRelativeTime
import com.qodein.feature.promocode.R
import com.qodein.shared.model.Discount
import com.qodein.shared.model.Promocode
import com.qodein.core.ui.R as CoreUiR

@Composable
fun PromocodeInfo(
    promocode: Promocode,
    displayVoteScore: Int,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
    ) {
        PromocodeHeader(promocode, displayVoteScore = displayVoteScore)
        PromocodeDescription(promocode, modifier = Modifier.fillMaxWidth())

        if (promocode.imageUrls.isNotEmpty()) {
            PromocodeImagePager(
                imageUrls = promocode.imageUrls,
                onImageClick = onImageClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun PromocodeHeader(
    promocode: Promocode,
    displayVoteScore: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val authorName = promocode.authorUsername ?: stringResource(CoreUiR.string.user_anonymous)
        QodeinAsyncImage(
            imageUrl = promocode.authorAvatarUrl ?: "",
            fallbackText = authorName,
            size = SizeTokens.Avatar.sizeSmall,
            contentDescription = authorName,
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.xxs),
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = authorName,
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
            Text(
                text = "$displayVoteScore",
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
    val minimumOrder = promocode.minimumOrderAmount.takeIf { it > 0 }?.let { formatNumber(it) }
    val discountText = when (val discount = promocode.discount) {
        is Discount.FixedAmount -> if (minimumOrder != null) {
            stringResource(R.string.promocode_fixed_amount_discount, formatNumber(discount.value), minimumOrder)
        } else {
            stringResource(R.string.promocode_fixed_amount_discount_no_minimum, formatNumber(discount.value))
        }
        is Discount.Percentage -> if (minimumOrder != null) {
            stringResource(R.string.promocode_percentage_discount, formatNumber(discount.value), minimumOrder)
        } else {
            stringResource(R.string.promocode_percentage_discount_no_minimum, formatNumber(discount.value))
        }
        is Discount.FreeItem -> if (minimumOrder != null) {
            stringResource(R.string.promocode_free_item_discount, discount.description, minimumOrder)
        } else {
            stringResource(R.string.promocode_free_item_discount_no_minimum, discount.description)
        }
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

@Composable
private fun PromocodeImagePager(
    imageUrls: List<String>,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { imageUrls.size })

    HorizontalPager(
        state = pagerState,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = SpacingTokens.xs),
        pageSpacing = SpacingTokens.sm,
    ) { page ->
        ContentImage(
            uri = imageUrls[page],
            currentPage = page + 1,
            totalPages = imageUrls.size,
            ratio = 16f / 9f,
            onClick = { onImageClick(imageUrls[page]) },
        )
    }
}

@PreviewLightDark
@Composable
private fun PromocodeHeaderPreview() {
    QodeTheme {
        val samplePromoCode = PromocodePreviewData.percentagePromocode

        PromocodeInfo(
            promocode = samplePromoCode,
            displayVoteScore = 5,
            onImageClick = {},
        )
    }
}
