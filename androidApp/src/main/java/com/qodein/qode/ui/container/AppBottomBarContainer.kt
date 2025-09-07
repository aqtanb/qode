package com.qodein.qode.ui.container

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.res.stringResource
import com.qodein.core.designsystem.component.BottomNavigation
import com.qodein.core.designsystem.component.TabItem
import com.qodein.qode.navigation.NavigationActions
import com.qodein.qode.navigation.TopLevelDestination
import com.qodein.qode.ui.QodeAppState
import com.qodein.qode.ui.state.AppUiEvents

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
                    contentDescription = "Navigate to ${stringResource(destination.iconTextId)}",
                )
            },
            selectedRoute = selectedTabDestination?.route?.simpleName ?: "",
            onItemClick = { selectedItem ->
                handleTabItemClick(
                    selectedItem = selectedItem,
                    destinations = appState.topLevelDestinations,
                    onEvent = onEvent,
                )
            },
            showLabels = false,
        )
    }
}

/**
 * Handle floating bottom navigation item clicks for Home and Feed tabs
 */
private fun handleTabItemClick(
    selectedItem: TabItem,
    destinations: List<TopLevelDestination>,
    onEvent: (AppUiEvents) -> Unit
) {
    destinations.find { it.route.simpleName == selectedItem.route }?.let { destination ->
        // Direct navigation for both Home and Feed (no coming soon dialogs needed)
        onEvent(AppUiEvents.Navigate(NavigationActions.NavigateToTab(destination)))
    }
}
