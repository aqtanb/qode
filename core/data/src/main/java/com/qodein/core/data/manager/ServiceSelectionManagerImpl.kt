package com.qodein.core.data.manager

import com.qodein.shared.domain.service.selection.SearchStatus
import com.qodein.shared.domain.service.selection.SelectionState
import com.qodein.shared.domain.service.selection.SelectionValidationResult
import com.qodein.shared.domain.service.selection.ServiceSelectionAction
import com.qodein.shared.domain.service.selection.ServiceSelectionManager
import com.qodein.shared.domain.service.selection.ServiceSelectionState
import com.qodein.shared.model.ServiceId

/**
 * Pure domain implementation of service selection business logic.
 * No UI state, no flows, no lifecycle - just business rules.
 */

class ServiceSelectionManagerImpl : ServiceSelectionManager {

    override fun applyAction(
        state: ServiceSelectionState,
        action: ServiceSelectionAction
    ): ServiceSelectionState =
        when (action) {
            is ServiceSelectionAction.UpdateQuery -> applyUpdateQuery(state, action.query)
            is ServiceSelectionAction.ToggleService -> applyToggleService(state, action.id)
            ServiceSelectionAction.ClearSelection -> applyClearSelection(state)
        }

    override fun validateSelection(selection: SelectionState): SelectionValidationResult =
        when (selection) {
            is SelectionState.Single -> {
                // Single mode is always valid (can have 0 or 1 selection)
                SelectionValidationResult.Valid
            }
            is SelectionState.Multi -> {
                // Multi mode is always valid (can have 0+ selections)
                // Could add business rules here like max selection limit
                SelectionValidationResult.Valid
            }
        }

    override fun getRecommendedServices(selection: SelectionState): List<ServiceId> {
        // Business logic for recommendations based on current selection
        return when (selection) {
            is SelectionState.Single -> {
                // Could recommend related services based on selected service
                emptyList() // Placeholder - implement recommendation logic
            }
            is SelectionState.Multi -> {
                // Could recommend services that work well with current selection
                emptyList() // Placeholder - implement recommendation logic
            }
        }
    }

    // Pure state transformation functions
    private fun applyUpdateQuery(
        state: ServiceSelectionState,
        query: String
    ): ServiceSelectionState {
        val newStatus = if (query.length >= 2) SearchStatus.Loading else SearchStatus.Idle
        return state.copy(
            search = state.search.copy(
                query = query,
                status = newStatus,
            ),
        )
    }

    private fun applyToggleService(
        state: ServiceSelectionState,
        serviceId: ServiceId
    ): ServiceSelectionState {
        val newSelection = when (val selection = state.selection) {
            is SelectionState.Single -> {
                if (selection.selectedId == serviceId) {
                    SelectionState.Single(selectedId = null) // Unselect if already selected
                } else {
                    SelectionState.Single(selectedId = serviceId) // Select if not selected
                }
            }
            is SelectionState.Multi -> {
                if (serviceId in selection.selectedIds) {
                    SelectionState.Multi(selectedIds = selection.selectedIds - serviceId) // Remove if already selected
                } else {
                    SelectionState.Multi(selectedIds = selection.selectedIds + serviceId) // Add if not selected
                }
            }
        }
        return state.copy(selection = newSelection)
    }

    private fun applyClearSelection(state: ServiceSelectionState): ServiceSelectionState {
        val newSelection = when (state.selection) {
            is SelectionState.Single -> SelectionState.Single(selectedId = null)
            is SelectionState.Multi -> SelectionState.Multi(selectedIds = emptySet())
        }
        return state.copy(selection = newSelection)
    }
}
