package com.qodein.qode.ui

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.qode.navigation.NavigationHandler
import com.qodein.qode.navigation.QodeNavHost
import com.qodein.qode.navigation.TopLevelDestination
import com.qodein.qode.ui.container.AppBottomBarContainer
import com.qodein.qode.ui.container.AppFabContainer
import com.qodein.qode.ui.state.AppUiEvents
import com.qodein.shared.domain.AuthState
import com.qodein.shared.model.Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun QodeApp(
    appState: QodeAppState,
    modifier: Modifier = Modifier
) {
    val appViewModel: QodeAppViewModel = hiltViewModel()
    val authState by appViewModel.authState.collectAsStateWithLifecycle()
    val languageState by appViewModel.languageState.collectAsStateWithLifecycle()

    val user = (authState as? AuthState.Authenticated)?.user

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

    val onEvent: (AppUiEvents) -> Unit = { event ->
        appViewModel.handleUiEvent(event)
    }

    val view = LocalView.current
    val context = LocalContext.current
    val isDarkTheme = when (appViewModel.themeState.collectAsStateWithLifecycle().value) {
        Theme.LIGHT -> false
        Theme.DARK -> true
        Theme.SYSTEM -> isSystemInDarkTheme()
    }
    SideEffect {
        val window = (context as? Activity)?.window ?: return@SideEffect
        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = !isDarkTheme
    }

    Box(modifier = modifier.fillMaxSize()) {
        val shouldUseScaffold = appState.currentTopLevelDestination == TopLevelDestination.HOME ||
            appState.currentTopLevelDestination == TopLevelDestination.FEED

        if (shouldUseScaffold) {
            Scaffold(
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
                QodeNavHost(
                    appState = appState,
                    userLanguage = languageState,
                    user = user,
                    isDarkTheme = isDarkTheme,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        } else {
            QodeNavHost(
                appState = appState,
                userLanguage = languageState,
                user = user,
                isDarkTheme = isDarkTheme,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
