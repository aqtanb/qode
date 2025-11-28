package com.qodein.shared.domain.service.selection

import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.Service

data class PopularServices(val services: List<Service> = emptyList(), val status: PopularStatus = PopularStatus.Success)

sealed class PopularStatus {
    data object Success : PopularStatus()
    data object Loading : PopularStatus()
    data class Error(val error: OperationError) : PopularStatus()
}

data class ServiceSelectionState(
    val search: SearchState = SearchState(),
    val popular: PopularServices = PopularServices(),
    val selection: SelectionState = SelectionState.Single()
)
