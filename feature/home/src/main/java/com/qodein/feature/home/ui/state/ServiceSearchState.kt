package com.qodein.feature.home.ui.state

import com.qodein.shared.common.result.ErrorType
import com.qodein.shared.model.Service

/**
 * Service search state for filter dialogs
 * Follows standardized Result-based error handling pattern
 */
sealed class ServiceSearchState {
    data object Empty : ServiceSearchState()
    data object Loading : ServiceSearchState()
    data class Success(val services: List<Service>) : ServiceSearchState() {
        val isEmpty: Boolean get() = services.isEmpty()
        val hasResults: Boolean get() = services.isNotEmpty()
    }
    data class Error(val errorType: ErrorType, val isRetryable: Boolean, val shouldShowSnackbar: Boolean, val errorCode: String?) :
        ServiceSearchState()
}
