
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
import com.qodein.core.ui.state.ServiceSelectionUiAction
import com.qodein.core.ui.state.ServiceSelectionUiState
import com.qodein.feature.home.HomeAction
import com.qodein.feature.home.ui.state.SearchResultState
import com.qodein.feature.home.ui.state.ServiceSearchState
import com.qodein.shared.domain.service.selection.PopularServices
import com.qodein.shared.domain.service.selection.PopularStatus
import com.qodein.shared.domain.service.selection.SearchState
import com.qodein.shared.domain.service.selection.SearchStatus
import com.qodein.shared.domain.service.selection.SelectionState
import com.qodein.shared.domain.service.selection.ServiceSelectionState
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

                // Map current home state to unified domain state
                val searchStatus = when {
                    serviceSearchState.isLoading -> SearchStatus.Loading
                    services.isNotEmpty() -> SearchStatus.Success(services.map { it.id })
                    searchQuery.length >= 2 -> SearchStatus.Success(emptyList())
                    else -> SearchStatus.Idle
                }

                val selectedServiceIds = when (val filter = currentFilters.serviceFilter) {
                    ServiceFilter.All -> emptySet()
                    is ServiceFilter.Selected -> filter.services.map { it.id }.toSet()
                }

                val domainState = ServiceSelectionState(
                    search = SearchState(query = searchQuery, status = searchStatus),
                    popular = PopularServices(
                        ids = services.map { it.id },
                        status = if (serviceSearchState.isLoading) PopularStatus.Loading else PopularStatus.Idle,
                    ),
                    selection = SelectionState.Multi(selectedIds = selectedServiceIds),
                )

                val uiState = ServiceSelectionUiState(
                    domainState = domainState,
                    allServices = services.associateBy { it.id.value },
                    isVisible = true,
                    isSearchFocused = isSearchFocused,
                )

                ServiceSelectorBottomSheet(
                    state = uiState,
                    sheetState = sheetState,
                    onAction = { uiAction ->
                        when (uiAction) {
                            is ServiceSelectionUiAction.UpdateQuery -> {
                                onAction(HomeAction.SearchServices(uiAction.query))
                            }
                            ServiceSelectionUiAction.ClearQuery -> {
                                onAction(HomeAction.SearchServices(""))
                            }
                            is ServiceSelectionUiAction.SelectService -> {
                                val currentFilter = currentFilters.serviceFilter
                                val newFilter = when (currentFilter) {
                                    ServiceFilter.All -> ServiceFilter.Selected(setOf(uiAction.service))
                                    is ServiceFilter.Selected -> currentFilter.toggle(uiAction.service)
                                }
                                onAction(HomeAction.ApplyServiceFilter(newFilter))
                                onAction(HomeAction.DismissFilterDialog)
                            }
                            is ServiceSelectionUiAction.SetSearchFocus -> {
                                isSearchFocused = uiAction.focused
                            }
                            ServiceSelectionUiAction.Dismiss -> {
                                onAction(HomeAction.DismissFilterDialog)
                            }
                            else -> {
                                // Handle other UI actions if needed
                            }
                        }
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
