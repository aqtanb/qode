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
import com.qodein.feature.auth.navigation.AuthBaseRoute
import com.qodein.feature.home.navigation.HomeBaseRoute
import com.qodein.qode.navigation.CatalogRoute
import com.qodein.qode.navigation.HistoryRoute
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
            val currentEntry = navController.currentBackStackEntryFlow
                .collectAsState(initial = null)

            return TopLevelDestination.entries.firstOrNull { topLevelDestination ->
                // Check if current destination matches the route
                currentDestination?.hasRoute(route = topLevelDestination.route) == true ||
                    // For nested navigation, check if we're in the parent graph
                    currentEntry.value?.destination?.parent?.hasRoute(route = topLevelDestination.route) == true
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
                    route = HomeBaseRoute,
                    navOptions = topLevelNavOptions,
                )
                CATALOG -> navController.navigate(
                    route = CatalogRoute,
                    navOptions = topLevelNavOptions,
                )
                HISTORY -> navController.navigate(
                    route = HistoryRoute,
                    navOptions = topLevelNavOptions,
                )
                MORE -> navController.navigate(
                    route = AuthBaseRoute,
                    navOptions = topLevelNavOptions,
                )
            }
        }
    }
}
