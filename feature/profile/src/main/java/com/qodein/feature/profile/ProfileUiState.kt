package com.qodein.feature.profile

import com.qodein.core.model.User

sealed interface ProfileUiState {
    data class Success(val user: User) : ProfileUiState

    data object Loading : ProfileUiState

    data class Error(val exception: Throwable, val isRetryable: Boolean = true) : ProfileUiState
}
