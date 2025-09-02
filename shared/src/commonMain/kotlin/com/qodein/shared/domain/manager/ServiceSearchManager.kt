package com.qodein.shared.domain.manager

import com.qodein.shared.common.result.Result
import com.qodein.shared.model.Service
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Manages service search functionality with debouncing and popular services fallback.
 * Provides clean Result-based API following domain layer principles.
 *
 * Interface allows for easy testing and flexibility across different implementations.
 */
interface ServiceSearchManager {
    /**
     * Current search query. Empty string means show popular services.
     */
    val searchQuery: StateFlow<String>

    /**
     * Search results as Result flow. Emits:
     * - Result.Loading when search is in progress
     * - Result.Success with services list (popular services for empty query, search results otherwise)
     * - Result.Error when search fails
     */
    val searchResult: Flow<Result<List<Service>>>

    /**
     * Update the search query. Triggers debounced search.
     * Empty or blank query will load popular services.
     */
    fun updateQuery(query: String)

    /**
     * Clear the search query and reset to popular services.
     */
    fun clearQuery()
}
