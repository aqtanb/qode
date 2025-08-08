package com.qodein.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.icon.QodeUIIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens

/**
 * TopAppBar variants for Qode design system
 */
enum class QodeTopAppBarVariant {
    Default,
    CenterAligned,
    Large
}

/**
 * TopAppBar scroll behavior types
 */
enum class QodeTopAppBarScrollBehavior {
    None,
    Pinned,
    EnterAlways,
    ExitUntilCollapsed
}

/**
 * Production-ready top app bar component for Qode design system
 *
 * @param title The title to display
 * @param modifier Modifier to be applied to the app bar
 * @param variant The variant of the app bar
 * @param navigationIcon Optional navigation icon (typically back or menu)
 * @param onNavigationClick Called when navigation icon is clicked
 * @param actions List of action buttons to display
 * @param scrollBehavior Scroll behavior configuration
 * @param colors Custom colors for the app bar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QodeTopAppBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    variant: QodeTopAppBarVariant = QodeTopAppBarVariant.Default,
    navigationIcon: ImageVector? = null,
    onNavigationClick: (() -> Unit)? = null,
    actions: List<TopAppBarAction> = emptyList(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
    colors: TopAppBarColors? = null
) {
    val defaultColors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        scrolledContainerColor = Color.Transparent,
        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    val effectiveColors = defaultColors

    when (variant) {
        QodeTopAppBarVariant.Default -> {
            TopAppBar(
                title = {
                    Text(
                        text = title ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = effectiveColors.titleContentColor,
                    )
                },
                modifier = modifier,
                navigationIcon = {
                    navigationIcon?.let { icon ->
                        IconButton(onClick = onNavigationClick ?: {}) {
                            Icon(
                                imageVector = icon,
                                contentDescription = when (icon) {
                                    Icons.AutoMirrored.Filled.ArrowBack -> "Navigate back"
                                    QodeUIIcons.Menu -> "Open menu"
                                    QodeActionIcons.Close -> "Close"
                                    else -> "Navigation"
                                },
                                tint = effectiveColors.navigationIconContentColor,
                            )
                        }
                    }
                },
                actions = {
                    actions.forEach { action ->
                        if (action.showAsAction) {
                            IconButton(
                                onClick = action.onClick,
                                enabled = action.enabled,
                            ) {
                                Icon(
                                    imageVector = action.icon,
                                    contentDescription = action.contentDescription,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    // Overflow menu for non-icon actions
                    val overflowActions = actions.filter { !it.showAsAction }
                    if (overflowActions.isNotEmpty()) {
                        OverflowMenu(actions = overflowActions)
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = effectiveColors,
            )
        }

        QodeTopAppBarVariant.CenterAligned -> {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = title ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = effectiveColors.titleContentColor,
                    )
                },
                modifier = modifier,
                navigationIcon = {
                    navigationIcon?.let { icon ->
                        IconButton(onClick = onNavigationClick ?: {}) {
                            Icon(
                                imageVector = icon,
                                contentDescription = when (icon) {
                                    Icons.AutoMirrored.Filled.ArrowBack -> "Navigate back"
                                    QodeUIIcons.Menu -> "Open menu"
                                    QodeActionIcons.Close -> "Close"
                                    else -> "Navigation"
                                },
                                tint = effectiveColors.navigationIconContentColor,
                            )
                        }
                    }
                },
                actions = {
                    actions.forEach { action ->
                        if (action.showAsAction) {
                            IconButton(
                                onClick = action.onClick,
                                enabled = action.enabled,
                            ) {
                                Icon(
                                    imageVector = action.icon,
                                    contentDescription = action.contentDescription,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    val overflowActions = actions.filter { !it.showAsAction }
                    if (overflowActions.isNotEmpty()) {
                        OverflowMenu(actions = overflowActions)
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = effectiveColors,
            )
        }

        QodeTopAppBarVariant.Large -> {
            LargeTopAppBar(
                title = {
                    Text(
                        text = title ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = effectiveColors.titleContentColor,
                    )
                },
                modifier = modifier,
                navigationIcon = {
                    navigationIcon?.let { icon ->
                        IconButton(onClick = onNavigationClick ?: {}) {
                            Icon(
                                imageVector = icon,
                                contentDescription = when (icon) {
                                    Icons.AutoMirrored.Filled.ArrowBack -> "Navigate back"
                                    QodeUIIcons.Menu -> "Open menu"
                                    QodeActionIcons.Close -> "Close"
                                    else -> "Navigation"
                                },
                                tint = effectiveColors.navigationIconContentColor,
                            )
                        }
                    }
                },
                actions = {
                    actions.forEach { action ->
                        if (action.showAsAction) {
                            IconButton(
                                onClick = action.onClick,
                                enabled = action.enabled,
                            ) {
                                Icon(
                                    imageVector = action.icon,
                                    contentDescription = action.contentDescription,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    val overflowActions = actions.filter { !it.showAsAction }
                    if (overflowActions.isNotEmpty()) {
                        OverflowMenu(actions = overflowActions)
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = effectiveColors,
            )
        }
    }
}

/**
 * Search-focused top app bar variant
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QodeSearchTopAppBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchClose: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
    onSearchAction: () -> Unit = {},
    actions: List<TopAppBarAction> = emptyList()
) {
    TopAppBar(
        title = {
            QodeTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                variant = QodeTextFieldVariant.Search,
                placeholder = placeholder,
                singleLine = true,
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onSearch = { onSearchAction() },
                ),
            )
        },
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = onSearchClose) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Close search",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        actions = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchQueryChange("") }) {
                    Icon(
                        imageVector = QodeActionIcons.Close,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            actions.forEach { action ->
                if (action.showAsAction) {
                    IconButton(
                        onClick = action.onClick,
                        enabled = action.enabled,
                    ) {
                        Icon(
                            imageVector = action.icon,
                            contentDescription = action.contentDescription,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
}

/**
 * Data class representing a top app bar action
 */
data class TopAppBarAction(
    val icon: ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
    val showAsAction: Boolean = true
)

/**
 * Overflow menu for additional actions
 */
@Composable
private fun OverflowMenu(actions: List<TopAppBarAction>) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = QodeNavigationIcons.More,
                contentDescription = "More options",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            actions.forEach { action ->
                DropdownMenuItem(
                    text = { Text(action.contentDescription) },
                    onClick = {
                        action.onClick()
                        expanded = false
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = action.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    enabled = action.enabled,
                )
            }
        }
    }
}

/**
 * Compact screen-specific top app bar with navigation actions
 * Layout: Favorites (left) | Screen Title (center) | Profile + Settings (right)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QodeScreenTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onFavoritesClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    profileImageUrl: String? = null
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = onFavoritesClick) {
                Icon(
                    imageVector = QodeNavigationIcons.Favorites,
                    contentDescription = "Favorites",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        actions = {
            // Profile Avatar Button
            IconButton(onClick = onProfileClick) {
                QodeProfileAvatar(
                    imageUrl = profileImageUrl,
                    size = SizeTokens.Icon.sizeXLarge,
                    contentDescription = "Profile",
                )
            }

            // Settings Button
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = QodeNavigationIcons.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
}

/**
 * Truly transparent top app bar that bypasses Material3's automatic background handling
 * Perfect for screens that need completely transparent overlays (like Profile screen)
 */
@Composable
fun QodeTransparentTopAppBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    navigationIcon: ImageVector? = null,
    onNavigationClick: (() -> Unit)? = null,
    actions: List<TopAppBarAction> = emptyList(),
    navigationIconTint: Color = MaterialTheme.colorScheme.onSurface,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    actionIconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding() // Handle status bar padding
            .height(SizeTokens.AppBar.height),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Navigation Icon
        Box(
            modifier = Modifier.weight(0.2f, fill = false),
            contentAlignment = Alignment.CenterStart,
        ) {
            navigationIcon?.let { icon ->
                IconButton(onClick = onNavigationClick ?: {}) {
                    Icon(
                        imageVector = icon,
                        contentDescription = when (icon) {
                            QodeActionIcons.Back -> "Navigate back"
                            QodeUIIcons.Menu -> "Open menu"
                            QodeActionIcons.Close -> "Close"
                            else -> "Navigation"
                        },
                        tint = navigationIconTint,
                    )
                }
            }
        }

        // Title (Center-aligned)
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            title?.let { titleText ->
                Text(
                    text = titleText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = titleColor,
                )
            }
        }

        // Actions
        Row(
            modifier = Modifier.weight(0.2f, fill = false),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            actions.forEach { action ->
                if (action.showAsAction) {
                    IconButton(
                        onClick = action.onClick,
                        enabled = action.enabled,
                    ) {
                        Icon(
                            imageVector = action.icon,
                            contentDescription = action.contentDescription,
                            tint = actionIconTint,
                        )
                    }
                }
            }

            // Overflow menu for non-icon actions
            val overflowActions = actions.filter { !it.showAsAction }
            if (overflowActions.isNotEmpty()) {
                OverflowMenu(actions = overflowActions)
            }
        }
    }
}

/**
 * Profile avatar component for top app bar
 */
@Composable
fun QodeProfileAvatar(
    imageUrl: String?,
    size: androidx.compose.ui.unit.Dp,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        if (imageUrl != null) {
            // TODO: Add Coil AsyncImage when profile images are implemented
            // AsyncImage(
            //     model = imageUrl,
            //     contentDescription = contentDescription,
            //     modifier = Modifier.fillMaxSize(),
            //     contentScale = ContentScale.Crop
            // )

            // Placeholder for now
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(size * 0.6f),
            )
        } else {
            // Default placeholder
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(size * 0.6f),
            )
        }
    }
}

// Previews
@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "TopAppBar Variants", showBackground = true)
@Composable
private fun QodeTopAppBarPreview() {
    QodeTheme {
        Column {
            // Default variant with navigation and actions
            QodeTopAppBar(
                title = "Qode",
                navigationIcon = QodeUIIcons.Menu,
                onNavigationClick = {},
                actions = listOf(
                    TopAppBarAction(
                        icon = QodeNavigationIcons.Search,
                        contentDescription = "Search",
                        onClick = {},
                    ),
                    TopAppBarAction(
                        icon = QodeNavigationIcons.Notifications,
                        contentDescription = "Notifications",
                        onClick = {},
                    ),
                ),
            )

            Spacer(modifier = Modifier.height(SpacingTokens.sm))

            // Center aligned variant with back navigation
            QodeTopAppBar(
                title = "Store Details",
                variant = QodeTopAppBarVariant.CenterAligned,
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = {},
                actions = listOf(
                    TopAppBarAction(
                        icon = QodeActionIcons.Share,
                        contentDescription = "Share",
                        onClick = {},
                    ),
                    TopAppBarAction(
                        icon = QodeNavigationIcons.Favorites,
                        contentDescription = "Add to favorites",
                        onClick = {},
                    ),
                ),
            )

            Spacer(modifier = Modifier.height(SpacingTokens.sm))

            // Large variant for primary screens
            QodeTopAppBar(
                title = "Promo Codes",
                variant = QodeTopAppBarVariant.Large,
                actions = listOf(
                    TopAppBarAction(
                        icon = QodeActionIcons.Add,
                        contentDescription = "Add promo code",
                        onClick = {},
                    ),
                ),
            )
        }
    }
}

@Preview(name = "Search TopAppBar", showBackground = true)
@Composable
private fun QodeSearchTopAppBarPreview() {
    QodeTheme {
        Column {
            // Empty search state
            var searchQuery1 by remember { mutableStateOf("") }
            QodeSearchTopAppBar(
                searchQuery = searchQuery1,
                onSearchQueryChange = { searchQuery1 = it },
                onSearchClose = {},
                placeholder = "Search promo codes...",
                actions = listOf(
                    TopAppBarAction(
                        icon = QodeNavigationIcons.Filter,
                        contentDescription = "Filter",
                        onClick = {},
                    ),
                ),
            )

            Spacer(modifier = Modifier.height(SpacingTokens.sm))

            // Search with query
            var searchQuery2 by remember { mutableStateOf("Summer Sale") }
            QodeSearchTopAppBar(
                searchQuery = searchQuery2,
                onSearchQueryChange = { searchQuery2 = it },
                onSearchClose = {},
                placeholder = "Search stores...",
                actions = listOf(
                    TopAppBarAction(
                        icon = QodeNavigationIcons.Filter,
                        contentDescription = "Sort",
                        onClick = {},
                    ),
                ),
            )
        }
    }
}

@Preview(name = "Screen TopAppBar", showBackground = true)
@Composable
private fun QodeScreenTopAppBarPreview() {
    QodeTheme {
        Column {
            // Home screen variant
            QodeScreenTopAppBar(
                title = "Home",
                onFavoritesClick = {},
                onProfileClick = {},
                onSettingsClick = {},
            )

            Spacer(modifier = Modifier.height(SpacingTokens.sm))

            // Inbox screen variant
            QodeScreenTopAppBar(
                title = "Inbox",
                onFavoritesClick = {},
                onProfileClick = {},
                onSettingsClick = {},
            )

            Spacer(modifier = Modifier.height(SpacingTokens.sm))

            // Search screen variant
            QodeScreenTopAppBar(
                title = "Search",
                onFavoritesClick = {},
                onProfileClick = {},
                onSettingsClick = {},
                profileImageUrl = null, // Shows placeholder profile avatar
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "TopAppBar States", showBackground = true)
@Composable
private fun QodeTopAppBarStatesPreview() {
    QodeTheme {
        Column {
            // No navigation icon
            QodeTopAppBar(
                title = "Settings",
                actions = listOf(
                    TopAppBarAction(
                        icon = QodeNavigationIcons.Settings,
                        contentDescription = "Settings",
                        onClick = {},
                    ),
                ),
            )

            Spacer(modifier = Modifier.height(SpacingTokens.sm))

            // With overflow menu
            QodeTopAppBar(
                title = "Store Details",
                variant = QodeTopAppBarVariant.CenterAligned,
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = {},
                actions = listOf(
                    TopAppBarAction(
                        icon = QodeActionIcons.Share,
                        contentDescription = "Share",
                        onClick = {},
                    ),
                    TopAppBarAction(
                        icon = QodeNavigationIcons.Favorites,
                        contentDescription = "Favorite",
                        onClick = {},
                    ),
                    TopAppBarAction(
                        icon = QodeActionIcons.Download,
                        contentDescription = "Download",
                        onClick = {},
                        showAsAction = false, // This will go to overflow menu
                    ),
                    TopAppBarAction(
                        icon = QodeActionIcons.Delete,
                        contentDescription = "Delete",
                        onClick = {},
                        showAsAction = false, // This will go to overflow menu
                    ),
                ),
            )

            Spacer(modifier = Modifier.height(SpacingTokens.sm))

            // Disabled action example
            QodeTopAppBar(
                title = "Profile",
                actions = listOf(
                    TopAppBarAction(
                        icon = QodeActionIcons.Edit,
                        contentDescription = "Edit profile",
                        onClick = {},
                        enabled = false,
                    ),
                ),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "TopAppBar Dark Theme", showBackground = true)
@Composable
private fun QodeTopAppBarDarkPreview() {
    QodeTheme(darkTheme = true) {
        Column {
            QodeTopAppBar(
                title = "Dark Theme",
                navigationIcon = QodeUIIcons.Menu,
                onNavigationClick = {},
                actions = listOf(
                    TopAppBarAction(
                        icon = QodeNavigationIcons.Search,
                        contentDescription = "Search",
                        onClick = {},
                    ),
                    TopAppBarAction(
                        icon = QodeNavigationIcons.Notifications,
                        contentDescription = "Notifications",
                        onClick = {},
                    ),
                ),
            )

            Spacer(modifier = Modifier.height(SpacingTokens.sm))

            QodeScreenTopAppBar(
                title = "Dark Mode Screen",
                onFavoritesClick = {},
                onProfileClick = {},
                onSettingsClick = {},
            )
        }
    }
}
