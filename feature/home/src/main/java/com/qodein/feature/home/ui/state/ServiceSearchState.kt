package com.qodein.feature.home.ui.state

import com.qodein.shared.common.result.ErrorType
import com.qodein.shared.model.Service

/**
 * Service search state for filter dialogs
 * Follows standardized Result-based error handling pattern
 */
data class ServiceSearchState(val query: String = "", val state: SearchResultState = SearchResultState.Empty) {
    val isSearching: Boolean get() = query.length >= 2
    val isLoading: Boolean get() = state is SearchResultState.Loading
}

sealed class SearchResultState {
    data object Empty : SearchResultState()
    data object Loading : SearchResultState()
    data class Success(val services: List<Service>) : SearchResultState() {
        val isEmpty: Boolean get() = services.isEmpty()
        val hasResults: Boolean get() = services.isNotEmpty()
    }
    data class Error(val errorType: ErrorType, val isRetryable: Boolean, val shouldShowSnackbar: Boolean, val errorCode: String?) :
        SearchResultState()
}
