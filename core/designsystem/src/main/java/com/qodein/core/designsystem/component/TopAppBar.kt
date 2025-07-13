package com.qodein.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.designsystem.theme.QodeSpacing
import com.qodein.core.designsystem.theme.QodeTheme

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
    title: String,
    modifier: Modifier = Modifier,
    variant: QodeTopAppBarVariant = QodeTopAppBarVariant.Default,
    navigationIcon: ImageVector? = null,
    onNavigationClick: (() -> Unit)? = null,
    actions: List<TopAppBarAction> = emptyList(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
    colors: TopAppBarColors? = null
) {
    val defaultColors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surface,
        scrolledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    val effectiveColors = colors ?: defaultColors

    when (variant) {
        QodeTopAppBarVariant.Default -> {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
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
                                    Icons.Default.Menu -> "Open menu"
                                    Icons.Default.Close -> "Close"
                                    else -> "Navigation"
                                },
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
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
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
                                    Icons.Default.Menu -> "Open menu"
                                    Icons.Default.Close -> "Close"
                                    else -> "Navigation"
                                },
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
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
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
                                    Icons.Default.Menu -> "Open menu"
                                    Icons.Default.Close -> "Close"
                                    else -> "Navigation"
                                },
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
                )
            }
        },
        actions = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear search",
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
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More options",
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
                        )
                    },
                    enabled = action.enabled,
                )
            }
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
            QodeTopAppBar(
                title = "Qode",
                navigationIcon = Icons.Default.Menu,
                onNavigationClick = {},
                actions = listOf(
                    TopAppBarAction(
                        icon = Icons.Default.Search,
                        contentDescription = "Search",
                        onClick = {},
                    ),
                    TopAppBarAction(
                        icon = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        onClick = {},
                    ),
                ),
            )

            Spacer(modifier = Modifier.height(QodeSpacing.sm))

            QodeTopAppBar(
                title = "Store Details",
                variant = QodeTopAppBarVariant.CenterAligned,
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationClick = {},
                actions = listOf(
                    TopAppBarAction(
                        icon = Icons.Default.Share,
                        contentDescription = "Share",
                        onClick = {},
                    ),
                    TopAppBarAction(
                        icon = Icons.Default.Favorite,
                        contentDescription = "Add to favorites",
                        onClick = {},
                    ),
                ),
            )

            Spacer(modifier = Modifier.height(QodeSpacing.sm))

            QodeTopAppBar(
                title = "Promo Codes",
                variant = QodeTopAppBarVariant.Large,
                actions = listOf(
                    TopAppBarAction(
                        icon = Icons.Default.Add,
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
        var searchQuery by remember { mutableStateOf("") }

        QodeSearchTopAppBar(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            onSearchClose = {},
            placeholder = "Search promo codes...",
        )
    }
}
