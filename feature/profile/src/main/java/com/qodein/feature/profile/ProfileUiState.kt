package com.qodein.feature.profile

import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.User

sealed interface ProfileUiState {
    data class Success(val user: User) : ProfileUiState

    data object Loading : ProfileUiState

    data class Error(
        val errorType: OperationError,
        val isRetryable: Boolean,
        val shouldShowSnackbar: Boolean = true,
        val errorCode: String? = null
    ) : ProfileUiState
}
