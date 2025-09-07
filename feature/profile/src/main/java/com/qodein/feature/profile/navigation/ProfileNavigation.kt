package com.qodein.feature.profile.navigation

import androidx.compose.foundation.rememberScrollState
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
    onSignOut: () -> Unit = {},
    onAchievementsClick: () -> Unit = {}, // TODO: Navigate to achievements screen
    onUserJourneyClick: () -> Unit = {} // TODO: Navigate to user journey screen (promocodes & comments history)
) {
    navigation<ProfileBaseRoute>(startDestination = ProfileRoute) {
        composable<ProfileRoute> {
            val scrollState = rememberScrollState()

            ProfileScreen(
                scrollState = scrollState,
                onBackClick = onBackClick,
                onSignOut = onSignOut,
                onAchievementsClick = onAchievementsClick,
                onUserJourneyClick = onUserJourneyClick,
            )
        }
    }
}
