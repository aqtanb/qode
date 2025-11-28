package com.qodein.shared.domain.service.selection

import com.qodein.shared.model.ServiceId

sealed interface ServiceSelectionAction {
    data class UpdateQuery(val query: String) : ServiceSelectionAction
    data class ToggleService(val id: ServiceId) : ServiceSelectionAction
    data object ClearSelection : ServiceSelectionAction
}
