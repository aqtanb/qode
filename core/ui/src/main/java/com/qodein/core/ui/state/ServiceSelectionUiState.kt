package com.qodein.core.ui.state

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
    val isVisible: Boolean = false
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
}
