package com.qodein.shared.domain.usecase.service

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.ServiceRepository
import com.qodein.shared.model.Service
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest

/**
 * Use case for searching services by query text.
 */
class SearchServicesUseCase(private val serviceRepository: ServiceRepository) {

    companion object {
        private const val DEFAULT_LIMIT = 5
        private const val SEARCH_DEBOUNCE_MS = 700L
    }

    /**
     * Search for services matching the query stream.
     *
     * @param queryFlow Stream of user-entered queries to search for
     * @return Flow<Result<List<Service>, OperationError>> with search results
     */
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    operator fun invoke(queryFlow: Flow<String>): Flow<Result<List<Service>, OperationError>> =
        queryFlow
            .debounce(SEARCH_DEBOUNCE_MS)
            .distinctUntilChanged()
            .mapLatest { query ->
                if (query.any { it.isLetterOrDigit() }) {
                    serviceRepository.searchServices(query, DEFAULT_LIMIT)
                } else {
                    Result.Success(emptyList())
                }
            }
}
