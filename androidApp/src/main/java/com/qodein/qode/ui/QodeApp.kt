package com.qodein.qode.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.feature.promocode.navigation.navigateToPromocodeSubmission
import com.qodein.qode.navigation.NavigationHandler
import com.qodein.qode.navigation.QodeNavHost
import com.qodein.qode.navigation.TopLevelDestination
import com.qodein.qode.ui.container.AppBottomBarContainer
import com.qodein.qode.ui.container.AppDialogsContainer
import com.qodein.qode.ui.container.AppFabContainer
import com.qodein.qode.ui.container.AppThemeContainer
import com.qodein.qode.ui.container.AppTopBarContainer
import com.qodein.qode.ui.container.rememberDialogState
import com.qodein.qode.ui.state.AppUiEvents
import com.qodein.qode.ui.state.TopBarConfig
import com.qodein.qode.ui.state.getTopBarConfig

/**
 * QodeApp - Clean orchestrator using container pattern.
 *
 * Benefits of refactored approach:
 * - Simplified main composable (~80 lines vs 273 lines)
 * - Clear separation of concerns via containers
 * - Hybrid top bar system with smart defaults
 * - Centralized theme and dialog management
 * - Easy to test and maintain individual containers
 * - Follows NIA patterns with modern Android practices
 */
@Composable
fun QodeApp(
    appState: QodeAppState,
    modifier: Modifier = Modifier
) {
    QodeApp(
        appState = appState,
        onTopBarActionClick = {
            appState.navController.navigateToPromocodeSubmission()
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun QodeApp(
    appState: QodeAppState,
    onTopBarActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // ViewModels and state
    val appViewModel: QodeAppViewModel = hiltViewModel()
    val authState by appViewModel.authState.collectAsStateWithLifecycle()
    val languageState by appViewModel.languageState.collectAsStateWithLifecycle()

    // Dialog state management
    val dialogState = rememberDialogState()

    // Navigation handler (reused instance)
    val navigationHandler = NavigationHandler()

    // Handle navigation events
    LaunchedEffect(Unit) {
        appViewModel.navigationEvents.collect { action ->
            navigationHandler.handleNavigation(
                action = action,
                navController = appState.navController,
                authState = authState,
                navigateToTopLevel = { destination ->
                    appState.navigateToTopLevelDestination(destination)
                },
            )
        }
    }

    // Handle UI events (dialogs, etc.)
    LaunchedEffect(Unit) {
        appViewModel.uiEvents.collect { event ->
            dialogState.handleUiEvent(event)
        }
    }

    // Event handler for containers
    val onEvent: (AppUiEvents) -> Unit = { event ->
        appViewModel.handleUiEvent(event)
    }

    // Theme container wraps everything for status bar management
    AppThemeContainer(appViewModel) { statusBarOverlayColor, isDarkTheme ->

        Box(modifier = modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    AppTopBarContainer(
                        config = appState.getTopBarConfig(),
                        appState = appState,
                        authState = authState,
                        onEvent = onEvent,
                    )
                },

                floatingActionButton = {
                    // Centralized FAB with auto-hiding for all top-level destinations
                    AppFabContainer(
                        appState = appState,
                        onEvent = onEvent,
                    )
                },

                bottomBar = {
                    AppBottomBarContainer(
                        appState = appState,
                        onEvent = onEvent,
                    )
                },

            ) { innerPadding ->
                // Get current screen state for smart padding
                val currentDestination = appState.currentTopLevelDestination
                val isHomeDestination = currentDestination == TopLevelDestination.HOME
                val isProfileScreen = appState.isProfileScreen
                val isAuthScreen = appState.isAuthScreen

                // Navigation host with smart padding based on screen type
                QodeNavHost(
                    appState = appState,
                    userLanguage = languageState,
                    isDarkTheme = isDarkTheme,
                    modifier = when {
                        // Home screens handle their own UI - no padding for translucent effect
                        isHomeDestination -> Modifier.fillMaxSize()
                        // Profile and auth screens with transparent top bar - no top padding
                        isProfileScreen || isAuthScreen -> Modifier.padding(
                            start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                            end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                            bottom = innerPadding.calculateBottomPadding(),
                            // No top padding - let content flow behind transparent top bar
                        )
                        // Screens that manage their own scaffolds (TopBarConfig.None) - no padding
                        appState.getTopBarConfig() is TopBarConfig.None -> Modifier.fillMaxSize()
                        // All other screens with solid top bars - full padding
                        else -> Modifier.padding(innerPadding)
                    },
                )

                // Status bar overlay for text visibility
                val density = LocalDensity.current
                val statusBarHeight = with(density) {
                    WindowInsets.statusBars.getTop(density).toDp()
                }

                Box(
                    modifier = modifier
                        .fillMaxWidth()
                        .height(statusBarHeight)
                        .background(statusBarOverlayColor),
                )
            }
        }

        // App-level dialogs
        AppDialogsContainer(dialogState = dialogState)
    }
}
