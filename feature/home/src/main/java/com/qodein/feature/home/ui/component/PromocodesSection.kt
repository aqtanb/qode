package com.qodein.feature.home.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.component.QodeButtonVariant
import com.qodein.core.designsystem.icon.QodeBusinessIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.component.SortIconHelper
import com.qodein.core.ui.error.toLocalizedMessage
import com.qodein.feature.home.HomeAction
import com.qodein.feature.home.R
import com.qodein.feature.home.ui.state.PromoCodeState
import com.qodein.shared.model.CompleteFilterState
import com.qodein.shared.model.PromoCode

/**
 * Promo codes section with header and list
 * Extracted from HomeScreen for better modularity
 */
@Composable
fun PromocodesSection(
    promoCodeState: PromoCodeState,
    currentFilters: CompleteFilterState,
    isLoadingMore: Boolean,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Section header for ALL states
        PromoCodesSectionHeader(
            currentFilters = currentFilters,
            modifier = modifier.padding(horizontal = SpacingTokens.lg),
        )

        when (promoCodeState) {
            PromoCodeState.Loading -> {
                PromoCodesLoadingState(
                    modifier = modifier
                        .fillMaxWidth()
                        .padding(SpacingTokens.xl),
                )
            }
            is PromoCodeState.Success -> {
                PromoCodeContent(
                    promoCodes = promoCodeState.promoCodes,
                    isLoadingMore = isLoadingMore,
                    onAction = onAction,
                )
            }
            PromoCodeState.Empty -> {
                PromoCodesEmptyState(
                    modifier = modifier.padding(horizontal = SpacingTokens.lg),
                )
            }
            is PromoCodeState.Error -> {
                PromoCodesErrorState(
                    errorState = promoCodeState,
                    onRetry = { onAction(HomeAction.RetryPromoCodesClicked) },
                    modifier = modifier.padding(horizontal = SpacingTokens.lg),
                )
            }
        }
    }
}

@Composable
private fun PromoCodeContent(
    promoCodes: List<PromoCode>,
    isLoadingMore: Boolean,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    PromoCodesList(
        promoCodes = promoCodes,
        isLoadingMore = isLoadingMore,
        onAction = onAction,
        modifier = modifier.fillMaxWidth().padding(top = SpacingTokens.md),
    )
}

@Composable
fun PromoCodesSectionHeader(
    currentFilters: CompleteFilterState,
    modifier: Modifier = Modifier
) {
    val titleRes = SortIconHelper.getSortSectionTitleRes(sortBy = currentFilters.sortFilter.sortBy)

    Text(
        text = stringResource(titleRes),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun PromoCodesList(
    promoCodes: List<PromoCode>,
    isLoadingMore: Boolean,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
    ) {
        promoCodes.forEach { promoCode ->
            CouponPromoCodeCard(
                promoCode = promoCode,
                onCardClick = {
                    onAction(HomeAction.PromoCodeClicked(promoCode))
                },
                onCopyCodeClick = {
                    onAction(HomeAction.CopyPromoCode(promoCode))
                },
                modifier = modifier.padding(
                    horizontal = SpacingTokens.lg,
                    vertical = SpacingTokens.xs,
                ),
            )
        }

        // Loading indicator for pagination
        if (isLoadingMore) {
            LoadingMoreIndicator()
        }
    }
}

@Composable
fun PromoCodesLoadingState(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.promocodes_loading_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.promocodes_loading_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        CircularProgressIndicator(
            modifier = modifier.size(20.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
fun PromoCodesErrorState(
    errorState: PromoCodeState.Error,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    ErrorCard(
        message = errorState.errorType.toLocalizedMessage(),
        isRetryable = errorState.isRetryable,
        onRetry = onRetry,
        modifier = modifier,
    )
}

@Composable
private fun ErrorCard(
    message: String,
    isRetryable: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(SpacingTokens.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
    ) {
        Icon(
            imageVector = QodeBusinessIcons.Asset,
            contentDescription = null,
            modifier = modifier.size(SizeTokens.Avatar.sizeLarge),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = ERROR_ICON_ALPHA),
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error,
        )

        if (isRetryable) {
            QodeButton(
                onClick = onRetry,
                text = stringResource(R.string.error_retry),
                variant = QodeButtonVariant.Secondary,
            )
        }
    }
}

@Composable
fun PromoCodesEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(SpacingTokens.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = QodeBusinessIcons.Asset,
            contentDescription = null,
            modifier = modifier.size(SizeTokens.Avatar.sizeLarge),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = ERROR_ICON_ALPHA),
        )

        Text(
            text = stringResource(R.string.empty_no_promo_codes),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.padding(top = SpacingTokens.lg),
        )

        Text(
            text = stringResource(R.string.empty_check_back_later),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.padding(top = SpacingTokens.sm),
        )
    }
}

@Composable
fun LoadingMoreIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(SpacingTokens.xl),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = ShapeTokens.Border.medium,
        )
    }
}

// MARK: - Constants

private const val ERROR_ICON_ALPHA = 0.6f

@Preview(name = "PromoCodesSection - Loading", showBackground = true)
@Composable
private fun PromoCodesSectionPreview() {
    QodeTheme {
        PromocodesSection(
            promoCodeState = PromoCodeState.Loading,
            currentFilters = CompleteFilterState(),
            isLoadingMore = false,
            onAction = { },
        )
    }
}
