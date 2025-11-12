package com.qodein.shared.domain.manager

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
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
    val searchResult: Flow<Result<List<Service>, OperationError>>

    /**
     * Cached services map (serviceId -> Service) for lookup.
     * Contains all services that have been fetched (popular + search results).
     */
    val cachedServices: StateFlow<Map<String, Service>>

    /**
     * Update the search query. Triggers debounced search.
     * Empty or blank query will load popular services.
     */
    fun updateQuery(query: String)

    /**
     * Clear the search query and reset to popular services.
     */
    fun clearQuery()

    /**
     * Activate service search. Must be called before any search operations.
     * This prevents expensive service loading on app start.
     */
    fun activate()

    /**
     * Deactivate service search to stop all operations.
     */
    fun deactivate()
}
