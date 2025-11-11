
package com.qodein.feature.home.ui.component

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.qodein.core.ui.component.ServiceSelectorBottomSheet
import com.qodein.core.ui.component.SortFilterBottomSheet
import com.qodein.core.ui.state.ServiceSelectionUiAction
import com.qodein.core.ui.state.ServiceSelectionUiState
import com.qodein.feature.home.HomeAction
import com.qodein.shared.domain.service.selection.SelectionState
import com.qodein.shared.domain.service.selection.ServiceSelectionState
import com.qodein.shared.model.CompleteFilterState
import com.qodein.shared.model.Service
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
    serviceSelectionState: ServiceSelectionState,
    cachedServices: Map<String, Service>,
    onAction: (HomeAction) -> Unit
) {
    activeDialog?.let { dialogType ->
        when (dialogType) {
            FilterDialogType.Service -> {
                var isSearchFocused by remember { mutableStateOf(false) }
                val isSearching = serviceSelectionState.search.isSearching

                // Use different sheet state based on focus/search mode
                val sheetState = rememberModalBottomSheetState(
                    skipPartiallyExpanded = isSearchFocused || isSearching,
                )

                // Use cached services from ViewModel

                val selectedServiceIds = when (val filter = currentFilters.serviceFilter) {
                    ServiceFilter.All -> emptySet()
                    is ServiceFilter.Selected -> filter.services.map { it.id }.toSet()
                }

                // Update selection state with current filter
                val updatedSelectionState = serviceSelectionState.copy(
                    selection = SelectionState.Multi(selectedIds = selectedServiceIds),
                )

                val uiState = ServiceSelectionUiState(
                    domainState = updatedSelectionState,
                    allServices = cachedServices,
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
        }
    }
}
