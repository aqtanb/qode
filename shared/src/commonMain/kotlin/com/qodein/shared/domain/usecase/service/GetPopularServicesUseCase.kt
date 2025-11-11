package com.qodein.shared.domain.usecase.service

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.ServiceRepository
import com.qodein.shared.model.Service
import kotlinx.coroutines.flow.Flow

/**
 * Use case for getting popular services.
 */
class GetPopularServicesUseCase(private val repository: ServiceRepository) {

    companion object {
        private const val DEFAULT_LIMIT = 20L
    }

    /**
     * Get popular services.
     *
     * @param limit Maximum number of results
     * @return Flow<Result<List<Service>, OperationError>> with popular services
     */
    operator fun invoke(limit: Long = DEFAULT_LIMIT): Flow<Result<List<Service>, OperationError>> = repository.getPopularServices(limit)
}
