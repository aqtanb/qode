package com.qodein.qode.ui.container

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.res.stringResource
import com.qodein.core.designsystem.component.AutoHideDirection
import com.qodein.core.designsystem.component.QodeinFab
import com.qodein.core.designsystem.icon.ActionIcons
import com.qodein.qode.R
import com.qodein.qode.navigation.NavigationActions
import com.qodein.qode.navigation.TopLevelDestination
import com.qodein.qode.ui.QodeAppState
import com.qodein.qode.ui.state.AppUiEvents

/**
 * Container responsible for floating action button with auto-hiding behavior.
 *
 * Handles:
 * - Context-aware FAB actions per screen (Create promocode on home, Add to feed, etc.)
 * - Auto-hiding behavior synchronized with bottom navigation
 * - Smart visibility logic (only shown on top-level destinations)
 * - Smooth animations and transitions
 *
 * Benefits:
 * - Context-aware actions that make sense for each screen
 * - Consistent auto-hiding behavior across the app
 * - Clean separation from main app logic
 * - Enterprise-grade performance optimizations
 */
@Composable
fun AppFabContainer(
    appState: QodeAppState,
    onEvent: (AppUiEvents) -> Unit
) {
    val currentScrollableState by appState.currentScrollableState

    // Update auto-hiding context (shared with bottom bar)
    appState.UpdateAutoHidingContext()

    // Get centralized FAB auto-hiding state
    val fabAutoHidingState by appState.fabAutoHidingState

    // Share the same scroll observation setup with AppBottomBarContainer
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

    // Only show FAB for top-level destinations (Home and Feed)
    val currentTopLevelDestination = appState.currentTopLevelDestination
    if (currentTopLevelDestination != null && currentTopLevelDestination != TopLevelDestination.FEED) {
        QodeinFab(
            onClick = {
                handleFabClick(
                    destination = currentTopLevelDestination,
                    onEvent = onEvent,
                )
            },
            icon = getFabIcon(currentTopLevelDestination),
            contentDescription = getFabContentDescription(currentTopLevelDestination),
            autoHide = fabAutoHidingState != null,
            autoHideState = fabAutoHidingState,
            autoHideDirection = AutoHideDirection.UP,
        )
    }
}

/**
 * Get context-aware FAB icon based on current destination
 */
private fun getFabIcon(destination: TopLevelDestination) =
    when (destination) {
        TopLevelDestination.HOME -> ActionIcons.Add // Create new promocode
        TopLevelDestination.FEED -> ActionIcons.Edit // Add content to feed
    }

/**
 * Get context-aware FAB content description based on current destination
 */
@Composable
private fun getFabContentDescription(destination: TopLevelDestination): String =
    when (destination) {
        TopLevelDestination.HOME -> stringResource(R.string.create_promo_code)
        TopLevelDestination.FEED -> stringResource(R.string.add_to_feed)
    }

/**
 * Handle context-aware FAB clicks for different destinations
 */
private fun handleFabClick(
    destination: TopLevelDestination,
    onEvent: (AppUiEvents) -> Unit
) {
    when (destination) {
        TopLevelDestination.HOME -> {
            // Navigate to promocode creation flow
            onEvent(AppUiEvents.Navigate(NavigationActions.NavigateToPromocodeSubmission))
        }
        TopLevelDestination.FEED -> {
            // Handle feed-specific action (could be add post, bookmark, etc.)
            onEvent(AppUiEvents.Navigate(NavigationActions.NavigateToPostSubmission))
        }
    }
}
