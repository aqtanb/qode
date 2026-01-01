package com.qodein.feature.service.selection

import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.Service
import com.qodein.shared.model.ServiceId

data class ServiceSelectionUiState(
    val searchText: String = "",
    val searchStatus: SearchUiState = SearchUiState.Idle,
    val popularStatus: PopularStatus = PopularStatus.Loading,
    val selectionMode: SelectionMode = SelectionMode.Multi,
    val selectedServiceIds: Set<ServiceId> = emptySet()
)

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data class Success(val services: List<Service>) : SearchUiState
    data object Loading : SearchUiState
    data class Error(val error: OperationError) : SearchUiState
}

sealed interface PopularStatus {
    data class Success(val services: List<Service>) : PopularStatus
    data object Loading : PopularStatus
    data class Error(val error: OperationError) : PopularStatus
}

enum class SelectionMode { Single, Multi }
