package com.qodein.qode.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
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
    val appViewModel: QodeAppViewModel = hiltViewModel()
    val authState by appViewModel.authState.collectAsStateWithLifecycle()
    val languageState by appViewModel.languageState.collectAsStateWithLifecycle()

    val dialogState = rememberDialogState()

    val navigationHandler = NavigationHandler()

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

    LaunchedEffect(Unit) {
        appViewModel.uiEvents.collect { event ->
            dialogState.handleUiEvent(event)
        }
    }

    val onEvent: (AppUiEvents) -> Unit = { event ->
        appViewModel.handleUiEvent(event)
    }

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
                val currentDestination = appState.currentTopLevelDestination
                val isHomeDestination = currentDestination == TopLevelDestination.HOME
                val isProfileScreen = appState.isProfileScreen
                val isAuthScreen = appState.isAuthScreen
                val isPostSubmissionScreen = appState.isPostSubmissionScreen

                QodeNavHost(
                    appState = appState,
                    userLanguage = languageState,
                    isDarkTheme = isDarkTheme,
                    modifier = when {
                        isHomeDestination || isProfileScreen || isAuthScreen || isPostSubmissionScreen -> Modifier.fillMaxSize()
                        else -> Modifier.padding(innerPadding)
                    },
                )

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

        AppDialogsContainer(dialogState = dialogState)
    }
}
