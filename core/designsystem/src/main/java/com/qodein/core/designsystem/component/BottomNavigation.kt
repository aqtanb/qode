package com.qodein.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import com.qodein.core.designsystem.icon.QodeCategoryIcons
import com.qodein.core.designsystem.icon.QodeIcons
import com.qodein.core.designsystem.theme.ElevationTokens
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import kotlin.math.roundToInt

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
 * Navigation action sealed class for different behaviors
 */
sealed class NavigationAction {
    data class Navigate(val tabItem: TabItem) : NavigationAction()
    data class ScrollToTop(val tabItem: TabItem) : NavigationAction()
}

/**
 * Modern floating bottom navigation with glassmorphism design and scroll-to-top support
 */
@Composable
fun BottomNavigation(
    items: List<TabItem>,
    selectedRoute: String,
    onNavigationAction: (NavigationAction) -> Unit,
    modifier: Modifier = Modifier,
    showLabels: Boolean = true,
    autoHidingState: AutoHidingState? = null
) {
    val density = LocalDensity.current
    val navigationBarsPadding = WindowInsets.navigationBars.asPaddingValues()

    // Calculate dynamic height for container using navigation tokens
    val containerHeight = if (showLabels) SizeTokens.Controller.containerHeightWithLabels else SizeTokens.Controller.containerHeight

    val content = @Composable {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(containerHeight)
                .padding(bottom = SpacingTokens.xl + navigationBarsPadding.calculateBottomPadding()),
            contentAlignment = Alignment.BottomCenter,
        ) {
            SwipeableNavigation(
                items = items,
                selectedRoute = selectedRoute,
                onNavigationAction = onNavigationAction,
                showLabels = showLabels,
                modifier = Modifier,
            )
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
 * High-performance swipeable navigation container using draggable
 */
@Composable
private fun SwipeableNavigation(
    items: List<TabItem>,
    selectedRoute: String,
    onNavigationAction: (NavigationAction) -> Unit,
    showLabels: Boolean,
    modifier: Modifier = Modifier
) {
    val swipeThreshold = 20f
    val currentIndex = items.indexOfFirst { it.route == selectedRoute }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    val draggableState = rememberDraggableState { delta ->
        dragOffset = (dragOffset + delta).coerceIn(-30f, 30f)
    }

    Surface(
        modifier = modifier
            .width(SizeTokens.Controller.pillWidth)
            .height(if (showLabels) SizeTokens.Controller.pillHeightWithLabels else SizeTokens.Controller.pillHeight)
            .clip(RoundedCornerShape(ShapeTokens.Corner.full))
            .draggable(
                state = draggableState,
                orientation = Orientation.Horizontal,
                onDragStarted = { dragOffset = 0f },
                onDragStopped = {
                    when {
                        dragOffset > swipeThreshold && currentIndex > 0 -> {
                            onNavigationAction(NavigationAction.Navigate(items[currentIndex - 1]))
                        }
                        dragOffset < -swipeThreshold && currentIndex < items.size - 1 -> {
                            onNavigationAction(NavigationAction.Navigate(items[currentIndex + 1]))
                        }
                    }
                    dragOffset = 0f
                },
            ),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shadowElevation = ElevationTokens.none,
        tonalElevation = ElevationTokens.small,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = SpacingTokens.xs,
                    vertical = SpacingTokens.xs,
                )
                .offset { IntOffset(dragOffset.roundToInt(), 0) },
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.forEach { item ->
                    val isSelected = selectedRoute == item.route

                    // Optimized item with smart action handling
                    OptimizedNavigationItem(
                        item = item,
                        selected = isSelected,
                        onClick = {
                            if (isSelected) {
                                // Handle scroll to top for already selected tab
                                onNavigationAction(NavigationAction.ScrollToTop(item))
                            } else {
                                // Handle normal navigation to new tab
                                onNavigationAction(NavigationAction.Navigate(item))
                            }
                        },
                        showLabel = showLabels,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

/**
 * Ultra-optimized navigation item with minimal recompositions
 */
@Composable
private fun OptimizedNavigationItem(
    item: TabItem,
    selected: Boolean,
    onClick: () -> Unit,
    showLabel: Boolean,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Simplified animations - no complex calculations during swipe
    val iconColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "icon_color",
    )

    val labelColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 400f),
        label = "label_color",
    )

    Column(
        modifier = modifier
            .clearAndSetSemantics {
                contentDescription = buildString {
                    append(item.contentDescription ?: item.label)
                    if (selected) append(", selected")
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        IconButton(
            onClick = onClick,
            enabled = item.enabled,
            interactionSource = interactionSource,
            modifier = Modifier
                .size(SizeTokens.IconButton.sizeLarge)
                .graphicsLayer {
                    scaleX = if (isPressed) {
                        0.9f
                    } else if (selected) {
                        1.05f
                    } else {
                        1f
                    }
                    scaleY = if (isPressed) {
                        0.9f
                    } else if (selected) {
                        1.05f
                    } else {
                        1f
                    }
                },
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Simple selection indicator
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(SizeTokens.IconButton.sizeLarge)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                    )
                }

                // Press state
                if (isPressed && !selected) {
                    Box(
                        modifier = Modifier
                            .size(SizeTokens.IconButton.sizeLarge)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                CircleShape,
                            ),
                    )
                }

                Icon(
                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(SizeTokens.Icon.sizeLarge),
                )
            }
        }

        if (showLabel) {
            Spacer(modifier = Modifier.height(SpacingTokens.xxs))
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
            // Without labels
            BottomNavigation(
                items = listOf(
                    TabItem(
                        route = "home",
                        label = "Home",
                        selectedIcon = QodeIcons.Promocode,
                        unselectedIcon = QodeIcons.Promocode,
                    ),
                    TabItem(
                        route = "feed",
                        label = "Feed",
                        selectedIcon = QodeCategoryIcons.Consulting,
                        unselectedIcon = QodeCategoryIcons.Consulting,
                    ),
                ),
                selectedRoute = "feed",
                onNavigationAction = { action ->
                    // Handle navigation actions in preview
                    when (action) {
                        is NavigationAction.Navigate -> {
                            // Navigate to tab: ${action.tabItem.route}
                        }
                        is NavigationAction.ScrollToTop -> {
                            // Scroll to top: ${action.tabItem.route}
                        }
                    }
                },
                showLabels = false,
            )
        }
    }
}
