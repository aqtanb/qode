package com.qodein.qode.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.util.trace
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.qodein.core.designsystem.component.AutoHideConfig
import com.qodein.core.designsystem.component.AutoHidingState
import com.qodein.core.designsystem.component.HidingBehavior
import com.qodein.core.designsystem.component.HidingSensitivity
import com.qodein.core.designsystem.component.LazyListScrollExtractor
import com.qodein.core.designsystem.component.ScrollInfo
import com.qodein.core.designsystem.component.ScrollStateExtractor
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

    // Centralized Auto-Hiding Manager
    private val autoHidingManager = AutoHidingManager()

    // Dynamic scroll state for auto-hiding behavior
    private val _currentScrollableState = mutableStateOf<ScrollableState?>(null)
    val currentScrollableState: State<ScrollableState?> get() = _currentScrollableState

    // ScrollStateRegistry implementation
    override fun registerScrollState(scrollableState: ScrollableState?) {
        _currentScrollableState.value = scrollableState
        autoHidingManager.updateScrollState(scrollableState)
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

    // MARK: - Auto-Hiding Public API

    /**
     * Get the centralized auto-hiding state for bottom navigation
     */
    val bottomBarAutoHidingState: State<AutoHidingState?> = autoHidingManager.bottomBarState

    /**
     * Get the centralized auto-hiding state for floating action button
     */
    val fabAutoHidingState: State<AutoHidingState?> = autoHidingManager.fabState

    /**
     * Update screen context for auto-hiding behavior
     */
    @Composable
    fun UpdateAutoHidingContext() {
        val currentScreenType = getScreenType()
        autoHidingManager.updateScreenType(currentScreenType)
    }

    /**
     * Update scroll info to the centralized auto-hiding manager
     */
    fun updateScrollInfo(scrollInfo: ScrollInfo) {
        autoHidingManager.updateScrollInfo(scrollInfo)
    }

    /**
     * Get the scroll extractor for current scroll state
     */
    fun getScrollExtractor(): ((ScrollableState) -> ScrollInfo)? = autoHidingManager.getScrollExtractor()

    /**
     * Determine current screen type for context-aware auto-hiding
     */
    @Composable
    private fun getScreenType(): ScreenType =
        when {
            currentTopLevelDestination == HOME -> ScreenType.HOME
            currentTopLevelDestination == FEED -> ScreenType.FEED
            isProfileScreen -> ScreenType.PROFILE
            isAuthScreen -> ScreenType.AUTH
            isSubmissionScreen -> ScreenType.SUBMISSION
            isSettingsScreen -> ScreenType.SETTINGS
            isPromocodeDetailScreen -> ScreenType.DETAIL
            else -> ScreenType.OTHER
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

// MARK: - AutoHidingManager

/**
 * Centralized manager for auto-hiding behavior (bottom navigation & FAB).
 * This is the single source of truth for auto-hiding states.
 * Screens handle their own top bars for better separation of concerns.
 */
@Stable
private class AutoHidingManager {
    private var currentScrollState by mutableStateOf<ScrollableState?>(null)
    private var currentScreenType by mutableStateOf(ScreenType.OTHER)

    // Cached extractors to prevent recreation
    private val lazyListExtractor = LazyListScrollExtractor()
    private val scrollStateExtractor = ScrollStateExtractor()

    // The manager owns the auto-hiding states
    private var _bottomBarState by mutableStateOf<AutoHidingState?>(null)
    private var _fabState by mutableStateOf<AutoHidingState?>(null)

    val bottomBarState: State<AutoHidingState?> = derivedStateOf { _bottomBarState }
    val fabState: State<AutoHidingState?> = derivedStateOf { _fabState }

    fun updateScrollState(scrollableState: ScrollableState?) {
        if (currentScrollState != scrollableState) {
            currentScrollState = scrollableState
            recreateState()
        }
    }

    fun updateScreenType(screenType: ScreenType) {
        if (currentScreenType != screenType) {
            currentScreenType = screenType
            recreateState()
        }
    }

    private fun recreateState() {
        val scrollState = currentScrollState
        val bottomBarConfig = getBottomBarConfig(currentScreenType)
        val fabConfig = getFabConfig(currentScreenType)

        // Create new states based on current scroll state and screen type
        _bottomBarState = createAutoHidingState(scrollState, bottomBarConfig)
        _fabState = createAutoHidingState(scrollState, fabConfig)
    }

    fun updateScrollInfo(scrollInfo: ScrollInfo) {
        // Update both states with scroll info
        _bottomBarState?.updateScroll(scrollInfo)
        _fabState?.updateScroll(scrollInfo)
    }

    fun getScrollExtractor(): ((ScrollableState) -> ScrollInfo)? =
        when (currentScrollState) {
            is LazyListState -> { state -> lazyListExtractor.extractScrollInfo(state as LazyListState) }
            is ScrollState -> { state -> scrollStateExtractor.extractScrollInfo(state as ScrollState) }
            else -> null
        }

    private fun createAutoHidingState(
        scrollableState: ScrollableState?,
        config: AutoHideConfig
    ): AutoHidingState? =
        when (scrollableState) {
            is LazyListState, is ScrollState, is LazyGridState -> AutoHidingState(config)
            else -> null
        }

    private fun getBottomBarConfig(screenType: ScreenType): AutoHideConfig =
        when (screenType) {
            ScreenType.HOME -> AutoHideConfig.Sensitive // More sensitive hiding on home
            ScreenType.FEED -> AutoHideConfig.Default // Balanced behavior on feed
            ScreenType.PROFILE -> AutoHideConfig.Relaxed // Gentle hiding on profile
            ScreenType.DETAIL -> AutoHideConfig(
                sensitivity = HidingSensitivity.LOW,
                behavior = HidingBehavior.VELOCITY_BASED,
            ) // Only hide on fast scrolls for detail screens
            else -> AutoHideConfig.Default
        }

    private fun getFabConfig(screenType: ScreenType): AutoHideConfig =
        // FAB should hide immediately like Telegram for better UX
        AutoHideConfig(behavior = HidingBehavior.IMMEDIATE)
}

// MARK: - Screen Types

/**
 * Context-aware screen types for auto-hiding behavior customization
 */
private enum class ScreenType {
    HOME,
    FEED,
    PROFILE,
    AUTH,
    SUBMISSION,
    SETTINGS,
    DETAIL,
    OTHER
}
