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

            val topLevelDestination = TopLevelDestination.entries.firstOrNull { destination ->
                // Check if current destination matches the route
                currentDestination?.hasRoute(route = destination.route) == true ||
                    // For nested navigation, check if we're in the parent graph
                    currentEntry.value?.destination?.parent?.hasRoute(route = destination.route) == true
            }

            // Always update the last known destination when we're on a valid top-level destination
            if (topLevelDestination != null) {
                lastTopLevelDestination.value = topLevelDestination
            }

            return topLevelDestination
        }

    /**
     * Get the destination that should be highlighted in bottom navigation
     * When on nested screens, this shows which tab the user came from
     */
    val selectedTabDestination: TopLevelDestination?
        @Composable get() {
            val actualDestination = currentTopLevelDestination
            return actualDestination
                ?: ( // We're on a nested screen, show the last known destination
                    lastTopLevelDestination.value ?: HOME
                    )
        }

    /**
     * Check if current destination is a nested screen (not a top-level destination)
     */
    val isNestedScreen: Boolean
        @Composable get() {
            val topLevelDestination = TopLevelDestination.entries.firstOrNull { destination ->
                currentDestination?.hasRoute(route = destination.route) == true ||
                    currentDestination?.parent?.hasRoute(route = destination.route) == true
            }
            return topLevelDestination == null
        }

    val topLevelDestinations: List<TopLevelDestination> = TopLevelDestination.entries

    /**
     * Check if current destination is the profile screen
     */
    val isProfileScreen: Boolean
        @Composable get() {
            return currentDestination?.hasRoute<ProfileRoute>() == true
        }

    fun navigateToTopLevelDestination(topLevelDestination: TopLevelDestination) {
        trace("Navigation: ${topLevelDestination.name}") {
            val topLevelNavOptions = navOptions {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                // Remove restoreState to always go to base route
                restoreState = false
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
