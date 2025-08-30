package com.qodein.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.designsystem.theme.ElevationTokens
import com.qodein.core.designsystem.theme.MotionTokens
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens

/**
 * Navigation item data class
 */
data class QodeNavigationItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badge: String? = null,
    val enabled: Boolean = true
)

/**
 * Production-ready bottom navigation component for Qode design system
 *
 * @param items List of navigation items
 * @param selectedRoute Currently selected route
 * @param onItemClick Called when an item is clicked
 * @param modifier Modifier to be applied to the navigation bar
 * @param showLabels Whether to show labels for items
 * @param containerColor Background color of the navigation bar
 */
@Composable
fun QodeBottomNavigation(
    items: List<QodeNavigationItem>,
    selectedRoute: String,
    onItemClick: (QodeNavigationItem) -> Unit,
    modifier: Modifier = Modifier,
    showLabels: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.surface
) {
    NavigationBar(
        modifier = modifier,
        containerColor = containerColor,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        tonalElevation = ElevationTokens.small,
    ) {
        items.forEach { item ->
            QodeNavigationBarItem(
                item = item,
                selected = selectedRoute == item.route,
                onClick = { onItemClick(item) },
                showLabel = showLabels,
            )
        }
    }
}

/**
 * Individual navigation bar item with animations
 */
@Composable
private fun RowScope.QodeNavigationBarItem(
    item: QodeNavigationItem,
    selected: Boolean,
    onClick: () -> Unit,
    showLabel: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }

    val scale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "nav_item_scale",
    )

    val iconColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = MotionTokens.Duration.MEDIUM),
        label = "nav_item_color",
    )

    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Box(
                modifier = Modifier.scale(scale),
                contentAlignment = Alignment.Center,
            ) {
                BadgedBox(
                    badge = {
                        item.badge?.let { badgeText ->
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError,
                            ) {
                                Text(
                                    text = badgeText,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                    },
                ) {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        tint = iconColor,
                    )
                }
            }
        },
        label = if (showLabel) {
            {
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        } else {
            null
        },
        enabled = item.enabled,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        interactionSource = interactionSource,
    )
}

/**
 * Rail navigation for larger screens or tablets
 */
@Composable
fun QodeNavigationRail(
    items: List<QodeNavigationItem>,
    selectedRoute: String,
    onItemClick: (QodeNavigationItem) -> Unit,
    modifier: Modifier = Modifier,
    header: @Composable (ColumnScope.() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface
) {
    NavigationRail(
        modifier = modifier,
        containerColor = containerColor,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        header = header,
    ) {
        Spacer(modifier = Modifier.height(SpacingTokens.sm))
        items.forEach { item ->
            QodeNavigationRailItem(
                item = item,
                selected = selectedRoute == item.route,
                onClick = { onItemClick(item) },
            )
        }
    }
}

/**
 * Individual navigation rail item
 */
@Composable
private fun QodeNavigationRailItem(
    item: QodeNavigationItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "rail_item_scale",
    )

    NavigationRailItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Box(
                modifier = Modifier.scale(scale),
                contentAlignment = Alignment.Center,
            ) {
                BadgedBox(
                    badge = {
                        item.badge?.let { badgeText ->
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError,
                            ) {
                                Text(
                                    text = badgeText,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                    },
                ) {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                    )
                }
            }
        },
        label = {
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        enabled = item.enabled,
        colors = NavigationRailItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
}

// Previews
@Preview(name = "Bottom Navigation", showBackground = true)
@Composable
private fun QodeBottomNavigationPreview() {
    QodeTheme {
        val items = listOf(
            QodeNavigationItem(
                route = "home",
                label = "Home",
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
            ),
            QodeNavigationItem(
                route = "search",
                label = "Search",
                selectedIcon = Icons.Filled.Search,
                unselectedIcon = Icons.Outlined.Search,
            ),
            QodeNavigationItem(
                route = "add",
                label = "Add",
                selectedIcon = Icons.Filled.Add,
                unselectedIcon = Icons.Outlined.Add,
                badge = "2",
            ),
            QodeNavigationItem(
                route = "favorites",
                label = "Favorites",
                selectedIcon = Icons.Filled.Favorite,
                unselectedIcon = Icons.Outlined.FavoriteBorder,
            ),
            QodeNavigationItem(
                route = "profile",
                label = "Profile",
                selectedIcon = Icons.Filled.Person,
                unselectedIcon = Icons.Outlined.Person,
            ),
        )

        Column {
            QodeBottomNavigation(
                items = items,
                selectedRoute = "home",
                onItemClick = {},
            )

            Spacer(modifier = Modifier.height(SpacingTokens.md))

            Text(
                "Without Labels",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = SpacingTokens.md),
            )

            QodeBottomNavigation(
                items = items,
                selectedRoute = "search",
                onItemClick = {},
                showLabels = false,
            )
        }
    }
}

@Preview(name = "Navigation Rail", showBackground = true)
@Composable
private fun QodeNavigationRailPreview() {
    QodeTheme {
        val items = listOf(
            QodeNavigationItem(
                route = "home",
                label = "Home",
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
            ),
            QodeNavigationItem(
                route = "search",
                label = "Search",
                selectedIcon = Icons.Filled.Search,
                unselectedIcon = Icons.Outlined.Search,
                badge = "5",
            ),
            QodeNavigationItem(
                route = "add",
                label = "Add",
                selectedIcon = Icons.Filled.Add,
                unselectedIcon = Icons.Outlined.Add,
            ),
            QodeNavigationItem(
                route = "favorites",
                label = "Favorites",
                selectedIcon = Icons.Filled.Favorite,
                unselectedIcon = Icons.Outlined.FavoriteBorder,
            ),
        )

        Row {
            QodeNavigationRail(
                items = items,
                selectedRoute = "home",
                onItemClick = {},
                header = {
                    IconButton(onClick = {}) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Menu",
                        )
                    }
                },
            )

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Box(
                    modifier = Modifier.padding(SpacingTokens.md),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Content Area")
                }
            }
        }
    }
}
