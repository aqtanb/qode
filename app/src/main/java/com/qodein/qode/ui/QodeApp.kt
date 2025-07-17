package com.qodein.qode.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.home.HomeScreen
import com.qodein.core.designsystem.component.QodeBottomNavigation
import com.qodein.core.designsystem.component.QodeButtonSize
import com.qodein.core.designsystem.component.QodeButtonVariant
import com.qodein.core.designsystem.component.QodeIconButton
import com.qodein.core.designsystem.component.QodeNavigationItem
import com.qodein.core.designsystem.component.QodeSearchTopAppBar
import com.qodein.core.designsystem.theme.QodeSpacing

@Composable
fun QodeApp(
    appState: QodeAppState,
    modifier: Modifier = Modifier
) {
}

@Composable
internal fun QodeApp(
    appState: QodeAppState,
    onTopBarActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            var searchQuery by remember { mutableStateOf("") }

            QodeSearchTopAppBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onSearchClose = {},
                placeholder = "Search promo codes...",
                modifier = Modifier.padding(8.dp),
            )
        },

        floatingActionButton = {
            QodeIconButton(
                onClick = { /* Handle FAB click */ },
                icon = Icons.Default.Add,
                contentDescription = "Add",
                variant = QodeButtonVariant.Primary,
                size = QodeButtonSize.Large,
                modifier = Modifier.padding(QodeSpacing.sm),
            )
        },

        bottomBar = {
            // place holding for now
            val items = listOf(
                QodeNavigationItem(route = "home", label = "Home", selectedIcon = Icons.Filled.Home, unselectedIcon = Icons.Outlined.Home),
                QodeNavigationItem(
                    route = "catalog",
                    label = "Catalog",
                    selectedIcon = Icons.Filled.Search,
                    unselectedIcon = Icons.Outlined.Search,
                ),
                QodeNavigationItem(
                    route = "history",
                    label = "history",
                    selectedIcon = Icons.Filled.Favorite,
                    unselectedIcon = Icons.Outlined.FavoriteBorder,
                ),
                QodeNavigationItem(
                    route = "more",
                    label = "More",
                    selectedIcon = Icons.Filled.Menu,
                    unselectedIcon = Icons.Outlined.Menu,
                ),
            )

            Column {
                QodeBottomNavigation(
                    items = items,
                    selectedRoute = "home",
                    onItemClick = {},
                )
            }
        },
    ) { innerPadding ->
        HomeScreen(
            modifier = Modifier
                .padding(innerPadding),
        )
    }
}
