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

@Composable
fun AppBottomBarContainer(
    appState: QodeAppState,
    onEvent: (AppUiEvents) -> Unit
) {
    val selectedTabDestination = appState.selectedTabDestination
    val currentScrollableState by appState.currentScrollableState
    val coroutineScope = rememberCoroutineScope()

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

    val currentTopLevelDestination = appState.currentTopLevelDestination
    if (currentTopLevelDestination != null) {
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

private fun handleNavigationAction(
    action: NavigationAction,
    destinations: List<TopLevelDestination>,
    appState: QodeAppState,
    onEvent: (AppUiEvents) -> Unit,
    coroutineScope: CoroutineScope
) {
    when (action) {
        is NavigationAction.Navigate -> {
            destinations.find { it.route.simpleName == action.tabItem.route }?.let { destination ->
                onEvent(AppUiEvents.Navigate(NavigationActions.NavigateToTab(destination)))
            }
        }
        is NavigationAction.ScrollToTop -> {
            appState.scrollToTop(coroutineScope)
        }
    }
}
