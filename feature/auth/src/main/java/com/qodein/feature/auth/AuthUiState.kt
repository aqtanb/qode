package com.qodein.feature.auth

import com.qodein.core.model.User

sealed interface AuthUiState {

    data object Idle : AuthUiState

    data object Loading : AuthUiState

    data class Success(val user: User) : AuthUiState

    data class Error(val exception: Throwable, val isRetryable: Boolean = true) : AuthUiState
}
