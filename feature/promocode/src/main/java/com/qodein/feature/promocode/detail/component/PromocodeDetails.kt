package com.qodein.feature.promocode.detail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.component.CircularImage
import com.qodein.core.designsystem.icon.PromocodeIcons
import com.qodein.core.designsystem.icon.QodeIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.preview.PromocodePreviewData
import com.qodein.core.ui.util.formatNumber
import com.qodein.core.ui.util.rememberFormattedRelativeTime
import com.qodein.feature.promocode.R
import com.qodein.feature.promocode.detail.PromocodeDetailScreen
import com.qodein.feature.promocode.detail.PromocodeDetailUiState
import com.qodein.shared.model.Discount
import com.qodein.shared.model.Promocode
import com.qodein.shared.model.PromocodeInteraction

@Composable
internal fun PromocodeDetails(
    promocode: Promocode,
    modifier: Modifier = Modifier
) {
    val isPercentage = promocode.discount is Discount.Percentage
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
    ) {
        Text(
            text = stringResource(R.string.promocode_details_title),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth(),
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
        ) {
            DetailItem(
                label = stringResource(R.string.promocode_details_service),
                value = promocode.serviceName,
                valueColor = MaterialTheme.colorScheme.primary,
            ) {
                CircularImage(
                    fallbackIcon = QodeIcons.Store,
                    imageUrl = promocode.serviceLogoUrl,
                    modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                )
            }

            DetailItem(
                icon = if (isPercentage) PromocodeIcons.Percentage else PromocodeIcons.FixedAmount,
                label = stringResource(R.string.promocode_details_discount),
                value = formatNumber(promocode.discount.value) + if (isPercentage) "%" else "₸",
                valueColor = MaterialTheme.colorScheme.primary,
            )

            DetailItem(
                icon = PromocodeIcons.MinimumOrder,
                label = stringResource(R.string.promocode_details_minimum_order),
                value = "${formatNumber(promocode.minimumOrderAmount)}₸",
                valueColor = MaterialTheme.colorScheme.primary,
            )
            DetailItem(
                icon = PromocodeIcons.StartDate,
                label = stringResource(R.string.promocode_details_start_date),
                value = rememberFormattedRelativeTime(promocode.startDate),
                valueColor = MaterialTheme.colorScheme.secondary,
            )
            DetailItem(
                icon = PromocodeIcons.EndDate,
                label = stringResource(R.string.promocode_details_end_date),
                value = rememberFormattedRelativeTime(promocode.endDate),
                valueColor = MaterialTheme.colorScheme.secondary,
            )
            DetailItem(
                icon = PromocodeIcons.NewUserOnly,
                label = stringResource(R.string.promocode_details_new_user_only),
                value = if (promocode.isFirstUseOnly) {
                    stringResource(R.string.promocode_details_value_yes)
                } else {
                    stringResource(R.string.promocode_details_value_no)
                },
                valueColor = MaterialTheme.colorScheme.tertiary,
            )
            DetailItem(
                icon = PromocodeIcons.OneTimeUse,
                label = stringResource(R.string.promocode_details_one_time_use_only),
                value = if (promocode.isOneTimeUseOnly) {
                    stringResource(R.string.promocode_details_value_yes)
                } else {
                    stringResource(R.string.promocode_details_value_no)
                },
                valueColor = MaterialTheme.colorScheme.tertiary,
            )
        }
    }
}

@Composable
private fun DetailItem(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    leadingContent: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingContent?.invoke() ?: icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                )
            }

            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        HorizontalDivider()
    }
}

@ThemePreviews
@Composable
private fun DetailsSectionPreview() {
    QodeTheme {
        val samplePromoCode = PromocodePreviewData.percentagePromocode

        PromocodeDetailScreen(
            uiState = PromocodeDetailUiState(
                promocodeId = samplePromoCode.id,
                promocodeInteraction = PromocodeInteraction(
                    promocode = samplePromoCode,
                    userInteraction = null,
                ),
                isLoading = false,
            ),
            onAction = {},
            onNavigateBack = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
