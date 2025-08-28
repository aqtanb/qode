package com.qodein.feature.home.ui.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import com.qodein.core.ui.component.ServiceSelectorBottomSheet
import com.qodein.feature.home.HomeAction
import com.qodein.feature.home.model.FilterDialogType
import com.qodein.feature.home.model.ServiceFilter
import com.qodein.feature.home.ui.state.FilterState
import com.qodein.feature.home.ui.state.ServiceSearchState

/**
 * Centralized dialog management for home screen
 * Handles all filter dialogs and their state
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogCoordinator(
    activeDialog: FilterDialogType?,
    currentFilters: FilterState,
    serviceSearchState: ServiceSearchState,
    onAction: (HomeAction) -> Unit
) {
    activeDialog?.let { dialogType ->
        when (dialogType) {
            FilterDialogType.Category -> {
                CategoryFilterDialog(
                    currentFilter = currentFilters.categoryFilter,
                    onFilterSelected = { filter ->
                        onAction(HomeAction.ApplyCategoryFilter(filter))
                        onAction(HomeAction.DismissFilterDialog)
                    },
                    onDismiss = { onAction(HomeAction.DismissFilterDialog) },
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
                SortFilterDialog(
                    currentFilter = currentFilters.sortFilter,
                    onFilterSelected = { filter ->
                        onAction(HomeAction.ApplySortFilter(filter))
                        onAction(HomeAction.DismissFilterDialog)
                    },
                    onDismiss = { onAction(HomeAction.DismissFilterDialog) },
                )
            }
        }
    }
}
