package com.qodein.shared.domain.service.selection

import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.ServiceId

sealed interface ServiceSelectionAction {
    // Search
    data class UpdateQuery(val query: String) : ServiceSelectionAction
    data object ClearQuery : ServiceSelectionAction
    data object RetrySearch : ServiceSelectionAction

    // Selection
    data class SelectService(val id: ServiceId) : ServiceSelectionAction
    data class UnselectService(val id: ServiceId) : ServiceSelectionAction
    data object ClearSelection : ServiceSelectionAction

    // Popular services
    data object LoadPopularServices : ServiceSelectionAction
    data object RetryPopularServices : ServiceSelectionAction
    data class SetPopularServices(val ids: List<ServiceId>) : ServiceSelectionAction
    data class SetPopularServicesError(val error: OperationError) : ServiceSelectionAction

    // Focus actions (handled at UI level)
    data class SetSearchFocus(val focused: Boolean) : ServiceSelectionAction
}
