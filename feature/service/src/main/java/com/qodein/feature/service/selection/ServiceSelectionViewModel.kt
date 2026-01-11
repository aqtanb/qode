package com.qodein.feature.service.selection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.domain.usecase.service.GetPopularServicesUseCase
import com.qodein.shared.domain.usecase.service.SearchServicesUseCase
import com.qodein.shared.model.Service
import com.qodein.shared.model.ServiceId
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ServiceSelectionViewModel(
    private val getPopularServicesUseCase: GetPopularServicesUseCase,
    private val searchServicesUseCase: SearchServicesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServiceSelectionUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ServiceSelectionEvent>()
    val events = _events.asSharedFlow()

    private val searchQueryFlow = MutableStateFlow("")

    init {
        Logger.d("ServiceSelectionViewModel") { "init: Initializing ViewModel" }
        loadPopularServices()
        observeSearchQuery()
    }

    fun initialize(
        initialServiceIds: Set<ServiceId>,
        isSingleSelection: Boolean
    ) {
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

    private fun loadPopularServices() {
        viewModelScope.launch {
            _uiState.update { it.copy(popularStatus = PopularStatus.Loading) }
            try {
                val result = getPopularServicesUseCase()
                _uiState.update { it.copy(popularStatus = result.toPopularStatus()) }
            } catch (e: Exception) {
                Logger.e("ServiceSelectionViewModel", e) { "Failed to load popular services" }
                _uiState.update { it.copy(popularStatus = PopularStatus.Error(SystemError.Unknown)) }
            }
        }
    }

    private fun observeSearchQuery() {
        viewModelScope.launch {
            searchServicesUseCase(searchQueryFlow.filter { it.isNotEmpty() })
                .collect { result ->
                    _uiState.update { it.copy(searchStatus = result.toSearchStatus()) }
                }
        }
    }

    private fun updateSearchQuery(query: String) {
        _uiState.update { state ->
            state.copy(
                searchText = query,
                searchStatus = if (query.isNotEmpty()) SearchUiState.Loading else SearchUiState.Idle,
            )
        }

        searchQueryFlow.value = query
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
        if (_uiState.value.searchText.isEmpty()) {
            loadPopularServices()
        } else {
            searchQueryFlow.value = _uiState.value.searchText
        }
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
