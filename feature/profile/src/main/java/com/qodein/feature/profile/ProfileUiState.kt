package com.qodein.feature.profile

import com.qodein.core.model.User

sealed interface ProfileUiState {

    data object Loading : ProfileUiState

    data object NotSignedIn : ProfileUiState

    data class SignedIn(val user: User) : ProfileUiState

    data class Error(val message: String, val isRetryable: Boolean = true) : ProfileUiState
}
