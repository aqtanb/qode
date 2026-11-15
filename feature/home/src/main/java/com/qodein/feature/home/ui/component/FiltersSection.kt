package com.qodein.feature.home.ui.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.qodein.core.designsystem.component.QodeinAsyncImage
import com.qodein.core.designsystem.icon.ActionIcons
import com.qodein.core.designsystem.icon.NavigationIcons
import com.qodein.core.designsystem.icon.QodeIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.preview.ServicePreviewData
import com.qodein.feature.home.R
import com.qodein.shared.model.CompleteFilterState
import com.qodein.shared.model.Service
import com.qodein.shared.model.ServiceFilter
import com.qodein.core.ui.R as CoreUiR

/**
 * Service filters for the home feed: the full picker plus the most picked services as quick chips.
 * Sorting lives in the section header.
 */
@Composable
fun FiltersSection(
    currentFilters: CompleteFilterState,
    popularServices: List<Service>,
    onShowServiceSelection: () -> Unit,
    onTogglePopularService: (Service) -> Unit,
    onResetFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = SpacingTokens.md),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ServiceChip(serviceFilter = currentFilters.serviceFilter, onClick = onShowServiceSelection)

        val selectedIds = currentFilters.selectedServiceIds
        popularServices.forEach { service ->
            FilterChip(
                selected = service.id in selectedIds,
                onClick = { onTogglePopularService(service) },
                label = { Text(service.name, maxLines = 1) },
                leadingIcon = { ServiceLogo(service) },
                shape = CircleShape,
            )
        }

        if (currentFilters.hasActiveFilters) {
            IconButton(onClick = onResetFilters) {
                Icon(ActionIcons.Clear, contentDescription = stringResource(R.string.filter_chip_reset))
            }
        }
    }
}

@Composable
private fun ServiceChip(
    serviceFilter: ServiceFilter,
    onClick: () -> Unit
) {
    val count = (serviceFilter as? ServiceFilter.Selected)?.services?.size ?: 0
    FilterChip(
        selected = count > 0,
        onClick = onClick,
        label = {
            Text(
                text = if (count > 0) {
                    pluralStringResource(R.plurals.home_services_selected, count, count)
                } else {
                    stringResource(CoreUiR.string.ui_service)
                },
                maxLines = 1,
            )
        },
        leadingIcon = {
            Icon(QodeIcons.Service, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize))
        },
        trailingIcon = {
            Icon(NavigationIcons.DropDown, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize))
        },
        shape = CircleShape,
    )
}

@Composable
private fun ServiceLogo(service: Service) {
    QodeinAsyncImage(
        imageUrl = service.logoUrl.orEmpty(),
        fallbackText = service.name,
        size = FilterChipDefaults.IconSize,
        contentDescription = "",
    )
}

@PreviewLightDark
@Composable
private fun FiltersSectionPreview() {
    QodeTheme {
        FiltersSection(
            currentFilters = CompleteFilterState(
                serviceFilter = ServiceFilter.Selected(services = setOf(ServicePreviewData.allSamples.first())),
            ),
            popularServices = ServicePreviewData.allSamples,
            onShowServiceSelection = { },
            onTogglePopularService = { },
            onResetFilters = { },
        )
    }
}

@PreviewLightDark
@Composable
private fun FiltersSectionEmptyPreview() {
    QodeTheme {
        FiltersSection(
            currentFilters = CompleteFilterState(),
            popularServices = ServicePreviewData.allSamples,
            onShowServiceSelection = { },
            onTogglePopularService = { },
            onResetFilters = { },
        )
    }
}
