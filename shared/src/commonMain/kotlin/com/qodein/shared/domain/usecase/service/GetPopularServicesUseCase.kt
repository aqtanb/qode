package com.qodein.shared.domain.usecase.service

import co.touchlab.kermit.Logger
import com.qodein.shared.common.result.Result
import com.qodein.shared.domain.repository.PromoCodeRepository
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
class GetPopularServicesUseCase(private val repository: PromoCodeRepository) {
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
            // 1. Always emit the loading state first to show a spinner on the UI.
            emit(Result.Loading)

            try {
                // 2. Try to get fresh data from the repository's Flow.
                // .first() will get the first emitted value and then complete,
                // or throw an exception if the Flow completes without emitting.
                val freshServices = repository.getPopularServices(limit).first()

                if (freshServices.isNotEmpty()) {
                    // 3. If fresh data is available, update the cache and emit success.
                    cachedServices = freshServices
                    emit(Result.Success(freshServices))
                } else {
                    // 4. If the fresh data is empty, fall back to the cache.
                    if (cachedServices.isNotEmpty()) {
                        Logger.w("Fresh data empty, using cached services.")
                        emit(Result.Success(cachedServices))
                    } else {
                        // If no cache is available, emit an empty list.
                        Logger.w("Fresh data empty and no cache available.")
                        emit(Result.Success(emptyList()))
                    }
                }
            } catch (e: Exception) {
                // 5. If an exception occurs, fall back to the cache.
                if (cachedServices.isNotEmpty()) {
                    Logger.w("Fresh data fetch failed: ${e.message}, using cached services.")
                    emit(Result.Success(cachedServices))
                } else {
                    // If the cache is also empty, emit an error.
                    Logger.e(e) { "Fresh data fetch failed and no cache available." }
                    emit(Result.Error(e))
                }
            }
        }
}
