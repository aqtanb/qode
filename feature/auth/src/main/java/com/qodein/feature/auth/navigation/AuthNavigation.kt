package com.qodein.feature.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.dialog
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.qodein.core.ui.AuthPromptAction
import com.qodein.feature.auth.AuthPromptScreen
import com.qodein.feature.auth.AuthRoute
import kotlinx.serialization.Serializable

@Serializable object AuthBaseRoute

@Serializable object AuthRoute

@Serializable data class AuthBottomSheetRoute(val authPromptAction: AuthPromptAction)

fun NavController.navigateToAuth(navOptions: NavOptions? = null) {
    navigate(route = AuthRoute, navOptions = navOptions)
}

fun NavController.navigateToAuthBottomSheet(
    authPromptAction: AuthPromptAction,
    navOptions: NavOptions? = null
) {
    navigate(route = AuthBottomSheetRoute(authPromptAction), navOptions = navOptions)
}

fun NavGraphBuilder.authSection(navController: NavController) {
    navigation<AuthBaseRoute>(
        startDestination = AuthBottomSheetRoute(authPromptAction = AuthPromptAction.Profile),
    ) {
        dialog<AuthBottomSheetRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<AuthBottomSheetRoute>()
            AuthPromptScreen(
                authPromptAction = args.authPromptAction,
                navController = navController,
            )
        }
    }
}
