package com.qodein.shared.domain.service.selection

import com.qodein.shared.model.ServiceId

sealed class SelectionState {
    data class Single(val selectedId: ServiceId? = null) : SelectionState()
    data class Multi(val selectedIds: Set<ServiceId> = emptySet()) : SelectionState()
}
