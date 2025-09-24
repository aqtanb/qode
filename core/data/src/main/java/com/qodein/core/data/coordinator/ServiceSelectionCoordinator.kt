package com.qodein.core.data.coordinator

import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.toErrorType
import com.qodein.shared.domain.manager.ServiceSearchManager
import com.qodein.shared.domain.service.ServiceCache
import com.qodein.shared.domain.service.selection.SearchStatus
import com.qodein.shared.domain.service.selection.ServiceSelectionAction
import com.qodein.shared.domain.service.selection.ServiceSelectionManager
import com.qodein.shared.domain.service.selection.ServiceSelectionState
import com.qodein.shared.model.Service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Coordinates service selection between ServiceSearchManager and ServiceSelectionManager.
 * Eliminates code duplication between ViewModels and centralizes complex state management.
 */
@Singleton
class ServiceSelectionCoordinator @Inject constructor(
    private val serviceSearchManager: ServiceSearchManager,
    private val serviceSelectionManager: ServiceSelectionManager,
    private val serviceCache: ServiceCache
) {

    /**
     * Sets up service selection coordination with state updates callback.
     * Handles the complex logic of combining search results, popular services, and caching.
     */
    fun setupServiceSelection(
        scope: CoroutineScope,
        getCurrentState: () -> ServiceSelectionState,
        onStateUpdate: (ServiceSelectionState) -> Unit,
        onCachedServicesUpdate: ((Map<String, Service>) -> Unit)? = null
    ) {
        serviceSearchManager.activate()

        scope.launch {
            // Combine all relevant flows
            val flows = if (onCachedServicesUpdate != null) {
                // Include cached services for ViewModels that need them (like HomeViewModel)
                combine(
                    serviceSearchManager.searchQuery,
                    serviceSearchManager.searchResult,
                    serviceCache.services,
                ) { query, searchResult, cachedServices ->
                    Triple(query, searchResult, cachedServices)
                }
            } else {
                // Simple version for ViewModels that don't need cached services in UI state
                combine(
                    serviceSearchManager.searchQuery,
                    serviceSearchManager.searchResult,
                ) { query, searchResult ->
                    Triple(query, searchResult, emptyMap<String, Service>())
                }
            }

            flows.collect { (query, searchResult, cachedServices) ->
                val currentState = getCurrentState()
                val updatedState = processServiceSelectionUpdate(
                    currentState = currentState,
                    query = query,
                    searchResult = searchResult,
                )

                onStateUpdate(updatedState)

                // Update cached services if callback provided
                onCachedServicesUpdate?.invoke(cachedServices)
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
     * Exposes the service cache for UI components that need direct access to cached services.
     */
    val cachedServices: StateFlow<Map<String, Service>> get() = serviceCache.services

    /**
     * Deactivates the service search manager.
     */
    fun deactivate() {
        serviceSearchManager.deactivate()
    }

    private fun processServiceSelectionUpdate(
        currentState: ServiceSelectionState,
        query: String,
        searchResult: Result<List<Service>>
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

        // Process search results
        return when (searchResult) {
            is Result.Loading -> {
                if (query.isBlank()) {
                    // Loading popular services
                    serviceSelectionManager.applyAction(updatedState, ServiceSelectionAction.LoadPopularServices)
                } else {
                    // Loading search results
                    updatedState.copy(
                        search = updatedState.search.copy(
                            status = SearchStatus.Loading,
                        ),
                    )
                }
            }
            is Result.Success -> {
                // Cache the services for lookup
                serviceCache.addServices(searchResult.data)

                if (query.isBlank()) {
                    // Popular services loaded
                    serviceSelectionManager.applyAction(
                        updatedState,
                        ServiceSelectionAction.SetPopularServices(searchResult.data.map { it.id }),
                    )
                } else {
                    // Search results loaded
                    updatedState.copy(
                        search = updatedState.search.copy(
                            status = SearchStatus.Success(searchResult.data.map { it.id }),
                        ),
                    )
                }
            }
            is Result.Error -> {
                if (query.isBlank()) {
                    // Popular services error
                    serviceSelectionManager.applyAction(
                        updatedState,
                        ServiceSelectionAction.SetPopularServicesError(searchResult.exception),
                    )
                } else {
                    // Search error
                    updatedState.copy(
                        search = updatedState.search.copy(
                            status = SearchStatus.Error(searchResult.exception.toErrorType()),
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
            ServiceSelectionAction.LoadPopularServices,
            ServiceSelectionAction.RetryPopularServices -> {
                serviceSearchManager.clearQuery() // Load popular services
            }
            ServiceSelectionAction.RetrySearch -> {
                if (newState.search.isSearching) {
                    serviceSearchManager.updateQuery(newState.search.query)
                }
            }
            // Other actions are pure state changes, no side effects needed
            else -> { /* No side effects needed */ }
        }
    }
}
