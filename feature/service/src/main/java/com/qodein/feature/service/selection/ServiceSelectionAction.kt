package com.qodein.feature.service.selection

import com.qodein.shared.model.ServiceId

sealed interface ServiceSelectionAction {
    data class UpdateQuery(val query: String) : ServiceSelectionAction
    data class ToggleService(val id: ServiceId) : ServiceSelectionAction
    data object RetryLoadServices : ServiceSelectionAction
}
