package com.qodein.core.ui.state

import com.qodein.shared.domain.service.selection.PopularStatus
import com.qodein.shared.domain.service.selection.SearchStatus
import com.qodein.shared.domain.service.selection.SelectionState
import com.qodein.shared.domain.service.selection.ServiceSelectionState
import com.qodein.shared.model.Service

/**
 * UI-specific state for service selection that combines domain state with UI concerns
 */
data class ServiceSelectionUiState(
    val domainState: ServiceSelectionState = ServiceSelectionState(),
    val allServices: Map<String, Service> = emptyMap(), // Service lookup by ID
    val isVisible: Boolean = false,
    val isSearchFocused: Boolean = false
) {
    /**
     * Services to display based on current domain state
     */
    val displayServices: List<Service> get() = when (val searchStatus = domainState.search.status) {
        is SearchStatus.Success -> {
            searchStatus.ids.mapNotNull { allServices[it.value] }
        }
        else -> emptyList()
    }

    /**
     * Popular services to display
     */
    val popularServices: List<Service> get() = domainState.popular.ids.mapNotNull {
        allServices[it.value]
    }

    /**
     * Currently selected services
     */
    val selectedServices: List<Service> get() = when (val selection = domainState.selection) {
        is SelectionState.Single -> {
            selection.selectedId?.let { allServices[it.value] }?.let { listOf(it) } ?: emptyList()
        }
        is SelectionState.Multi -> {
            selection.selectedIds.mapNotNull { allServices[it.value] }
        }
    }

    /**
     * Whether auto-expand should be triggered (search focused or actively searching)
     */
    val shouldAutoExpand: Boolean get() = isSearchFocused || domainState.search.isSearching

    /**
     * Whether search is currently loading
     */
    val isSearchLoading: Boolean get() =
        domainState.search.status is SearchStatus.Loading

    /**
     * Whether popular services are loading
     */
    val isPopularLoading: Boolean get() =
        domainState.popular.status is PopularStatus.Loading
}

/**
 * UI actions for service selection
 */
sealed interface ServiceSelectionUiAction {
    // Domain actions that map directly
    data class UpdateQuery(val query: String) : ServiceSelectionUiAction
    data object ClearQuery : ServiceSelectionUiAction
    data class SelectService(val service: Service) : ServiceSelectionUiAction
    data class UnselectService(val service: Service) : ServiceSelectionUiAction
    data object ClearSelection : ServiceSelectionUiAction
    data object RetrySearch : ServiceSelectionUiAction
    data object LoadPopularServices : ServiceSelectionUiAction

    // UI-specific actions
    data class SetSearchFocus(val focused: Boolean) : ServiceSelectionUiAction
    data object Show : ServiceSelectionUiAction
    data object Hide : ServiceSelectionUiAction
    data object Dismiss : ServiceSelectionUiAction
}
