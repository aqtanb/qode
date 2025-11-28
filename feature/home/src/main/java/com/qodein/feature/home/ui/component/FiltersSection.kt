package com.qodein.feature.home.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.component.ButtonSize
import com.qodein.core.designsystem.component.CircularImage
import com.qodein.core.designsystem.component.QodeinOutlinedIconButton
import com.qodein.core.designsystem.icon.QodeEssentialIcons
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.icon.QodeUIIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.component.SortIconHelper
import com.qodein.core.ui.preview.ServicePreviewData
import com.qodein.feature.home.R
import com.qodein.shared.model.CompleteFilterState
import com.qodein.shared.model.Service
import com.qodein.shared.model.ServiceFilter
import com.qodein.shared.ui.FilterDialogType

/**
 * Quick filters section with category, service, and sort chips
 * Extracted from HomeScreen for better modularity
 */
@Composable
fun FiltersSection(
    currentFilters: CompleteFilterState,
    onFilterSelected: (FilterDialogType) -> Unit,
    onResetFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            space = SpacingTokens.lg,
            alignment = Alignment.CenterHorizontally,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val (serviceIcon, logoUrl, fallbackText) = when (val filter = currentFilters.serviceFilter) {
            ServiceFilter.All -> Triple(QodeEssentialIcons.Store, null, null)
            is ServiceFilter.Selected -> {
                when (filter.services.size) {
                    1 -> {
                        val service = filter.services.first()
                        Triple(QodeUIIcons.Filter1, service.logoUrl, service.name)
                    }
                    2 -> {
                        Triple(QodeUIIcons.Filter2, null, null)
                    }
                    3 -> {
                        Triple(QodeUIIcons.Filter3, null, null)
                    }
                    4 -> {
                        Triple(QodeUIIcons.Filter4, null, null)
                    }
                    5 -> {
                        Triple(QodeUIIcons.Filter5, null, null)
                    }
                    6 -> {
                        Triple(QodeUIIcons.Filter6, null, null)
                    }
                    7 -> {
                        Triple(QodeUIIcons.Filter7, null, null)
                    }
                    8 -> {
                        Triple(QodeUIIcons.Filter8, null, null)
                    }
                    9 -> {
                        Triple(QodeUIIcons.Filter9, null, null)
                    }
                    else -> Triple(QodeUIIcons.Filter9Plus, null, null)
                }
            }
        }

        FilterChip(
            nameRes = R.string.filter_chip_service,
            icon = serviceIcon,
            logoUrl = logoUrl,
            fallbackText = fallbackText,
            onClick = { onFilterSelected(FilterDialogType.Service) },
            isSelected = currentFilters.serviceFilter !is ServiceFilter.All,
        )

        val currentSortBy = currentFilters.sortFilter.sortBy
        FilterChip(
            nameRes = R.string.filter_chip_sort,
            icon = SortIconHelper.getSortIcon(currentSortBy),
            onClick = { onFilterSelected(FilterDialogType.Sort) },
            isSelected = false,
        )

        val hasActiveFilters = currentFilters.serviceFilter !is ServiceFilter.All
        if (hasActiveFilters) {
            FilterChip(
                nameRes = R.string.filter_chip_reset,
                icon = QodeNavigationIcons.Refresh,
                onClick = onResetFilters,
                isSelected = false,
            )
        }
    }
}

@Composable
private fun FilterChip(
    nameRes: Int,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    logoUrl: String? = null,
    fallbackText: String? = null
) {
    val logoUrlOrNull = logoUrl?.takeIf { it.isNotBlank() }
    val buttonIcon = if (logoUrlOrNull != null) null else icon
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xxs),
    ) {
        val border = BorderStroke(
            width = ShapeTokens.Border.medium,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        )
        val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

        QodeinOutlinedIconButton(
            onClick = onClick,
            icon = buttonIcon,
            contentDescription = stringResource(nameRes),
            size = ButtonSize.XL,
            contentColor = contentColor,
            border = border,
            content = logoUrlOrNull?.let {
                {
                    CircularImage(
                        imageUrl = it,
                        fallbackText = fallbackText,
                        fallbackIcon = icon,
                        size = SizeTokens.IconButton.sizeXL,
                        contentColor = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        contentDescription = stringResource(nameRes),
                    )
                }
            },
        )

        // Filter name
        Text(
            text = stringResource(nameRes),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@ThemePreviews
@Composable
private fun FiltersSectionPreview() {
    QodeTheme {
        FiltersSection(
            currentFilters = CompleteFilterState(
                serviceFilter = ServiceFilter.Selected(services = (ServicePreviewData.allSamples.toSet())),
            ),
            onFilterSelected = { },
            onResetFilters = { },
        )
    }
}
