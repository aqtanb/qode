package com.qodein.feature.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.qodein.feature.auth.AuthRoute
import kotlinx.serialization.Serializable

@Serializable object AuthBaseRoute

@Serializable object AuthRoute

fun NavController.navigateToAuth(navOptions: NavOptions? = null) {
    navigate(route = AuthRoute, navOptions = navOptions)
}

fun NavGraphBuilder.authSection(
    onNavigateBack: () -> Unit,
    isDarkTheme: Boolean
) {
    navigation<AuthBaseRoute>(startDestination = AuthRoute) {
        composable<AuthRoute> {
            AuthRoute(
                onNavigateToHome = onNavigateBack,
                isDarkTheme = isDarkTheme,
                onNavigateToTermsOfService = { },
                onNavigateToPrivacyPolicy = { },
            )
        }
    }
}
