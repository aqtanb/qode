package com.qodein.core.data.manager

import com.qodein.shared.domain.service.selection.SearchState
import com.qodein.shared.domain.service.selection.SearchStatus
import com.qodein.shared.domain.service.selection.SelectionState
import com.qodein.shared.domain.service.selection.SelectionValidationResult
import com.qodein.shared.domain.service.selection.ServiceSelectionAction
import com.qodein.shared.domain.service.selection.ServiceSelectionManager
import com.qodein.shared.domain.service.selection.ServiceSelectionState
import com.qodein.shared.model.ServiceId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pure domain implementation of service selection business logic.
 * No UI state, no flows, no lifecycle - just business rules.
 */
@Singleton
class ServiceSelectionManagerImpl @Inject constructor() : ServiceSelectionManager {

    override fun applyAction(
        state: ServiceSelectionState,
        action: ServiceSelectionAction
    ): ServiceSelectionState =
        when (action) {
            // Search actions
            is ServiceSelectionAction.UpdateQuery -> applyUpdateQuery(state, action.query)
            ServiceSelectionAction.ClearQuery -> applyClearQuery(state)
            ServiceSelectionAction.RetrySearch -> applyRetrySearch(state)

            // Selection actions
            is ServiceSelectionAction.SelectService -> applySelectService(state, action.id)
            is ServiceSelectionAction.UnselectService -> applyUnselectService(state, action.id)
            ServiceSelectionAction.ClearSelection -> applyClearSelection(state)

            // Popular services actions - these trigger effects, no state change
            ServiceSelectionAction.LoadPopularServices,
            ServiceSelectionAction.RetryPopularServices -> state

            // Focus actions - pure UI concern, no domain state change
            is ServiceSelectionAction.SetSearchFocus -> state
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

    private fun applyClearQuery(state: ServiceSelectionState): ServiceSelectionState =
        state.copy(
            search = SearchState(query = "", status = SearchStatus.Idle),
        )

    private fun applyRetrySearch(state: ServiceSelectionState): ServiceSelectionState =
        if (state.search.isSearching) {
            state.copy(
                search = state.search.copy(status = SearchStatus.Loading),
            )
        } else {
            state // No change if not in search mode
        }

    private fun applySelectService(
        state: ServiceSelectionState,
        serviceId: ServiceId
    ): ServiceSelectionState {
        val newSelection = when (val selection = state.selection) {
            is SelectionState.Single -> SelectionState.Single(selectedId = serviceId)
            is SelectionState.Multi -> SelectionState.Multi(
                selectedIds = selection.selectedIds + serviceId,
            )
        }
        return state.copy(selection = newSelection)
    }

    private fun applyUnselectService(
        state: ServiceSelectionState,
        serviceId: ServiceId
    ): ServiceSelectionState {
        val newSelection = when (val selection = state.selection) {
            is SelectionState.Single -> {
                if (selection.selectedId == serviceId) {
                    SelectionState.Single(selectedId = null)
                } else {
                    selection // No change if different service
                }
            }
            is SelectionState.Multi -> SelectionState.Multi(
                selectedIds = selection.selectedIds - serviceId,
            )
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
