package com.qodein.qode.ui.container

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.qodein.core.designsystem.component.QodeBottomNavigation
import com.qodein.core.designsystem.component.QodeNavigationItem
import com.qodein.qode.navigation.NavigationActions
import com.qodein.qode.navigation.TopLevelDestination
import com.qodein.qode.ui.QodeAppState
import com.qodein.qode.ui.state.AppUiEvents

/**
 * Container responsible for bottom navigation bar.
 *
 * Handles:
 * - Bottom navigation rendering
 * - Tab selection and navigation
 * - Coming soon dialog triggers for unimplemented features
 * - Visibility logic (hidden for certain screens like submission)
 *
 * Benefits:
 * - Isolates bottom navigation logic
 * - Centralized coming soon handling
 * - Easy to extend with new tabs
 * - Clean separation from main app logic
 */
@Composable
fun AppBottomBarContainer(
    appState: QodeAppState,
    onEvent: (AppUiEvents) -> Unit
) {
    val selectedTabDestination = appState.selectedTabDestination
    val isSubmissionScreen = appState.isSubmissionScreen

    // Hide bottom navigation for fullscreen flows like submission wizard
    if (!isSubmissionScreen) {
        Column {
            QodeBottomNavigation(
                items = appState.topLevelDestinations.map { destination ->
                    QodeNavigationItem(
                        route = destination.route.simpleName ?: "",
                        label = stringResource(destination.iconTextId),
                        selectedIcon = destination.selectedIcon,
                        unselectedIcon = destination.unSelectedIcon,
                    )
                },
                selectedRoute = selectedTabDestination?.route?.simpleName ?: "",
                onItemClick = { selectedItem ->
                    handleBottomNavClick(
                        selectedItem = selectedItem,
                        destinations = appState.topLevelDestinations,
                        onEvent = onEvent,
                    )
                },
            )
        }
    }
}

/**
 * Handle bottom navigation item clicks with coming soon logic
 */
private fun handleBottomNavClick(
    selectedItem: QodeNavigationItem,
    destinations: List<TopLevelDestination>,
    onEvent: (AppUiEvents) -> Unit
) {
    destinations.find { it.route.simpleName == selectedItem.route }?.let { destination ->
        when (destination) {
            TopLevelDestination.INBOX -> {
                // Show coming soon for inbox
                onEvent(AppUiEvents.ShowInboxComingSoon)
            }
            else -> {
                // Navigate to the destination
                onEvent(AppUiEvents.Navigate(NavigationActions.NavigateToTab(destination)))
            }
        }
    }
}

/**
 * Extension function to determine if bottom bar should be visible
 */
@Composable
fun QodeAppState.shouldShowBottomBar(): Boolean = !isSubmissionScreen // Hide for submission wizard
