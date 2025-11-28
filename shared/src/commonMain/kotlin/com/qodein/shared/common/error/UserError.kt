package com.qodein.shared.common.error

/**
 * Domain errors for User/Auth operations.
 * User-focused hierarchical errors that abstract away implementation details.
 */
sealed interface UserError : OperationError {

    /**
     * Failures when creating/registering a user (client-side validation).
     */
    sealed interface CreationFailure : UserError {
        data object InvalidUserId : CreationFailure
        data object InvalidEmail : CreationFailure
        data object DisplayNameTooLong : CreationFailure
        data object InvalidPhotoUrl : CreationFailure
    }

    /**
     * Failures when user tries to sign in/out or authenticate.
     */
    sealed interface AuthenticationFailure : UserError {
        data object Cancelled : AuthenticationFailure
        data object InvalidCredentials : AuthenticationFailure
        data object NoCredentialsAvailable : AuthenticationFailure
        data object ServiceUnavailable : AuthenticationFailure
        data object ConfigurationError : AuthenticationFailure
        data object UnsupportedCredential : AuthenticationFailure
        data object TooManyAttempts : AuthenticationFailure
        data object AccountDisabled : AuthenticationFailure
        data object AccountConflict : AuthenticationFailure
        data object Unknown : AuthenticationFailure
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
