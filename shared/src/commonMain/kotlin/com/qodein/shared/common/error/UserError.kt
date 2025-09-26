package com.qodein.shared.common.error

/**
 * Domain errors for User/Auth operations.
 * User-focused hierarchical errors that abstract away implementation details.
 */
sealed interface UserError : OperationError {

    /**
     * Failures when user tries to sign in/out or authenticate.
     */
    sealed interface AuthenticationFailure : UserError {
        data object Cancelled : AuthenticationFailure
        data object InvalidCredentials : AuthenticationFailure
        data object ServiceUnavailable : AuthenticationFailure
        data object TooManyAttempts : AuthenticationFailure
    }

    /**
     * Failures when user tries to access/update their profile.
     */
    sealed interface ProfileFailure : UserError {
        data object NotFound : ProfileFailure
        data object AccessDenied : ProfileFailure
        data object DataCorrupted : ProfileFailure
        data object UpdateFailed : ProfileFailure
    }
}
