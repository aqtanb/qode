package com.qodein.feature.settings.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.qodein.feature.settings.SettingsScreen
import kotlinx.serialization.Serializable

@Serializable object SettingsRoute // screen

@Serializable object SettingsBaseRoute // route to base navigation graph

fun NavController.navigateToSettings(navOptions: NavOptions? = null) = navigate(route = SettingsRoute, navOptions)

fun NavGraphBuilder.settingsSection(onBackClick: () -> Unit = {}) {
    navigation<SettingsBaseRoute>(startDestination = SettingsRoute) {
        composable<SettingsRoute> {
            SettingsScreen(
                onBackClick = onBackClick,
            )
        }
    }
}
