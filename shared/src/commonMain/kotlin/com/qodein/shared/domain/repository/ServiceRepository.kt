package com.qodein.shared.domain.repository

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.Service
import com.qodein.shared.model.ServiceId
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Service operations.
 */
interface ServiceRepository {

    /**
     * Get service by ID.
     */
    suspend fun getById(id: ServiceId): Result<Service, OperationError>

    /**
     * Find service by name (case-insensitive).
     * Returns error if not found.
     */
    suspend fun findByName(name: String): Result<Service, OperationError>

    /**
     * Create a new service.
     */
    suspend fun create(service: Service): Result<Service, OperationError>

    /**
     * Search for services by query string.
     */
    fun searchServices(
        query: String,
        limit: Int
    ): Flow<Result<List<Service>, OperationError>>

    /**
     * Get popular services sorted by promo code count.
     */
    fun getPopularServices(limit: Long): Flow<Result<List<Service>, OperationError>>
}
