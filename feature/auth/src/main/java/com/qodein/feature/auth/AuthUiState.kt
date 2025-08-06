package com.qodein.feature.auth

import com.qodein.core.model.User

sealed interface AuthUiState {

    data object Idle : AuthUiState

    data object Loading : AuthUiState

    data class Success(val user: User) : AuthUiState

    data class Error(val message: String, val isRetryable: Boolean = true) : AuthUiState
}
