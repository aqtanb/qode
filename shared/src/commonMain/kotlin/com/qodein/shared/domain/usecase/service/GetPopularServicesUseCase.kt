package com.qodein.shared.domain.usecase.service

import com.qodein.shared.common.result.Result
import com.qodein.shared.domain.repository.PromocodeRepository
import com.qodein.shared.model.Service
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

/**
 * Use case for getting popular services with a cached fallback for reliability.
 *
 * This implementation uses standard Flow operators to correctly handle the
 * data stream, caching, and error recovery, using Kermit for logging.
 */
class GetPopularServicesUseCase(private val repository: PromocodeRepository) {
    // TODO: Simple in-memory cache for fallback, add stale-while-revalidate logic
    private var cachedServices: List<Service> = emptyList()

    /**
     * Get popular services with a cached fallback.
     *
     * @param limit Maximum number of results (default: 20)
     * @return Flow<Result<List<Service>>> with popular services
     */
    operator fun invoke(limit: Int = 20): Flow<Result<List<Service>>> =
        flow {
            emit(Result.Loading)

            try {
                val freshServices = repository.getPopularServices(limit).first()

                if (freshServices.isNotEmpty()) {
                    cachedServices = freshServices
                    emit(Result.Success(freshServices))
                } else {
                    if (cachedServices.isNotEmpty()) {
                        emit(Result.Success(cachedServices))
                    } else {
                        emit(Result.Success(emptyList()))
                    }
                }
            } catch (e: Exception) {
                if (cachedServices.isNotEmpty()) {
                    emit(Result.Success(cachedServices))
                } else {
                    emit(Result.Error(e))
                }
            }
        }
}
