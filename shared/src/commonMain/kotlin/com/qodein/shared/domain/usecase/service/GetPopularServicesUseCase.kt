package com.qodein.shared.domain.usecase.service

import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.asResult
import com.qodein.shared.domain.repository.PromoCodeRepository
import com.qodein.shared.model.Service
import kotlinx.coroutines.flow.Flow

/**
 * Use case for getting popular services for quick selection.
 *
 * Returns services marked as popular, useful for:
 * - Default dropdown options
 * - Quick service selection
 * - Fallback when search query is empty
 */
class GetPopularServicesUseCase constructor(private val repository: PromoCodeRepository) {
    /**
     * Get popular services.
     *
     * @param limit Maximum number of results (default: 10)
     * @return Flow<Result<List<Service>>> with popular services
     */
    operator fun invoke(limit: Int = 10): Flow<Result<List<Service>>> = repository.getPopularServices(limit).asResult()
}
