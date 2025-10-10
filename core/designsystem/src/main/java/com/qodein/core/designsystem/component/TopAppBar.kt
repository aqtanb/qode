package com.qodein.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.icon.QodeUIIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens

// MARK: - Data Models & Enums

/**
 * TopAppBar variants for Qode design system
 */
enum class QodeTopAppBarVariant {
    CenterAligned,
    Transparent
}

// MARK: - Core TopAppBar Component

/**
 * Ultimate customizable top app bar component for Qode design system
 *
 * @param modifier Modifier to be applied to the app bar
 * @param title The title string to display
 * @param titleComposable Custom composable for the title (overrides title string)
 * @param navigationIcon Optional navigation icon (completely nullable)
 * @param onNavigationClick Called when navigation icon is clicked
 * @param actions List of action buttons to display
 * @param navigationIconTint Tint color for navigation icon
 * @param titleColor Color for title text
 * @param actionIconTint Tint color for action icons
 * @param backgroundColor Background color for the app bar
 * @param variant The variant of the app bar
 * @param elevation Elevation of the app bar
 * @param statusBarPadding Whether to add status bar padding
 * @param scrollBehavior Scroll behavior configuration
 * @param colors Custom Material3 colors (overrides individual color params)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QodeTopAppBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    titleComposable: (@Composable () -> Unit)? = null,
    navigationIcon: ImageVector? = null,
    onNavigationClick: (() -> Unit)? = null,
    actions: List<TopAppBarAction> = emptyList(),
    customActions: (@Composable RowScope.() -> Unit)? = null,
    navigationIconTint: Color = MaterialTheme.colorScheme.onSurface,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    actionIconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    variant: QodeTopAppBarVariant = QodeTopAppBarVariant.CenterAligned,
    elevation: Dp = 0.dp,
    statusBarPadding: Boolean = false,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    colors: TopAppBarColors? = null
) {
    val finalColors = colors ?: TopAppBarDefaults.topAppBarColors(
        containerColor = backgroundColor,
        navigationIconContentColor = navigationIconTint,
        titleContentColor = titleColor,
        actionIconContentColor = actionIconTint,
    )

    val finalModifier = if (statusBarPadding) {
        modifier.statusBarsPadding()
    } else {
        modifier
    }

    val titleContent: @Composable () -> Unit = titleComposable ?: {
        title?.let {
            Text(
                text = it,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = titleColor,
            )
        }
    }

    val navigationContent: @Composable () -> Unit = {
        navigationIcon?.let { icon ->
            IconButton(
                onClick = onNavigationClick ?: {},
                enabled = onNavigationClick != null,
            ) {
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

    val actionsContent: @Composable RowScope.() -> Unit = {
        val visibleActions = actions.filter { it.showAsAction }

        visibleActions.forEach { action ->
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

        // Custom actions
        customActions?.invoke(this)
    }

    when (variant) {
        QodeTopAppBarVariant.CenterAligned -> {
            CenterAlignedTopAppBar(
                title = titleContent,
                modifier = finalModifier,
                navigationIcon = navigationContent,
                actions = actionsContent,
                colors = finalColors,
                scrollBehavior = scrollBehavior,
            )
        }
        QodeTopAppBarVariant.Transparent -> {
            QodeTransparentTopAppBar(
                modifier = modifier,
                title = title,
                titleComposable = titleComposable,
                navigationIcon = navigationIcon,
                onNavigationClick = onNavigationClick,
                actions = actions,
                customActions = customActions,
                navigationIconTint = navigationIconTint,
                titleColor = titleColor,
                actionIconTint = actionIconTint,
                statusBarPadding = statusBarPadding,
            )
        }
    }
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

// MARK: - Transparent Implementation

/**
 * Internal implementation for transparent variant that integrates with QodeTopAppBar
 */
@Composable
private fun QodeTransparentTopAppBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    titleComposable: (@Composable () -> Unit)? = null,
    navigationIcon: ImageVector? = null,
    onNavigationClick: (() -> Unit)? = null,
    actions: List<TopAppBarAction> = emptyList(),
    customActions: (@Composable RowScope.() -> Unit)? = null,
    navigationIconTint: Color = MaterialTheme.colorScheme.onSurface,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    actionIconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    statusBarPadding: Boolean = false
) {
    val finalModifier = if (statusBarPadding) {
        modifier.statusBarsPadding()
    } else {
        modifier
    }

    val titleContent: @Composable () -> Unit = titleComposable ?: {
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

    Row(
        modifier = finalModifier
            .fillMaxWidth()
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
                IconButton(
                    onClick = onNavigationClick ?: {},
                    modifier = Modifier
                        .size(SizeTokens.IconButton.sizeLarge)
                        .padding(start = SpacingTokens.sm)
                        .background(
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                            shape = CircleShape,
                        ),
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = when (icon) {
                            QodeActionIcons.Back -> "Navigate back"
                            QodeUIIcons.Menu -> "Open menu"
                            QodeActionIcons.Close -> "Close"
                            else -> "Navigation"
                        },
                        tint = navigationIconTint,
                        modifier = Modifier.size(SizeTokens.Icon.sizeLarge),
                    )
                }
            }
        }

        // Title (Center-aligned)
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            titleContent()
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

            // Custom actions
            customActions?.invoke(this)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewLightDark
@Composable
private fun QodeTopAppBarStatesPreview() {
    QodeTheme {
        Column {
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
