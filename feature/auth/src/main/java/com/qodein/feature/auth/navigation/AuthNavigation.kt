package com.qodein.feature.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import com.qodein.feature.auth.AuthScreen
import kotlinx.serialization.Serializable

@Serializable object AuthRoute

// NavController: do what, NavOptions: how
fun NavController.navigateToAuth(navOptions: NavOptions) {
    navigate(route = AuthRoute, navOptions = navOptions)
}

// NavGraphBuilder: what
fun NavGraphBuilder.authSection() {
    composable<AuthRoute> {
        AuthScreen()
    }
}
