package com.qodein.feature.service.selection

import com.qodein.shared.model.ServiceId

sealed interface ServiceSelectionEvent {
    data class ServiceSelected(val selectedServiceIds: Set<ServiceId>) : ServiceSelectionEvent
}
