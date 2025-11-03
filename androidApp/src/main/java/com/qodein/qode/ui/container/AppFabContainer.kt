package com.qodein.qode.ui.container

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.qodein.core.designsystem.component.AutoHideDirection
import com.qodein.core.designsystem.component.AutoHidingContent
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.theme.ElevationTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.qode.R
import com.qodein.qode.navigation.NavigationActions
import com.qodein.qode.navigation.TopLevelDestination
import com.qodein.qode.ui.QodeAppState
import com.qodein.qode.ui.state.AppUiEvents

/**
 * Container responsible for floating action button with auto-hiding behavior.
 *
 * Handles:
 * - Context-aware FAB actions per screen (Create promo code on home, Add to feed, etc.)
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
    if (currentTopLevelDestination != null) {
        // Capture delegated property for smart casting
        val currentFabState = fabAutoHidingState
        if (currentFabState != null) {
            // Use AutoHidingContent for smooth animations
            AutoHidingContent(
                state = currentFabState,
                direction = AutoHideDirection.UP,
            ) {
                FloatingActionButton(
                    onClick = {
                        handleFabClick(
                            destination = currentTopLevelDestination,
                            onEvent = onEvent,
                        )
                    },
                    modifier = Modifier.size(SizeTokens.Fab.sizeSmall),
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = ElevationTokens.large,
                        pressedElevation = ElevationTokens.extraLarge,
                        focusedElevation = ElevationTokens.large,
                        hoveredElevation = ElevationTokens.extraLarge,
                    ),
                ) {
                    Icon(
                        imageVector = getFabIcon(currentTopLevelDestination),
                        contentDescription = getFabContentDescription(currentTopLevelDestination),
                        modifier = Modifier.size(SizeTokens.Fab.iconSize),
                    )
                }
            }
        } else {
            // No scrollable state - always show FAB
            FloatingActionButton(
                onClick = {
                    handleFabClick(
                        destination = currentTopLevelDestination,
                        onEvent = onEvent,
                    )
                },
                modifier = Modifier.size(SizeTokens.Fab.sizeSmall),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = ElevationTokens.large,
                    pressedElevation = ElevationTokens.extraLarge,
                    focusedElevation = ElevationTokens.large,
                    hoveredElevation = ElevationTokens.extraLarge,
                ),
            ) {
                Icon(
                    imageVector = getFabIcon(currentTopLevelDestination),
                    contentDescription = getFabContentDescription(currentTopLevelDestination),
                    modifier = Modifier.size(SizeTokens.Fab.iconSize),
                )
            }
        }
    }
}

/**
 * Get context-aware FAB icon based on current destination
 */
private fun getFabIcon(destination: TopLevelDestination) =
    when (destination) {
        TopLevelDestination.HOME -> QodeActionIcons.Add // Create new promo code
        TopLevelDestination.FEED -> QodeActionIcons.Edit // Add content to feed
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
            // Navigate to promo code creation flow
            onEvent(AppUiEvents.Navigate(NavigationActions.NavigateToPromocodeSubmission))
        }
        TopLevelDestination.FEED -> {
            // Handle feed-specific action (could be add post, bookmark, etc.)
            onEvent(AppUiEvents.Navigate(NavigationActions.NavigateToPostSubmission))
        }
    }
}
