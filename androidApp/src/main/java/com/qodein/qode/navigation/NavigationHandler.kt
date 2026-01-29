package com.qodein.qode.navigation

import androidx.navigation.NavController
import com.qodein.core.ui.AuthPromptAction
import com.qodein.feature.auth.navigation.navigateToAuthBottomSheet
import com.qodein.feature.post.navigation.navigateToPostSubmission
import com.qodein.feature.promocode.navigation.navigateToPromocodeSubmission
import com.qodein.shared.domain.AuthState

class NavigationHandler {

    /**
     * Handle navigation action with auth state awareness
     *
     * @param action The navigation action to handle
     * @param navController Navigation controller for actual navigation
     * @param authState Current authentication state (null = loading)
     * @param navigateToTopLevel Function to navigate to top level destinations
     */
    fun handleNavigation(
        action: NavigationActions,
        navController: NavController,
        authState: AuthState?,
        navigateToTopLevel: (TopLevelDestination) -> Unit
    ) {
        when (action) {
            is NavigationActions.NavigateToTab -> {
                navigateToTopLevel(action.destination)
            }

            NavigationActions.NavigateBack -> {
                if (!navController.popBackStack()) {
                    // If no back stack, navigate to home
                    navigateToTopLevel(TopLevelDestination.HOME)
                }
            }

            NavigationActions.NavigateToPromocodeSubmission -> {
                when (authState) {
                    is AuthState.Authenticated -> navController.navigateToPromocodeSubmission()
                    is AuthState.Unauthenticated -> navController.navigateToAuthBottomSheet(AuthPromptAction.SubmitPromocode)
                    null -> {
                        // Auth state is loading, wait for it to resolve
                    }
                }
            }

            NavigationActions.NavigateToPostSubmission -> {
                when (authState) {
                    is AuthState.Authenticated -> navController.navigateToPostSubmission()
                    is AuthState.Unauthenticated -> navController.navigateToAuthBottomSheet(AuthPromptAction.CreatePost)
                    null -> {
                        // Auth state is loading, wait for it to resolve
                    }
                }
            }
        }
    }
}
