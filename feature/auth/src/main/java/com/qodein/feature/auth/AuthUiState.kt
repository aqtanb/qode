package com.qodein.feature.auth

import com.qodein.shared.common.error.OperationError

/**
 * Sign-in screen UI state - handles screen-specific concerns.
 *
 * Used by AuthScreen to manage sign-in UI states and navigation events.
 */
sealed interface AuthUiState {

    /**
     * Initial state - ready for user interaction
     */
    data object Idle : AuthUiState

    /**
     * Sign-in operation in progress
     */
    data object Loading : AuthUiState

    /**
     * Sign-in failed with error
     */
    data class Error(val errorType: OperationError) : AuthUiState
}
