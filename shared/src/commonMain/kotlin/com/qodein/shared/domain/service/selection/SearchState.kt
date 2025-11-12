package com.qodein.shared.domain.service.selection

import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.ServiceId

/**
 * Domain state for service search functionality.
 * Encapsulates results and status fully in the sealed class to avoid contradictions.
 */
data class SearchState(val query: String = "", val status: SearchStatus = SearchStatus.Idle)

/**
 * Search status for service search
 */
sealed class SearchStatus {
    data object Idle : SearchStatus()
    data object Loading : SearchStatus()
    data class Error(val error: OperationError) : SearchStatus()
    data class Success(val ids: List<ServiceId>) : SearchStatus()
}
