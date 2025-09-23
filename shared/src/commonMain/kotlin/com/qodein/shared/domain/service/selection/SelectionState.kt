package com.qodein.shared.domain.service.selection

import com.qodein.shared.model.ServiceId

sealed class SelectionState {
    data class Single(val selectedId: ServiceId? = null) : SelectionState()
    data class Multi(val selectedIds: Set<ServiceId> = emptySet()) : SelectionState()

    fun isSelected(id: ServiceId): Boolean =
        when (this) {
            is Single -> selectedId == id
            is Multi -> id in selectedIds
        }

    val hasSelection: Boolean get() = when (this) {
        is Single -> selectedId != null
        is Multi -> selectedIds.isNotEmpty()
    }
}
