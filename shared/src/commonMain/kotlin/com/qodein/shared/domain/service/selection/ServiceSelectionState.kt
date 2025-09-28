package com.qodein.shared.domain.service.selection

import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.ServiceId

data class PopularServices(val ids: List<ServiceId> = emptyList(), val status: PopularStatus = PopularStatus.Idle)

sealed class PopularStatus {
    data object Idle : PopularStatus()
    data object Loading : PopularStatus()
    data class Error(val error: OperationError) : PopularStatus()
}

data class ServiceSelectionState(
    val search: SearchState = SearchState(),
    val popular: PopularServices = PopularServices(),
    val selection: SelectionState = SelectionState.Single()
)
