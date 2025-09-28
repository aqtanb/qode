package com.qodein.feature.auth

import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.User

/**
 * Sign-in screen UI state - handles screen-specific concerns.
 *
 * Used by AuthScreen to manage sign-in UI states and navigation events.
 */
sealed interface SignInUiState {

    /**
     * Initial state - ready for user interaction
     */
    data object Idle : SignInUiState

    /**
     * Sign-in operation in progress
     */
    data object Loading : SignInUiState

    /**
     * Sign-in completed successfully - ready to navigate
     */
    data class Success(val user: User) : SignInUiState

    /**
     * Sign-in failed with error
     */
    data class Error(
        val errorType: OperationError,
        val isRetryable: Boolean,
        val shouldShowSnackbar: Boolean = true,
        val errorCode: String? = null
    ) : SignInUiState
}
