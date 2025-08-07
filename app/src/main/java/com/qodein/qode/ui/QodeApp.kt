package com.qodein.qode.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.designsystem.component.QodeBottomNavigation
import com.qodein.core.designsystem.component.QodeButtonSize
import com.qodein.core.designsystem.component.QodeButtonVariant
import com.qodein.core.designsystem.component.QodeIconButton
import com.qodein.core.designsystem.component.QodeNavigationItem
import com.qodein.core.designsystem.component.QodeScreenTopAppBar
import com.qodein.core.designsystem.component.QodeTopAppBar
import com.qodein.core.designsystem.component.QodeTopAppBarVariant
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.theme.SpacingTokens
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
        onTopBarActionClick = { /* Handle top bar action */ },
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
        topBar = {
            if (appState.isNestedScreen) {
                // All nested screens get transparent top bar with adaptive colors
                QodeTopAppBar(
                    title = null,
                    variant = QodeTopAppBarVariant.CenterAligned,
                    navigationIcon = QodeActionIcons.Back,
                    onNavigationClick = {
                        appState.navController.popBackStack()
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        navigationIconContentColor = if (isProfileScreen) {
                            // Profile screen uses primaryContainer colors for better visibility
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            // Other nested screens use standard surface colors
                            MaterialTheme.colorScheme.onSurface
                        },
                    ),
                )
            } else {
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
        },
        modifier = modifier,
    ) { innerPadding ->
        QodeNavHost(
            appState = appState,
            modifier = Modifier.padding(innerPadding),
        )
    }
}
