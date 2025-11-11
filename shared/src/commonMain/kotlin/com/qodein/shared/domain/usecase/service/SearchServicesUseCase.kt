package com.qodein.shared.domain.usecase.service

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.ServiceRepository
import com.qodein.shared.model.Service
import kotlinx.coroutines.flow.Flow

/**
 * Use case for searching services by query text.
 */
class SearchServicesUseCase(private val repository: ServiceRepository) {

    companion object {
        private const val DEFAULT_LIMIT = 5
    }

    /**
     * Search for services matching the query.
     *
     * @param query Search query text
     * @param limit Maximum number of results
     * @return Flow<Result<List<Service>, OperationError>> with search results
     */
    operator fun invoke(
        query: String,
        limit: Int = DEFAULT_LIMIT
    ): Flow<Result<List<Service>, OperationError>> = repository.searchServices(query, limit)
}
