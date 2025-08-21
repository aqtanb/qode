package com.qodein.feature.auth

import com.qodein.shared.common.result.ErrorType
import com.qodein.shared.model.User

sealed interface AuthUiState {

    data object Idle : AuthUiState

    data object Loading : AuthUiState

    data class Success(val user: User) : AuthUiState

    data class Error(
        val errorType: ErrorType,
        val isRetryable: Boolean,
        val shouldShowSnackbar: Boolean = true,
        val errorCode: String? = null
    ) : AuthUiState
}
