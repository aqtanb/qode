package com.qodein.qode.navigation

import androidx.navigation.NavController
import com.qodein.core.domain.AuthState
import com.qodein.feature.auth.navigation.navigateToAuth
import com.qodein.feature.profile.navigation.navigateToProfile
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized navigation handler that converts NavigationActions to actual navigation calls.
 *
 * Benefits:
 * - Type-safe navigation through sealed interface
 * - Single responsibility for navigation logic
 * - Easy to test and mock
 * - Auth-aware navigation routing
 * - Scalable - easy to add new navigation actions
 *
 * Following enterprise patterns for separation of concerns.
 * Located in app module to access TopLevelDestination without architecture violations.
 */
@Singleton
class NavigationHandler @Inject constructor() {

    /**
     * Handle navigation action with auth state awareness
     *
     * @param action The navigation action to handle
     * @param navController Navigation controller for actual navigation
     * @param authState Current authentication state
     * @param navigateToTopLevel Function to navigate to top level destinations
     */
    fun handleNavigation(
        action: NavigationActions,
        navController: NavController,
        authState: AuthState,
        navigateToTopLevel: (TopLevelDestination) -> Unit
    ) {
        when (action) {
            NavigationActions.NavigateToProfile -> {
                when (authState) {
                    is AuthState.Authenticated -> navController.navigateToProfile()
                    is AuthState.Unauthenticated -> navController.navigateToAuth()
                    is AuthState.Loading -> {
                        // Wait for auth state to resolve, or show loading
                        // Could implement queue for pending actions
                    }
                }
            }

            NavigationActions.NavigateToFavorites -> {
                // TODO: Implement favorites navigation when feature exists
                // For now, could navigate to profile or show coming soon
                handleNavigation(
                    NavigationActions.NavigateToProfile,
                    navController,
                    authState,
                    navigateToTopLevel,
                )
            }

            NavigationActions.NavigateToSettings -> {
                // TODO: Implement settings navigation when feature exists
                // For now, could navigate to profile or show coming soon
                handleNavigation(
                    NavigationActions.NavigateToProfile,
                    navController,
                    authState,
                    navigateToTopLevel,
                )
            }

            is NavigationActions.NavigateToTab -> {
                navigateToTopLevel(action.destination)
            }

            NavigationActions.NavigateBack -> {
                if (!navController.popBackStack()) {
                    // If no back stack, navigate to home
                    navigateToTopLevel(TopLevelDestination.HOME)
                }
            }

            NavigationActions.NavigateToHome -> {
                navigateToTopLevel(TopLevelDestination.HOME)
            }
        }
    }

    /**
     * Get appropriate navigation action for profile click based on auth state
     *
     * @param authState Current authentication state
     * @return NavigationAction for profile navigation
     */
    fun getProfileNavigationAction(authState: AuthState): NavigationActions =
        when (authState) {
            is AuthState.Authenticated -> NavigationActions.NavigateToProfile
            is AuthState.Unauthenticated -> NavigationActions.NavigateToProfile // Will route to auth
            is AuthState.Loading -> NavigationActions.NavigateToProfile // Will wait or show loading
        }
}
