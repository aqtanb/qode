package com.qodein.feature.home.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.designsystem.component.CircularImage
import com.qodein.core.designsystem.icon.QodeEssentialIcons
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.ui.component.SortIconHelper
import com.qodein.feature.home.R
import com.qodein.shared.model.CompleteFilterState
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
                        Triple(QodeEssentialIcons.Store, service.logoUrl, service.name)
                    }
                    else -> Triple(QodeEssentialIcons.StoreFilled, null, null)
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
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.xxs),
    ) {
        // Circular container with borders
        Box(
            modifier = modifier.size(SizeTokens.Avatar.sizeMedium + SpacingTokens.sm),
            contentAlignment = Alignment.Center,
        ) {
            // Outer surface
            Surface(
                modifier = modifier.fillMaxSize().clip(CircleShape),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.surface
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
            ) {
                // Inner surface with border
                Surface(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(ShapeTokens.Border.thick)
                        .clip(CircleShape),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    border = BorderStroke(
                        width = ShapeTokens.Border.thin,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                    ),
                ) {
                    CircularImage(
                        imageUrl = logoUrl,
                        fallbackText = fallbackText,
                        fallbackIcon = icon,
                        size = SizeTokens.Avatar.sizeMedium,
                        backgroundColor = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        contentColor = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        contentDescription = stringResource(nameRes),
                        modifier = modifier.fillMaxSize(),
                    )
                }
            }
        }

        // Filter name
        Text(
            text = stringResource(nameRes),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Preview(name = "FiltersSection", showBackground = true)
@Composable
private fun FiltersSectionPreview() {
    QodeTheme {
        FiltersSection(
            currentFilters = CompleteFilterState(),
            onFilterSelected = { },
            onResetFilters = { },
        )
    }
}
