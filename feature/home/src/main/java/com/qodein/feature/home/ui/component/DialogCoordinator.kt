
package com.qodein.feature.home.ui.component

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import com.qodein.core.ui.component.CategoryFilterBottomSheet
import com.qodein.core.ui.component.ServiceSelectorBottomSheet
import com.qodein.core.ui.component.SortFilterBottomSheet
import com.qodein.feature.home.HomeAction
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
                val sheetState = rememberModalBottomSheetState()
                val services = when (serviceSearchState) {
                    is ServiceSearchState.Success -> serviceSearchState.services
                    else -> emptyList()
                }
                val isLoading = serviceSearchState is ServiceSearchState.Loading

                ServiceSelectorBottomSheet(
                    isVisible = true,
                    services = services,
                    popularServices = services,
                    currentSelection = "",
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
                    isLoading = isLoading,
                    sheetState = sheetState,
                    title = "Filter by Service",
                    searchPlaceholder = "Search for services...",
                    emptyMessage = "No services found",
                    selectedServices = when (val filter = currentFilters.serviceFilter) {
                        ServiceFilter.All -> emptyList()
                        is ServiceFilter.Selected -> filter.services.toList()
                    },
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
