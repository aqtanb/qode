package com.qodein.core.data.manager

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.manager.ServiceSearchManager
import com.qodein.shared.domain.service.ServiceCache
import com.qodein.shared.domain.usecase.service.GetPopularServicesUseCase
import com.qodein.shared.domain.usecase.service.SearchServicesUseCase
import com.qodein.shared.model.Service
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

/**
 * Production implementation of ServiceSearchManager.
 *
 * Handles debounced service search with popular services fallback following
 * the established patterns from HomeViewModel and SubmissionWizardViewModel.
 *
 * Uses 300ms debouncing as per existing codebase standards.
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class ServiceSearchManagerImpl constructor(
    private val searchServicesUseCase: SearchServicesUseCase,
    private val getPopularServicesUseCase: GetPopularServicesUseCase,
    private val serviceCache: ServiceCache
) : ServiceSearchManager {

    private val _searchQuery = MutableStateFlow("")
    override val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    override val cachedServices: StateFlow<Map<String, Service>> = serviceCache.services

    private val isActive = MutableStateFlow(false)

    override val searchResult: Flow<Result<List<Service>, OperationError>> = combine(
        _searchQuery,
        isActive,
        serviceCache.services,
    ) { query, isActive, cachedServices ->
        Triple(query, isActive, cachedServices)
    }
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { (query, isActive, cachedServices) ->
            when {
                !isActive -> flowOf(Result.Success(emptyList()))
                query.isBlank() -> {
                    val cachedPopularServices = cachedServices.values.toList()
                    if (cachedPopularServices.size >= 20) {
                        flowOf(Result.Success(cachedPopularServices.take(20)))
                    } else {
                        getPopularServicesUseCase()
                    }
                }
                else -> {
                    searchServicesUseCase(query = query)
                }
            }
        }

    override fun updateQuery(query: String) {
        _searchQuery.value = query
    }

    override fun clearQuery() {
        _searchQuery.value = ""
    }

    override fun activate() {
        isActive.value = true
    }

    override fun deactivate() {
        isActive.value = false
    }
}
