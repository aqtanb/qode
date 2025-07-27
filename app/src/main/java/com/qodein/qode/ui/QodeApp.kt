package com.qodein.qode.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.component.QodeBottomNavigation
import com.qodein.core.designsystem.component.QodeButtonSize
import com.qodein.core.designsystem.component.QodeButtonVariant
import com.qodein.core.designsystem.component.QodeIconButton
import com.qodein.core.designsystem.component.QodeNavigationItem
import com.qodein.core.designsystem.component.QodeSearchTopAppBar
import com.qodein.core.designsystem.theme.SpacingTokens
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

@Composable
internal fun QodeApp(
    appState: QodeAppState,
    onTopBarActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentDestination = appState.currentTopLevelDestination
    val isHomeDestination = currentDestination == TopLevelDestination.HOME

    Scaffold(
        topBar = {
            if (isHomeDestination) {
                var searchQuery by remember { mutableStateOf("") }

                QodeSearchTopAppBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onSearchClose = { searchQuery = "" },
                    placeholder = "Search promo codes...",
                    modifier = Modifier.padding(8.dp),
                )
            }
        },

        floatingActionButton = {
            if (isHomeDestination) {
                QodeIconButton(
                    onClick = onTopBarActionClick,
                    icon = Icons.Default.Add,
                    contentDescription = "Add",
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
                    selectedRoute = currentDestination?.route?.simpleName ?: "",
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
