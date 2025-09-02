
package com.qodein.feature.home.ui.component

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.qodein.core.ui.component.CategoryFilterBottomSheet
import com.qodein.core.ui.component.ServiceSelectorBottomSheet
import com.qodein.core.ui.component.SortFilterBottomSheet
import com.qodein.feature.home.HomeAction
import com.qodein.feature.home.ui.state.SearchResultState
import com.qodein.feature.home.ui.state.ServiceSearchState
import com.qodein.shared.model.CompleteFilterState
import com.qodein.shared.model.ServiceFilter
import com.qodein.shared.model.SortFilter
import com.qodein.shared.ui.FilterDialogType

/**
 * Centralized dialog management for home screen
 * Handles all filter dialogs and their state
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DialogCoordinator(
    activeDialog: FilterDialogType?,
    currentFilters: CompleteFilterState,
    serviceSearchState: ServiceSearchState,
    onAction: (HomeAction) -> Unit
) {
    activeDialog?.let { dialogType ->
        when (dialogType) {
            FilterDialogType.Category -> {
                val sheetState = rememberModalBottomSheetState()
                CategoryFilterBottomSheet(
                    isVisible = true,
                    currentFilter = currentFilters.categoryFilter,
                    onFilterSelected = { filter ->
                        onAction(HomeAction.ApplyCategoryFilter(filter))
                        onAction(HomeAction.DismissFilterDialog)
                    },
                    onDismiss = { onAction(HomeAction.DismissFilterDialog) },
                    sheetState = sheetState,
                )
            }

            FilterDialogType.Service -> {
                var isSearchFocused by remember { mutableStateOf(false) }
                val searchQuery = serviceSearchState.query
                val isSearching = serviceSearchState.isSearching

                // Use different sheet state based on focus/search mode
                val sheetState = rememberModalBottomSheetState(
                    skipPartiallyExpanded = isSearchFocused || isSearching,
                )

                val services = when (serviceSearchState.state) {
                    is SearchResultState.Success -> serviceSearchState.state.services
                    else -> emptyList()
                }

                ServiceSelectorBottomSheet(
                    isVisible = true,
                    services = services,
                    popularServices = services, // TODO: Separate popular services from search results
                    searchQuery = searchQuery,
                    onSearchQueryChange = { query -> onAction(HomeAction.SearchServices(query)) },
                    onServiceSelected = { service ->
                        val currentFilter = currentFilters.serviceFilter
                        val newFilter = when (currentFilter) {
                            ServiceFilter.All -> ServiceFilter.Selected(setOf(service))
                            is ServiceFilter.Selected -> currentFilter.toggle(service)
                        }
                        onAction(HomeAction.ApplyServiceFilter(newFilter))
                        onAction(HomeAction.DismissFilterDialog)
                    },
                    onDismiss = { onAction(HomeAction.DismissFilterDialog) },
                    onSearch = { query -> onAction(HomeAction.SearchServices(query)) },
                    isLoading = serviceSearchState.isLoading,
                    sheetState = sheetState,
                    selectedServices = when (val filter = currentFilters.serviceFilter) {
                        ServiceFilter.All -> emptyList()
                        is ServiceFilter.Selected -> filter.services.toList()
                    },
                    onSearchFocused = { focused -> isSearchFocused = focused },
                )
            }

            FilterDialogType.Sort -> {
                val sheetState = rememberModalBottomSheetState()
                SortFilterBottomSheet(
                    isVisible = true,
                    currentSortBy = currentFilters.sortFilter.sortBy,
                    onSortBySelected = { sortBy ->
                        onAction(HomeAction.ApplySortFilter(SortFilter(sortBy)))
                        onAction(HomeAction.DismissFilterDialog)
                    },
                    onDismiss = { onAction(HomeAction.DismissFilterDialog) },
                    sheetState = sheetState,
                )
            }

            FilterDialogType.Tag -> {
                // Home doesn't use tag filters, but we handle it for completeness
                onAction(HomeAction.DismissFilterDialog)
            }
        }
    }
}
