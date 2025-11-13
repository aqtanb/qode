package com.qodein.shared.domain.coordinator

import co.touchlab.kermit.Logger
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.service.selection.PopularStatus
import com.qodein.shared.domain.service.selection.SearchStatus
import com.qodein.shared.domain.service.selection.ServiceSelectionAction
import com.qodein.shared.domain.service.selection.ServiceSelectionManager
import com.qodein.shared.domain.service.selection.ServiceSelectionState
import com.qodein.shared.domain.usecase.service.GetPopularServicesUseCase
import com.qodein.shared.domain.usecase.service.SearchServicesUseCase
import com.qodein.shared.model.Service
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

/**
 * Coordinates service selection and search.
 * Handles debounced search, popular services fallback, and selection state management.
 * Eliminates code duplication between ViewModels.
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class ServiceSelectionCoordinator(
    private val searchServicesUseCase: SearchServicesUseCase,
    private val getPopularServicesUseCase: GetPopularServicesUseCase,
    private val serviceSelectionManager: ServiceSelectionManager
) {
    private var collectionJob: Job? = null
    private val searchQuery = MutableStateFlow("")
    private val isActive = MutableStateFlow(false)

    /**
     * Sets up service selection coordination with state updates callback.
     * Handles debounced search, popular services fallback, and state updates.
     */
    fun setupServiceSelection(
        scope: CoroutineScope,
        getCurrentState: () -> ServiceSelectionState,
        onStateUpdate: (ServiceSelectionState) -> Unit
    ) {
        collectionJob?.cancel()

        isActive.value = true
        searchQuery.value = ""

        val initialState = getCurrentState().copy(
            popular = getCurrentState().popular.copy(
                status = PopularStatus.Loading,
            ),
        )
        onStateUpdate(initialState)
        collectionJob = scope.launch {
            searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    when {
                        !isActive.value -> flowOf(Result.Success(emptyList()))
                        query.isBlank() -> getPopularServicesUseCase()
                        else -> searchServicesUseCase(query)
                    }
                }
                .collect { searchResult ->
                    val currentState = getCurrentState()
                    val query = searchQuery.value

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

        if (action is ServiceSelectionAction.UpdateQuery) {
            searchQuery.value = action.query
        }

        return newState
    }

    /**
     * Deactivates search and cancels any running collection jobs.
     */
    fun deactivate() {
        collectionJob?.cancel()
        collectionJob = null
        isActive.value = false
        searchQuery.value = ""
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
                Logger.d("ServiceSelectionCoordinator") {
                    "processServiceSelectionUpdate: Success with ${searchResult.data.size} services, query='$query'"
                }

                if (query.isBlank()) {
                    Logger.d("ServiceSelectionCoordinator") { "Setting popular services: ${searchResult.data.size} services" }
                    updatedState.copy(
                        popular = updatedState.popular.copy(
                            services = searchResult.data,
                            status = PopularStatus.Success,
                        ),
                    )
                } else {
                    Logger.d("ServiceSelectionCoordinator") { "Setting search results: ${searchResult.data.size} services" }
                    updatedState.copy(
                        search = updatedState.search.copy(
                            status = SearchStatus.Success(searchResult.data),
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
}
