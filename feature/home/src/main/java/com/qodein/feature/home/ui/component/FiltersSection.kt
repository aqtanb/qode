package com.qodein.feature.home.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.designsystem.component.CircularImage
import com.qodein.core.designsystem.icon.QodeCommerceIcons
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.home.R
import com.qodein.feature.home.model.CategoryFilter
import com.qodein.feature.home.model.FilterDialogType
import com.qodein.feature.home.model.ServiceFilter
import com.qodein.feature.home.model.SortFilter
import com.qodein.feature.home.ui.HomeIconService
import com.qodein.feature.home.ui.ServiceIconData
import com.qodein.feature.home.ui.state.FilterState
import com.qodein.shared.domain.repository.PromoCodeSortBy

/**
 * Quick filters section with category, service, and sort chips
 * Extracted from HomeScreen for better modularity
 */
@Composable
fun FiltersSection(
    currentFilters: FilterState,
    onFilterSelected: (FilterDialogType) -> Unit,
    modifier: Modifier = Modifier,
    iconService: HomeIconService? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = SpacingTokens.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = SpacingTokens.lg),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
        ) {
            // Category Filter
            item(key = "category_filter") {
                FilterChip(
                    nameRes = R.string.filter_chip_category,
                    icon = when (val filter = currentFilters.categoryFilter) {
                        CategoryFilter.All -> QodeNavigationIcons.Categories
                        is CategoryFilter.Selected -> {
                            when (filter.categories.size) {
                                0 -> QodeNavigationIcons.Categories
                                1 -> iconService?.getCategoryIcon(filter.categories.first()) ?: QodeNavigationIcons.Categories
                                else -> QodeNavigationIcons.More
                            }
                        }
                    },
                    onClick = { onFilterSelected(FilterDialogType.Category) },
                    isSelected = currentFilters.categoryFilter !is CategoryFilter.All,
                )
            }

            // Service Filter
            item(key = "service_filter") {
                val (icon, logoUrl, fallbackText) = when (val filter = currentFilters.serviceFilter) {
                    ServiceFilter.All -> Triple(QodeCommerceIcons.Store, null, null)
                    is ServiceFilter.Selected -> {
                        when (filter.services.size) {
                            0 -> Triple(QodeCommerceIcons.Store, null, null)
                            1 -> {
                                val serviceData = iconService?.getServiceIconData(filter.services.first()) ?: ServiceIconData()
                                Triple(QodeCommerceIcons.Store, serviceData.logoUrl, serviceData.fallbackText)
                            }
                            else -> Triple(QodeNavigationIcons.More, null, null)
                        }
                    }
                }

                FilterChip(
                    nameRes = R.string.filter_chip_service,
                    icon = icon,
                    logoUrl = logoUrl,
                    fallbackText = fallbackText,
                    onClick = { onFilterSelected(FilterDialogType.Service) },
                    isSelected = currentFilters.serviceFilter !is ServiceFilter.All,
                )
            }

            // Sort Filter
            item(key = "sort_filter") {
                val currentSortBy = (currentFilters.sortFilter as SortFilter.Selected).sortBy
                FilterChip(
                    nameRes = R.string.filter_chip_sort,
                    icon = iconService?.getSortIcon(currentSortBy) ?: QodeCommerceIcons.Store,
                    onClick = { onFilterSelected(FilterDialogType.Sort) },
                    isSelected = currentSortBy != PromoCodeSortBy.POPULARITY,
                )
            }
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
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.sm),
    ) {
        // Circular container with borders
        Box(
            modifier = Modifier.size(SizeTokens.Avatar.sizeMedium + SpacingTokens.sm),
            contentAlignment = Alignment.Center,
        ) {
            // Outer surface
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = CircleShape,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.surface
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
            ) {
                // Inner surface with border
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(ShapeTokens.Border.thick),
                    shape = CircleShape,
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
                        initials = fallbackText,
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
                        modifier = Modifier.fillMaxSize(),
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
                MaterialTheme.colorScheme.primary
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
            currentFilters = FilterState(),
            onFilterSelected = { },
        )
    }
}
