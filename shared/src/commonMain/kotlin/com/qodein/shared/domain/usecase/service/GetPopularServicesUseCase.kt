package com.qodein.shared.domain.usecase.service

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.PromocodeRepository
import com.qodein.shared.model.Service
import kotlinx.coroutines.flow.Flow

/**
 * Use case for getting popular services.
 */
class GetPopularServicesUseCase(private val repository: PromocodeRepository) {

    /**
     * Get popular services.
     *
     * @param limit Maximum number of results (default: 20)
     * @return Flow<Result<List<Service>, OperationError>> with popular services
     */
    operator fun invoke(limit: Int = 20): Flow<Result<List<Service>, OperationError>> = repository.getPopularServices(limit)
}
