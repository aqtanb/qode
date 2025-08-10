package com.qodein.feature.profile.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.qodein.feature.profile.ProfileScreen
import kotlinx.serialization.Serializable

@Serializable object ProfileBaseRoute

@Serializable object ProfileRoute

fun NavController.navigateToProfile(navOptions: NavOptions? = null) {
    navigate(route = ProfileRoute, navOptions = navOptions)
}

fun NavGraphBuilder.profileSection(
    onBackClick: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    navigation<ProfileBaseRoute>(startDestination = ProfileRoute) {
        composable<ProfileRoute> {
            ProfileScreen(
                onBackClick = onBackClick,
                onSignOut = onSignOut,
            )
        }
    }
}
