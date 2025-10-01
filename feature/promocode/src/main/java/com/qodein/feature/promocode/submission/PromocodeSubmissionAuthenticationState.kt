package com.qodein.feature.promocode.submission

import com.qodein.shared.model.User

/**
 * Clean authentication domain state without UI concerns.
 *
 * Each state stores only essential domain data.
 * UI behavior should be derived in presentation layer.
 */
sealed interface PromocodeSubmissionAuthenticationState {
    data object Loading : PromocodeSubmissionAuthenticationState
    data object Unauthenticated : PromocodeSubmissionAuthenticationState
    data class Authenticated(val user: User) : PromocodeSubmissionAuthenticationState
    data class Error(val throwable: Throwable) : PromocodeSubmissionAuthenticationState
}
