package com.qodein.qode.ui.state

import com.qodein.qode.navigation.NavigationActions

/**
 * UI-specific events for the QodeApp.
 *
 * This sealed interface extends the existing NavigationActions pattern
 * to handle UI-only events that don't involve navigation routing.
 *
 * Benefits:
 * - Leverages existing NavigationActions architecture
 * - Clean separation between navigation and UI-only events
 * - Type-safe event handling
 * - Easy to test and mock
 */
sealed interface AppUiEvents {

    /**
     * Navigation events - delegates to existing NavigationActions
     */
    data class Navigate(val action: NavigationActions) : AppUiEvents

    /**
     * UI-only events that don't involve navigation routing
     */

    /**
     * Show "Coming Soon" dialog for unimplemented features
     */
    data object ShowComingSoonDialog : AppUiEvents

    /**
     * Dismiss "Coming Soon" dialog
     */
    data object DismissComingSoonDialog : AppUiEvents

    /**
     * Show coming soon specifically for favorites feature
     * (Currently routed through profile)
     */
    data object ShowFavoritesComingSoon : AppUiEvents

    /**
     * Show coming soon for inbox feature
     */
    data object ShowInboxComingSoon : AppUiEvents
}

/**
 * Extension functions to easily convert NavigationActions to AppUiEvents
 */
fun NavigationActions.toUiEvent(): AppUiEvents = AppUiEvents.Navigate(this)
