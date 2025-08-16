package com.qodein.qode.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.designsystem.component.QodeBottomNavigation
import com.qodein.core.designsystem.component.QodeButtonSize
import com.qodein.core.designsystem.component.QodeButtonVariant
import com.qodein.core.designsystem.component.QodeIconButton
import com.qodein.core.designsystem.component.QodeNavigationItem
import com.qodein.core.designsystem.component.QodeScreenTopAppBar
import com.qodein.core.designsystem.component.QodeTransparentTopAppBar
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.feature.promocode.navigation.navigateToSubmission
import com.qodein.qode.R
import com.qodein.qode.navigation.NavigationActions
import com.qodein.qode.navigation.NavigationHandler
import com.qodein.qode.navigation.QodeNavHost
import com.qodein.qode.navigation.TopLevelDestination

@Composable
fun QodeApp(
    appState: QodeAppState,
    modifier: Modifier = Modifier
) {
    QodeApp(
        appState = appState,
        onTopBarActionClick = {
            appState.navController.navigateToSubmission()
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

    val currentDestination = appState.currentTopLevelDestination
    val selectedTabDestination = appState.selectedTabDestination
    val isHomeDestination = currentDestination == TopLevelDestination.HOME
    val isProfileScreen = appState.isProfileScreen
    val isSubmissionScreen = appState.isSubmissionScreen

    // Handle navigation events from ViewModel
    LaunchedEffect(Unit) {
        appViewModel.navigationEvents.collect { action ->
            NavigationHandler().handleNavigation(
                action = action,
                navController = appState.navController,
                authState = authState,
                navigateToTopLevel = { destination ->
                    appState.navigateToTopLevelDestination(destination)
                },
            )
        }
    }

    val onProfileClick = {
        val navigationAction = appViewModel.getProfileNavigationAction()
        appViewModel.handleNavigation(navigationAction)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            when {
                // Submission screens are fullscreen modal - no top bar
                isSubmissionScreen -> {
                    // No top bar for submission flow
                }
                // Other nested screens (not profile, not submission) get transparent top bar
                appState.isNestedScreen && !isProfileScreen -> {
                    QodeTransparentTopAppBar(
                        title = null,
                        navigationIcon = QodeActionIcons.Back,
                        onNavigationClick = {
                            appState.navController.popBackStack()
                        },
                        navigationIconTint = MaterialTheme.colorScheme.onSurface,
                        titleColor = MaterialTheme.colorScheme.onSurface,
                        actionIconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                // Top level screens (except home) get the main app bar
                !appState.isNestedScreen && !isHomeDestination -> {
                    currentDestination?.let { destination ->
                        QodeScreenTopAppBar(
                            title = stringResource(destination.titleTextId),
                            onFavoritesClick = {
                                appViewModel.handleNavigation(NavigationActions.NavigateToFavorites)
                            },
                            onProfileClick = onProfileClick,
                            onSettingsClick = {
                                appViewModel.handleNavigation(NavigationActions.NavigateToSettings)
                            },
                        )
                    }
                }
                // Home, Profile, and Submission screens handle their own UI - no app-level TopAppBar
            }
        },

        floatingActionButton = {
            if (isHomeDestination) {
                QodeIconButton(
                    onClick = onTopBarActionClick,
                    icon = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add),
                    variant = QodeButtonVariant.Primary,
                    size = QodeButtonSize.Large,
                    modifier = Modifier.padding(SpacingTokens.sm),
                )
            }
        },

        bottomBar = {
            // Hide bottom navigation for submission flow (fullscreen modal)
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
                            appState.topLevelDestinations.find {
                                it.route.simpleName == selectedItem.route
                            }?.let { destination ->
                                appState.navigateToTopLevelDestination(destination)
                            }
                        },
                    )
                }
            }
        },
    ) { innerPadding ->
        QodeNavHost(
            appState = appState,
            modifier = when {
                // Home, submission screens handle their own UI - no padding
                isHomeDestination || isSubmissionScreen -> Modifier.fillMaxSize()
                // Nested screens with transparent top bar - no top padding
                appState.isNestedScreen -> Modifier.padding(
                    start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                    end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                    bottom = innerPadding.calculateBottomPadding(),
                    // No top padding - let content flow behind transparent top bar
                )
                // Regular screens with solid top bars - full padding
                else -> Modifier.padding(innerPadding)
            },
        )
    }
}
