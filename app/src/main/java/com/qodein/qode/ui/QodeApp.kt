package com.qodein.qode.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.core.designsystem.component.QodeBottomNavigation
import com.qodein.core.designsystem.component.QodeButtonSize
import com.qodein.core.designsystem.component.QodeButtonVariant
import com.qodein.core.designsystem.component.QodeComingSoonDialog
import com.qodein.core.designsystem.component.QodeIconButton
import com.qodein.core.designsystem.component.QodeNavigationItem
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.core.domain.AuthState
import com.qodein.core.ui.component.QodeAppTopAppBar
import com.qodein.core.ui.component.TopAppBarScreenType
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
    val uriHandler = LocalUriHandler.current
    var showComingSoon by remember { mutableStateOf(false) }

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
            val screenType = when {
                isSubmissionScreen -> TopAppBarScreenType.Nested
                isProfileScreen -> TopAppBarScreenType.ScrollAware
                appState.isNestedScreen -> TopAppBarScreenType.Nested
                !appState.isNestedScreen && !isHomeDestination -> TopAppBarScreenType.Main
                else -> TopAppBarScreenType.None
            }

            if (screenType != TopAppBarScreenType.None) {
                val title = when {
                    screenType == TopAppBarScreenType.Nested && isSubmissionScreen -> stringResource(R.string.add)
                    screenType == TopAppBarScreenType.Nested -> null
                    screenType == TopAppBarScreenType.ScrollAware -> "" // ProfileScreen has no title
                    currentDestination != null -> stringResource(currentDestination.titleTextId)
                    else -> ""
                }

                QodeAppTopAppBar(
                    title = title ?: "",
                    screenType = screenType,
                    user = (authState as? AuthState.Authenticated)?.user,
                    navigationIcon = when (screenType) {
                        TopAppBarScreenType.Nested -> QodeActionIcons.Back
                        TopAppBarScreenType.ScrollAware -> QodeActionIcons.Back
                        TopAppBarScreenType.Main -> QodeNavigationIcons.Favorites
                        else -> null
                    },
                    onNavigationClick = when (screenType) {
                        TopAppBarScreenType.Nested -> ({ appState.navController.popBackStack() })
                        TopAppBarScreenType.ScrollAware -> ({ appState.navController.popBackStack() })
                        TopAppBarScreenType.Main -> ({ appViewModel.handleNavigation(NavigationActions.NavigateToFavorites) })
                        else -> null
                    },
                    onProfileClick = { _ -> onProfileClick() },
                    onSettingsClick = {
                        appViewModel.handleNavigation(NavigationActions.NavigateToSettings)
                    },
                    showProfile = screenType != TopAppBarScreenType.ScrollAware, // Don't show profile on profile screen
                    showSettings = screenType != TopAppBarScreenType.ScrollAware, // Don't show settings on profile screen
                    scrollState = if (screenType == TopAppBarScreenType.ScrollAware) appState.profileScrollState else null,
                )
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
                                // Intercept inbox navigation and show Coming Soon dialog
                                if (destination == TopLevelDestination.INBOX) {
                                    showComingSoon = true
                                } else {
                                    appState.navigateToTopLevelDestination(destination)
                                }
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
                // Home screens handle their own UI - no padding
                isHomeDestination -> Modifier.fillMaxSize()
                // Profile screen with transparent top bar - no top padding
                isProfileScreen -> Modifier.padding(
                    start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                    end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                    bottom = innerPadding.calculateBottomPadding(),
                    // No top padding - let content flow behind transparent top bar
                )
                // All other screens with solid top bars - full padding
                else -> Modifier.padding(innerPadding)
            },
        )
    }

    // Show Coming Soon dialog when user tries to access inbox
    if (showComingSoon) {
        QodeComingSoonDialog(
            onDismiss = { showComingSoon = false },
            onTelegramClick = {
                uriHandler.openUri("https://www.t.me/qodeinhq")
            },
        )
    }
}
