package com.qodein.feature.auth

/**
 * Sign-in screen specific actions - UI interaction events.
 *
 * Handles screen-specific user interactions and navigation actions.
 */
sealed interface SignInAction {
    /**
     * User clicked the Google sign-in button
     */
    data object SignInWithGoogleClicked : SignInAction

    /**
     * User clicked retry after an error
     */
    data object RetryClicked : SignInAction

    /**
     * User dismissed an error message
     */
    data object DismissErrorClicked : SignInAction

    /**
     * User clicked Terms of Service link
     */
    data object TermsOfServiceClicked : SignInAction

    /**
     * User clicked Privacy Policy link
     */
    data object PrivacyPolicyClicked : SignInAction
}
