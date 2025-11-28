package com.qodein.shared.domain.usecase.service

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.ServiceRepository
import com.qodein.shared.model.Service

/**
 * Use case for getting popular services.
 */
class GetPopularServicesUseCase(private val repository: ServiceRepository) {

    companion object {
        const val DEFAULT_LIMIT = 10L
    }

    /**
     * Get popular services.
     *
     * @param limit Maximum number of results
     * @return Result<List<Service>, OperationError> with popular services
     */
    suspend operator fun invoke(limit: Long = DEFAULT_LIMIT): Result<List<Service>, OperationError> = repository.getPopularServices(limit)
}
