package com.qodein.feature.home.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.component.QodeButton
import com.qodein.core.designsystem.icon.UIIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.component.SortIconHelper
import com.qodein.core.ui.error.asUiText
import com.qodein.feature.home.R
import com.qodein.feature.home.ui.state.PromocodeUiState
import com.qodein.shared.common.error.FirestoreError
import com.qodein.shared.model.CompleteFilterState

@Composable
fun PromocodeSectionHeader(
    currentFilters: CompleteFilterState,
    modifier: Modifier = Modifier
) {
    val titleRes = SortIconHelper.getSortSectionTitleRes(sortBy = currentFilters.sortFilter.sortBy)

    Text(
        text = stringResource(titleRes),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
fun PromocodeSectionLoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.md),
    ) {
        repeat(3) {
            PromocodeCardSkeleton(
                modifier = Modifier.padding(SpacingTokens.sm),
            )
        }
    }
}

@Composable
fun PromocodeSectionErrorState(
    errorState: PromocodeUiState.Error,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    StateContainer(modifier = modifier) {
        Icon(
            imageVector = UIIcons.Error,
            contentDescription = null,
            modifier = Modifier.size(SizeTokens.Icon.sizeLarge),
            tint = MaterialTheme.colorScheme.error,
        )

        Text(
            text = errorState.error.asUiText(),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(horizontal = SpacingTokens.sm),
        )

        QodeButton(
            onClick = onRetry,
            text = stringResource(R.string.error_retry),
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}

@Composable
fun PromocodeSectionEmptyState(modifier: Modifier = Modifier) {
    StateContainer(modifier = modifier) {
        Icon(
            imageVector = UIIcons.Empty,
            contentDescription = null,
            modifier = Modifier.size(SizeTokens.Icon.sizeLarge),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = stringResource(R.string.empty_no_promo_codes),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = SpacingTokens.sm),
        )

        Text(
            text = stringResource(R.string.empty_check_back_later),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = SpacingTokens.sm),
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

@Composable
private fun StateContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SpacingTokens.lg, vertical = SpacingTokens.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.md, Alignment.CenterVertically),
        content = content,
    )
}

@ThemePreviews
@Composable
private fun PromocodeSectionEmptyStatePreview() {
    QodeTheme {
        PromocodeSectionEmptyState()
    }
}

@ThemePreviews
@Composable
private fun PromocodeSectionErrorStatePreview() {
    QodeTheme {
        PromocodeSectionErrorState(
            errorState = PromocodeUiState.Error(error = FirestoreError.NotFound),
            onRetry = { },
        )
    }
}
