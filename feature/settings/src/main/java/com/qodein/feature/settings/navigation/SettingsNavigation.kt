package com.qodein.feature.settings.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.qodein.feature.settings.AboutScreen
import com.qodein.feature.settings.LicensesScreen
import com.qodein.feature.settings.SettingsRoute
import kotlinx.serialization.Serializable

@Serializable object SettingsRoute

@Serializable object SettingsBaseRoute

@Serializable object LicensesRoute

@Serializable object AboutRoute

fun NavController.navigateToSettings(navOptions: NavOptions? = null) = navigate(route = SettingsRoute, navOptions)

fun NavController.navigateToLicenses(navOptions: NavOptions? = null) = navigate(route = LicensesRoute, navOptions)

fun NavController.navigateToAbout(navOptions: NavOptions? = null) = navigate(route = AboutRoute, navOptions)

fun NavGraphBuilder.settingsSection(
    onBackClick: () -> Unit,
    onNavigateToLicenses: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    navigation<SettingsBaseRoute>(startDestination = SettingsRoute) {
        composable<SettingsRoute> {
            SettingsRoute(
                onBackClick = onBackClick,
                onNavigateToLicenses = onNavigateToLicenses,
                onNavigateToAbout = onNavigateToAbout,
            )
        }

        composable<LicensesRoute> {
            LicensesScreen(
                onBackClick = onBackClick,
            )
        }

        composable<AboutRoute> {
            AboutScreen(
                onNavigateBack = onBackClick,
            )
        }
    }
}
