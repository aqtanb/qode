package com.qodein.shared.domain.service.selection

import com.qodein.shared.common.result.ErrorType
import com.qodein.shared.model.ServiceId

/**
 * Domain state for service search functionality.
 * Encapsulates results and status fully in the sealed class to avoid contradictions.
 */
data class SearchState(val query: String = "", val status: SearchStatus = SearchStatus.Idle) {
    val isSearching: Boolean get() = query.length >= 2
}

/**
 * Search status for service search
 */
sealed class SearchStatus {
    data object Idle : SearchStatus()
    data object Loading : SearchStatus()
    data class Error(val type: ErrorType) : SearchStatus()
    data class Success(val ids: List<ServiceId>) : SearchStatus()
}
