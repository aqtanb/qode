package com.qodein.qode.ui

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
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
import com.qodein.core.ui.scroll.ScrollStateRegistry
import com.qodein.feature.auth.navigation.AuthRoute
import com.qodein.feature.feed.navigation.FeedRoute
import com.qodein.feature.home.navigation.HomeBaseRoute
import com.qodein.feature.profile.navigation.ProfileRoute
import com.qodein.feature.promocode.navigation.PromocodeDetailRoute
import com.qodein.feature.promocode.navigation.SubmissionRoute
import com.qodein.feature.settings.navigation.SettingsRoute
import com.qodein.qode.navigation.TopLevelDestination
import com.qodein.qode.navigation.TopLevelDestination.FEED
import com.qodein.qode.navigation.TopLevelDestination.HOME

@Composable
fun rememberQodeAppState(navController: NavHostController = rememberNavController()): QodeAppState =
    remember(navController) {
        QodeAppState(navController = navController)
    }

@Stable
class QodeAppState(val navController: NavHostController) : ScrollStateRegistry {
    private val previewsDestination = mutableStateOf<NavDestination?>(null)
    private val lastTopLevelDestination = mutableStateOf<TopLevelDestination?>(null)

    // Dynamic scroll state for auto-hiding behavior
    private val _currentScrollableState = mutableStateOf<ScrollableState?>(null)
    val currentScrollableState: State<ScrollableState?> get() = _currentScrollableState

    // ScrollStateRegistry implementation
    override fun registerScrollState(scrollableState: ScrollableState?) {
        _currentScrollableState.value = scrollableState
    }

    override fun unregisterScrollState() {
        // Don't set to null during tab switches to prevent auto-hiding state destruction
        // The new screen registration will override this value anyway
        // Only set to null if we're truly going to a screen without scrollable content
    }

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
            // Use currentTopLevelDestination for consistency - if we have a top level destination, we're not nested
            return currentTopLevelDestination == null
        }

    val topLevelDestinations: List<TopLevelDestination> = TopLevelDestination.entries

    /**
     * Check if current destination is the profile screen
     */
    val isProfileScreen: Boolean
        @Composable get() {
            return currentDestination?.hasRoute<ProfileRoute>() == true
        }

    /**
     * Check if current destination is the submission screen
     */
    val isSubmissionScreen: Boolean
        @Composable get() {
            return currentDestination?.hasRoute<SubmissionRoute>() == true
        }

    /**
     * Check if current destination is the settings screen
     */
    val isSettingsScreen: Boolean
        @Composable get() {
            return currentDestination?.hasRoute<SettingsRoute>() == true
        }

    /**
     * Check if current destination is the promocode detail screen
     */
    val isPromocodeDetailScreen: Boolean
        @Composable get() {
            return currentDestination?.hasRoute<PromocodeDetailRoute>() == true
        }

    val isAuthScreen: Boolean
        @Composable get() {
            return currentDestination?.hasRoute<AuthRoute>() == true
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
                FEED -> navController.navigate(
                    route = FeedRoute,
                    navOptions = topLevelNavOptions,
                )
            }
        }
    }
}
