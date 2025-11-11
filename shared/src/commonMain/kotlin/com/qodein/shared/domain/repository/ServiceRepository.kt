package com.qodein.shared.domain.repository

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.Service
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Service operations.
 */
interface ServiceRepository {

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
