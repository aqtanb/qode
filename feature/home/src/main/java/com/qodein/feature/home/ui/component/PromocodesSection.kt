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
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.icon.QodeBusinessIcons
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.component.SortIconHelper
import com.qodein.core.ui.error.asUiText
import com.qodein.feature.home.R
import com.qodein.feature.home.ui.state.PromoCodeState
import com.qodein.shared.model.CompleteFilterState

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
        message = errorState.errorType.asUiText(),
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
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
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
