package com.qodein.qode.ui.container

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.res.stringResource
import com.qodein.core.designsystem.component.BottomNavigation
import com.qodein.core.designsystem.component.NavigationAction
import com.qodein.core.designsystem.component.TabItem
import com.qodein.qode.R
import com.qodein.qode.navigation.NavigationActions
import com.qodein.qode.navigation.TopLevelDestination
import com.qodein.qode.ui.QodeAppState
import com.qodein.qode.ui.state.AppUiEvents
import kotlinx.coroutines.CoroutineScope

/**
 * Container responsible for floating bottom navigation bar.
 *
 * Handles:
 * - Modern floating bottom navigation rendering with 2 tabs (Home and Feed)
 * - Tab selection and navigation
 * - Autohiding behavior based on real scroll state from screens
 * - Visibility logic (hidden for certain screens like submission)
 *
 * Benefits:
 * - Modern floating design with enhanced animations
 * - Simplified 2-tab navigation (Home and Feed only)
 * - Smart autohiding based on actual scroll behavior from current screen
 * - Clean separation from main app logic
 */
@Composable
fun AppBottomBarContainer(
    appState: QodeAppState,
    onEvent: (AppUiEvents) -> Unit
) {
    val selectedTabDestination = appState.selectedTabDestination
    val currentScrollableState by appState.currentScrollableState
    val coroutineScope = rememberCoroutineScope()

    // Update auto-hiding context based on current screen
    appState.UpdateAutoHidingContext()

    // Get centralized auto-hiding state
    val autoHidingState by appState.bottomBarAutoHidingState

    // Set up scroll observation to feed into centralized manager
    LaunchedEffect(currentScrollableState) {
        val extractor = appState.getScrollExtractor()
        val scrollState = currentScrollableState

        if (extractor != null && scrollState != null) {
            snapshotFlow {
                extractor(scrollState)
            }.collect { scrollInfo ->
                appState.updateScrollInfo(scrollInfo)
            }
        }
    }

    // Only show bottom navigation for top-level destinations (Home and Feed)
    val currentTopLevelDestination = appState.currentTopLevelDestination
    if (currentTopLevelDestination != null) {
        // Always show bottom navigation - no auto-hiding for better UX
        BottomNavigation(
            items = appState.topLevelDestinations.map { destination ->
                TabItem(
                    route = destination.route.simpleName ?: "",
                    label = stringResource(destination.iconTextId),
                    selectedIcon = destination.selectedIcon,
                    unselectedIcon = destination.unSelectedIcon,
                    contentDescription = stringResource(R.string.navigate_to, stringResource(destination.iconTextId)),
                )
            },
            selectedRoute = selectedTabDestination?.route?.simpleName ?: "",
            onNavigationAction = { action ->
                handleNavigationAction(
                    action = action,
                    destinations = appState.topLevelDestinations,
                    appState = appState,
                    onEvent = onEvent,
                    coroutineScope = coroutineScope,
                )
            },
            showLabels = false,
        )
    }
}

/**
 * Handle navigation actions for Home and Feed tabs with scroll-to-top support
 */
private fun handleNavigationAction(
    action: NavigationAction,
    destinations: List<TopLevelDestination>,
    appState: QodeAppState,
    onEvent: (AppUiEvents) -> Unit,
    coroutineScope: CoroutineScope
) {
    when (action) {
        is NavigationAction.Navigate -> {
            // Handle navigation to different tab
            destinations.find { it.route.simpleName == action.tabItem.route }?.let { destination ->
                onEvent(AppUiEvents.Navigate(NavigationActions.NavigateToTab(destination)))
            }
        }
        is NavigationAction.ScrollToTop -> {
            // Handle scroll to top for current tab
            appState.scrollToTop(coroutineScope)
        }
    }
}
