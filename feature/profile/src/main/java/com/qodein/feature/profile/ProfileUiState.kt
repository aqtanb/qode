package com.qodein.feature.profile

import com.qodein.core.model.User

sealed interface ProfileUiState {

    data object Loading : ProfileUiState

    data class SignedIn(val user: User) : ProfileUiState

    data class Error(val exception: Throwable, val isRetryable: Boolean = true) : ProfileUiState
}
