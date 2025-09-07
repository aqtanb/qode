package com.qodein.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.theme.ElevationTokens
import com.qodein.core.designsystem.theme.MotionTokens
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SpacingTokens

// MARK: - Data Classes

/**
 * Clean navigation item data class for floating navigation
 */
data class TabItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val enabled: Boolean = true,
    val contentDescription: String? = null // Custom accessibility description
)

// MARK: - Components

/**
 * Modern floating bottom navigation with glassmorphism design
 */
@Composable
fun BottomNavigation(
    items: List<TabItem>,
    selectedRoute: String,
    onItemClick: (TabItem) -> Unit,
    modifier: Modifier = Modifier,
    showLabels: Boolean = true,
    autoHidingState: AutoHidingState? = null
) {
    val density = LocalDensity.current
    val navigationBarsPadding = WindowInsets.navigationBars.asPaddingValues()

    // Calculate dynamic height based on content
    val baseHeight = if (showLabels) 64.dp else 48.dp
    val totalHeight = baseHeight + navigationBarsPadding.calculateBottomPadding()

    val content = @Composable {
        FloatingNavigationContainer(
            modifier = modifier
                .fillMaxWidth()
                .height(totalHeight),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = SpacingTokens.lg,
                        vertical = SpacingTokens.sm,
                    )
                    .padding(bottom = navigationBarsPadding.calculateBottomPadding()),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.forEach { item ->
                    FloatingNavigationItem(
                        item = item,
                        selected = selectedRoute == item.route,
                        onClick = { onItemClick(item) },
                        showLabel = showLabels,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }

    // Apply auto-hiding behavior if provided
    if (autoHidingState != null) {
        AutoHidingContent(
            state = autoHidingState,
            direction = AutoHideDirection.UP,
        ) {
            content()
        }
    } else {
        content()
    }
}

/**
 * Glassmorphism container for floating navigation
 */
@Composable
private fun FloatingNavigationContainer(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val isDarkTheme = surfaceColor.luminance() < 0.5f

    Box(
        modifier = modifier
            .padding(horizontal = SpacingTokens.md, vertical = SpacingTokens.xs),
    ) {
        // Glassmorphism background
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(ShapeTokens.Corner.extraLarge)),
            color = if (isDarkTheme) {
                surfaceColor.copy(alpha = 0.95f)
            } else {
                surfaceColor.copy(alpha = 0.98f)
            },
            shadowElevation = ElevationTokens.large,
            tonalElevation = ElevationTokens.medium,
        ) {
            // Subtle gradient overlay for depth
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                                Color.Transparent,
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.05f),
                            ),
                        ),
                    ),
            ) {
                content()
            }
        }
    }
}

/**
 * Individual floating navigation item with enhanced animations
 */
@Composable
private fun FloatingNavigationItem(
    item: TabItem,
    selected: Boolean,
    onClick: () -> Unit,
    showLabel: Boolean,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Enhanced animations
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
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

    val labelColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        },
        animationSpec = tween(durationMillis = MotionTokens.Duration.MEDIUM),
        label = "nav_label_color",
    )

    // Accessibility description
    val accessibilityDescription = buildString {
        append(item.contentDescription ?: item.label)
        if (selected) append(", selected")
    }

    Column(
        modifier = modifier
            .padding(horizontal = SpacingTokens.xs)
            .clearAndSetSemantics {
                contentDescription = accessibilityDescription
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Icon with selection indicator
        IconButton(
            onClick = onClick,
            enabled = item.enabled,
            interactionSource = interactionSource,
            modifier = Modifier.scale(scale),
        ) {
            Box(
                contentAlignment = Alignment.Center,
            ) {
                // Selected state indicator background
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                CircleShape,
                            ),
                    )
                }

                Icon(
                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                    contentDescription = null, // Handled by parent semantics
                    tint = iconColor,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        // Label with improved typography
        if (showLabel) {
            Spacer(modifier = Modifier.height(SpacingTokens.xxxs))
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                ),
                color = labelColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// MARK: - Preview

@Preview(name = "Floating Bottom Navigation", showBackground = true)
@Composable
private fun QodeBottomNavigationPreview() {
    QodeTheme {
        Column {
            // With labels
            BottomNavigation(
                items = listOf(
                    TabItem(
                        route = "home",
                        label = "Home",
                        selectedIcon = QodeNavigationIcons.Home,
                        unselectedIcon = QodeNavigationIcons.Home,
                        contentDescription = "Navigate to home screen",
                    ),
                    TabItem(
                        route = "feed",
                        label = "Feed",
                        selectedIcon = QodeNavigationIcons.Feed,
                        unselectedIcon = QodeNavigationIcons.Feed,
                        contentDescription = "Navigate to feed screen",
                    ),
                ),
                selectedRoute = "home",
                onItemClick = {},
                showLabels = true,
            )

            Spacer(modifier = Modifier.height(SpacingTokens.md))

            // Without labels
            BottomNavigation(
                items = listOf(
                    TabItem(
                        route = "home",
                        label = "Home",
                        selectedIcon = QodeNavigationIcons.Home,
                        unselectedIcon = QodeNavigationIcons.Home,
                    ),
                    TabItem(
                        route = "feed",
                        label = "Feed",
                        selectedIcon = QodeNavigationIcons.Feed,
                        unselectedIcon = QodeNavigationIcons.Feed,
                    ),
                ),
                selectedRoute = "feed",
                onItemClick = {},
                showLabels = false,
            )
        }
    }
}
