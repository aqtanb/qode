package com.qodein.core.domain.usecase.service

import com.qodein.core.domain.repository.PromoCodeRepository
import com.qodein.core.model.Service
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case for getting popular services for quick selection.
 *
 * Returns services marked as popular, useful for:
 * - Default dropdown options
 * - Quick service selection
 * - Fallback when search query is empty
 */
class GetPopularServicesUseCase @Inject constructor(private val repository: PromoCodeRepository) {
    /**
     * Get popular services.
     *
     * @param limit Maximum number of results (default: 10)
     * @return Flow<Result<List<Service>>> with popular services
     */
    operator fun invoke(limit: Int = 10): Flow<Result<List<Service>>> =
        repository.getPopularServices(limit)
            .map { services -> Result.success(services) }
            .catch { exception -> emit(Result.failure(exception)) }
}
