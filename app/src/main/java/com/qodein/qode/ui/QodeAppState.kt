package com.qodein.qode.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.util.trace
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.qodein.feature.auth.navigation.navigateToAuth
import com.qodein.qode.navigation.TopLevelDestination
import com.qodein.qode.navigation.TopLevelDestination.CATALOG
import com.qodein.qode.navigation.TopLevelDestination.HISTORY
import com.qodein.qode.navigation.TopLevelDestination.HOME
import com.qodein.qode.navigation.TopLevelDestination.MORE

@Composable
fun rememberQodeAppState(navController: NavHostController = rememberNavController()): QodeAppState =
    remember(navController) {
        QodeAppState(
            navController,
        )
    }

@Stable
class QodeAppState(val navController: NavHostController) {
    private val previewsDestination = mutableStateOf<NavDestination?>(null)

    val currentDestination: NavDestination?
        @Composable get() {
            val currentEntry = navController.currentBackStackEntryFlow
                .collectAsState(initial = null)

            return currentEntry.value?.destination.also { destination ->
                if (destination != null) {
                    previewsDestination.value = destination
                }
            } ?: previewsDestination.value
        }

    val currentTopLevelDestination: TopLevelDestination?
        @Composable get() {
            return TopLevelDestination.entries.firstOrNull { topLevelDestination ->
                currentDestination?.hasRoute(route = topLevelDestination.route) == true
            }
        }

    val topLevelDestinations: List<TopLevelDestination> = TopLevelDestination.entries

    fun navigateToTopLevelDestination(topLevelDestination: TopLevelDestination) {
        trace("Navigation: ${topLevelDestination.name}") {
            val topLevelNavOptions = navOptions {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }

            when (topLevelDestination) {
                HOME -> navController.navigate(
                    route = com.qodein.feature.home.navigation.HomeBaseRoute,
                    navOptions = topLevelNavOptions,
                )
                CATALOG -> navController.navigate(
                    route = com.qodein.qode.navigation.CatalogBaseRoute,
                    navOptions = topLevelNavOptions,
                )
                HISTORY -> navController.navigate(
                    route = com.qodein.qode.navigation.HistoryBaseRoute,
                    navOptions = topLevelNavOptions,
                )
                MORE -> navController.navigateToAuth(navOptions = topLevelNavOptions)
            }
        }
    }
}
