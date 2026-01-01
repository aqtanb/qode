package com.qodein.feature.service.selection

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.usecase.service.SearchServicesUseCase
import com.qodein.shared.model.Service
import com.qodein.shared.model.ServiceId
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ServiceSelectionViewModel(internal val savedStateHandle: SavedStateHandle, private val searchServicesUseCase: SearchServicesUseCase) :
    ViewModel() {

    private val _uiState = MutableStateFlow(ServiceSelectionUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ServiceSelectionEvent>()
    val events = _events.asSharedFlow()

    private val queryFlow = MutableStateFlow("")
    private var isInitialized = false

    init {
        Logger.d("ServiceSelectionViewModel") { "init: Initializing ViewModel" }
        observeServiceSearch()
    }

    fun initialize(
        initialServiceIds: Set<ServiceId>,
        isSingleSelection: Boolean
    ) {
        if (isInitialized) return
        isInitialized = true

        Logger.d("ServiceSelectionViewModel") {
            "initialize: initialServiceIds=${initialServiceIds.size}, isSingleSelection=$isSingleSelection"
        }

        _uiState.update {
            it.copy(
                selectedServiceIds = initialServiceIds,
                selectionMode = if (isSingleSelection) SelectionMode.Single else SelectionMode.Multi,
            )
        }
    }

    internal fun onAction(action: ServiceSelectionAction) {
        when (action) {
            is ServiceSelectionAction.UpdateQuery -> updateSearchQuery(action.query)
            is ServiceSelectionAction.ToggleService -> toggleServiceSelection(action.id)
            ServiceSelectionAction.RetryLoadServices -> retryLoadServices()
        }
    }

    private fun observeServiceSearch() {
        viewModelScope.launch {
            searchServicesUseCase(queryFlow).collect { result ->
                handleServiceSearchResult(result)
            }
        }
    }

    private fun handleServiceSearchResult(result: Result<List<Service>, OperationError>) {
        val isSearching = queryFlow.value.isNotEmpty()

        _uiState.update { state ->
            if (isSearching) {
                state.copy(searchStatus = result.toSearchStatus())
            } else {
                state.copy(popularStatus = result.toPopularStatus())
            }
        }
    }

    private fun updateSearchQuery(query: String) {
        val isSearching = query.isNotEmpty()

        _uiState.update { state ->
            state.copy(
                searchText = query,
                searchStatus = if (isSearching) SearchUiState.Loading else state.searchStatus,
                popularStatus = if (!isSearching) PopularStatus.Loading else state.popularStatus,
            )
        }

        queryFlow.value = query
    }

    private fun toggleServiceSelection(serviceId: ServiceId) {
        _uiState.update { state ->
            val updatedSelection = when (state.selectionMode) {
                SelectionMode.Single -> setOf(serviceId)
                SelectionMode.Multi -> state.selectedServiceIds.toggle(serviceId)
            }

            Logger.d("ServiceSelectionViewModel") {
                "toggleServiceSelection: serviceId=$serviceId, mode=${state.selectionMode}, newSelection=${updatedSelection.size}"
            }

            state.copy(selectedServiceIds = updatedSelection)
        }

        if (_uiState.value.selectionMode == SelectionMode.Single) {
            emitSelectionComplete()
        }
    }

    private fun retryLoadServices() {
        queryFlow.value = _uiState.value.searchText
    }

    private fun emitSelectionComplete() {
        viewModelScope.launch {
            Logger.d("ServiceSelectionViewModel") {
                "emitSelectionComplete: Emitting selection with ${_uiState.value.selectedServiceIds.size} services"
            }
            _events.emit(
                ServiceSelectionEvent.ServiceSelected(_uiState.value.selectedServiceIds),
            )
        }
    }

    private fun Result<List<Service>, OperationError>.toSearchStatus(): SearchUiState =
        when (this) {
            is Result.Success -> SearchUiState.Success(data)
            is Result.Error -> SearchUiState.Error(error)
        }

    private fun Result<List<Service>, OperationError>.toPopularStatus(): PopularStatus =
        when (this) {
            is Result.Success -> PopularStatus.Success(data)
            is Result.Error -> PopularStatus.Error(error)
        }

    private fun Set<ServiceId>.toggle(id: ServiceId): Set<ServiceId> = if (id in this) this - id else this + id
}
