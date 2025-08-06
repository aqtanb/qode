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
import com.qodein.feature.home.navigation.HomeBaseRoute
import com.qodein.feature.inbox.navigation.InboxRoute
import com.qodein.feature.profile.navigation.ProfileRoute
import com.qodein.feature.search.navigation.SearchRoute
import com.qodein.qode.navigation.TopLevelDestination
import com.qodein.qode.navigation.TopLevelDestination.HOME
import com.qodein.qode.navigation.TopLevelDestination.INBOX
import com.qodein.qode.navigation.TopLevelDestination.SEARCH

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
    private val lastTopLevelDestination = mutableStateOf<TopLevelDestination?>(null)

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

            val topLevelDestination = TopLevelDestination.entries.firstOrNull { topLevelDestination ->
                // Check if current destination matches the route
                currentDestination?.hasRoute(route = topLevelDestination.route) == true ||
                    // For nested navigation, check if we're in the parent graph
                    currentEntry.value?.destination?.parent?.hasRoute(route = topLevelDestination.route) == true
            }

            // If we're on a valid top-level destination, update the last known one
            if (topLevelDestination != null) {
                lastTopLevelDestination.value = topLevelDestination
            }

            // If we're on the profile screen, return the last known top-level destination
            val isOnProfile = currentDestination?.hasRoute(route = ProfileRoute::class) == true
            return if (isOnProfile) {
                lastTopLevelDestination.value ?: HOME // Default to HOME if no previous destination
            } else {
                topLevelDestination
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
                SEARCH -> navController.navigate(
                    route = SearchRoute,
                    navOptions = topLevelNavOptions,
                )
                INBOX -> navController.navigate(
                    route = InboxRoute,
                    navOptions = topLevelNavOptions,
                )
            }
        }
    }
}
