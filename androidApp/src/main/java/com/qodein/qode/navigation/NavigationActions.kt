package com.qodein.qode.navigation

sealed interface NavigationActions {
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
     * Navigate to submission/promocode creation screen
     */
    data object NavigateToPromocodeSubmission : NavigationActions
    data object NavigateToPostSubmission : NavigationActions
}
