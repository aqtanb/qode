package com.qodein.qode.navigation

/**
 * Type-safe navigation actions for the application.
 *
 * Benefits:
 * - Compile-time safety for navigation
 * - Easy to test and mock
 * - Clear separation between UI actions and navigation logic
 * - Scalable - easy to add new navigation actions
 *
 * Following MVI pattern where actions represent user intents.
 * Located in app module to avoid clean architecture violations.
 */
sealed interface NavigationActions {

    /**
     * Navigate to user profile (auth-aware)
     * Will go to profile if authenticated, auth screen if not
     */
    data object NavigateToProfile : NavigationActions

    /**
     * Navigate to favorites screen
     */
    data object NavigateToFavorites : NavigationActions

    /**
     * Navigate to settings screen
     */
    data object NavigateToSettings : NavigationActions

    /**
     * Navigate to a specific top-level destination tab
     */
    data class NavigateToTab(val destination: TopLevelDestination) : NavigationActions

    /**
     * Navigate back (context-aware)
     * Will use appropriate back navigation based on current context
     */
    data object NavigateBack : NavigationActions

    /**
     * Navigate to home screen (for sign out, etc.)
     */
    data object NavigateToHome : NavigationActions
}
