package com.qodein.feature.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.qodein.feature.auth.AuthScreen
import kotlinx.serialization.Serializable

@Serializable object AuthBaseRoute

@Serializable object AuthRoute

// NavController: do what, NavOptions: how
fun NavController.navigateToAuth(navOptions: NavOptions? = null) {
    navigate(route = AuthRoute, navOptions = navOptions)
}

// NavGraphBuilder: what
fun NavGraphBuilder.authSection(
    onAuthSuccess: () -> Unit = {},
    onBackClick: () -> Unit = {},
    isDarkTheme: Boolean
) {
    navigation<AuthBaseRoute>(startDestination = AuthRoute) {
        composable<AuthRoute> {
            AuthScreen(
                onNavigateToHome = onAuthSuccess,
                isDarkTheme = isDarkTheme,
            )
        }
    }
}
