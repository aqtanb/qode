package com.qodein.core.data.manager

import com.qodein.shared.common.result.Result
import com.qodein.shared.domain.manager.ServiceSearchManager
import com.qodein.shared.domain.usecase.service.GetPopularServicesUseCase
import com.qodein.shared.domain.usecase.service.SearchServicesUseCase
import com.qodein.shared.model.Service
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production implementation of ServiceSearchManager.
 *
 * Handles debounced service search with popular services fallback following
 * the established patterns from HomeViewModel and SubmissionWizardViewModel.
 *
 * Uses 300ms debouncing as per existing codebase standards.
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@Singleton
class ServiceSearchManagerImpl @Inject constructor(
    private val searchServicesUseCase: SearchServicesUseCase,
    private val getPopularServicesUseCase: GetPopularServicesUseCase
) : ServiceSearchManager {

    private val _searchQuery = MutableStateFlow("")
    override val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    override val searchResult: Flow<Result<List<Service>>> = _searchQuery
        .debounce(300) // 300ms debounce as per existing codebase standards
        .distinctUntilChanged()
        .flatMapLatest { query ->
            when {
                query.isBlank() -> getPopularServicesUseCase(limit = 20)
                else -> searchServicesUseCase(query = query, limit = 5)
            }
        }

    override fun updateQuery(query: String) {
        _searchQuery.value = query
    }

    override fun clearQuery() {
        _searchQuery.value = ""
    }
}
