package com.qodein.shared.domain.coordinator

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.manager.ServiceSearchManager
import com.qodein.shared.domain.service.selection.PopularStatus
import com.qodein.shared.domain.service.selection.SearchStatus
import com.qodein.shared.domain.service.selection.ServiceSelectionAction
import com.qodein.shared.domain.service.selection.ServiceSelectionManager
import com.qodein.shared.domain.service.selection.ServiceSelectionState
import com.qodein.shared.model.Service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Coordinates service selection between ServiceSearchManager and ServiceSelectionManager.
 * Eliminates code duplication between ViewModels and centralizes complex state management.
 */

class ServiceSelectionCoordinator(
    private val serviceSearchManager: ServiceSearchManager,
    private val serviceSelectionManager: ServiceSelectionManager
) {
    private var cachedPopularServices: List<Service>? = null
    private var isLoadingPopularServices = false

    /**
     * Exposes cached services from ServiceSearchManager.
     * Contains all services that have been fetched (popular + search results).
     */
    val cachedServices: StateFlow<Map<String, Service>>
        get() = serviceSearchManager.cachedServices

    /**
     * Sets up service selection coordination with state updates callback.
     * Handles the complex logic of combining search results, popular services, and caching.
     * Automatically loads popular services if not already cached.
     */
    fun setupServiceSelection(
        scope: CoroutineScope,
        getCurrentState: () -> ServiceSelectionState,
        onStateUpdate: (ServiceSelectionState) -> Unit
    ) {
        serviceSearchManager.activate()

        if (cachedPopularServices == null && !isLoadingPopularServices) {
            isLoadingPopularServices = true
            serviceSearchManager.clearQuery()
        }

        scope.launch {
            combine(
                serviceSearchManager.searchQuery,
                serviceSearchManager.searchResult,
            ) { query, searchResult ->
                Pair(query, searchResult)
            }.collect { (query, searchResult) ->
                val currentState = getCurrentState()
                val updatedState = processServiceSelectionUpdate(
                    currentState = currentState,
                    query = query,
                    searchResult = searchResult,
                )

                onStateUpdate(updatedState)
            }
        }
    }

    /**
     * Handles service selection actions and returns updated state.
     */
    fun handleAction(
        currentState: ServiceSelectionState,
        action: ServiceSelectionAction
    ): ServiceSelectionState {
        val newState = serviceSelectionManager.applyAction(currentState, action)

        // Handle side effects
        handleSideEffects(action, newState)

        return newState
    }

    /**
     * Deactivates the service search manager.
     */
    fun deactivate() {
        serviceSearchManager.deactivate()
    }

    private fun processServiceSelectionUpdate(
        currentState: ServiceSelectionState,
        query: String,
        searchResult: Result<List<Service>, OperationError>
    ): ServiceSelectionState {
        // Apply query update if changed
        val updatedState = if (query != currentState.search.query) {
            serviceSelectionManager.applyAction(
                currentState,
                ServiceSelectionAction.UpdateQuery(query),
            )
        } else {
            currentState
        }

        return when (searchResult) {
            is Result.Success -> {
                val serviceIds = searchResult.data.map { it.id }

                if (query.isBlank()) {
                    updatedState.copy(
                        popular = updatedState.popular.copy(
                            ids = serviceIds,
                            status = PopularStatus.Success,
                        ),
                    )
                } else {
                    updatedState.copy(
                        search = updatedState.search.copy(
                            status = SearchStatus.Success(serviceIds),
                        ),
                    )
                }
            }
            is Result.Error -> {
                if (query.isBlank()) {
                    updatedState.copy(
                        popular = updatedState.popular.copy(
                            status = PopularStatus.Error(searchResult.error),
                        ),
                    )
                } else {
                    updatedState.copy(
                        search = updatedState.search.copy(
                            status = SearchStatus.Error(searchResult.error),
                        ),
                    )
                }
            }
        }
    }

    private fun handleSideEffects(
        action: ServiceSelectionAction,
        newState: ServiceSelectionState
    ) {
        when (action) {
            is ServiceSelectionAction.UpdateQuery -> {
                serviceSearchManager.updateQuery(action.query)
            }
            ServiceSelectionAction.ClearSelection,
            is ServiceSelectionAction.SelectService,
            is ServiceSelectionAction.UnselectService -> {
                // No side effects
            }
        }
    }
}
