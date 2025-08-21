package com.qodein.shared.domain.usecase.service

import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.asResult
import com.qodein.shared.domain.repository.PromoCodeRepository
import com.qodein.shared.model.Service
import kotlinx.coroutines.flow.Flow

/**
 * Use case for searching services by query text.
 *
 * Searches services by name and category, with intelligent ranking:
 * - Exact name matches first
 * - Name prefix matches second
 * - Category matches third
 * - Popular services fourth
 * - Others last
 */
class SearchServicesUseCase constructor(private val repository: PromoCodeRepository) {
    /**
     * Search for services matching the query.
     *
     * @param query Search query text
     * @param limit Maximum number of results (default: 20)
     * @return Flow<Result<List<Service>>> with search results
     */
    operator fun invoke(
        query: String,
        limit: Int = 20
    ): Flow<Result<List<Service>>> = repository.searchServices(query.trim(), limit).asResult()
}
