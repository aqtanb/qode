package com.qodein.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.designsystem.icon.QodeCategoryIcons
import com.qodein.core.designsystem.icon.QodeCommerceIcons
import com.qodein.core.designsystem.theme.AnimationTokens
import com.qodein.core.designsystem.theme.ElevationTokens
import com.qodein.core.designsystem.theme.MotionTokens
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.ShapeTokens
import com.qodein.core.designsystem.theme.SizeTokens
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
            FloatingNavigationContainer(
                modifier = Modifier,
                showLabels = showLabels,
            ) {
                Row(
                    modifier = Modifier
                        .padding(
                            horizontal = SpacingTokens.xs,
                            vertical = SpacingTokens.xs,
                        ),
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
 * Material 3 floating pill container for navigation
 */
@Composable
private fun FloatingNavigationContainer(
    modifier: Modifier = Modifier,
    showLabels: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    // Material 3 floating pill surface with semantic sizing
    Surface(
        modifier = modifier
            .width(SizeTokens.Controller.pillWidth)
            .height(if (showLabels) SizeTokens.Controller.pillHeightWithLabels else SizeTokens.Controller.pillHeight)
            .clip(RoundedCornerShape(ShapeTokens.Corner.full)),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shadowElevation = ElevationTokens.none,
        tonalElevation = ElevationTokens.small,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
            content = content,
        )
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

    // Enhanced Material 3 animations with press feedback
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> MotionTokens.Scale.PRESSED
            selected -> MotionTokens.Scale.HOVER
            else -> 1f
        },
        animationSpec = AnimationTokens.Spec.emphasized,
        label = "nav_item_scale",
    )

    val iconColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.onPrimary
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
            MaterialTheme.colorScheme.onSurfaceVariant
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
            .clearAndSetSemantics {
                contentDescription = accessibilityDescription
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Icon with selection indicator and improved touch target
        IconButton(
            onClick = onClick,
            enabled = item.enabled && !selected, // Disable when already selected
            interactionSource = interactionSource,
            modifier = Modifier
                .scale(scale)
                .size(SizeTokens.IconButton.sizeLarge),
        ) {
            Box(
                contentAlignment = Alignment.Center,
            ) {
                // Material 3 selected state indicator with semantic sizing
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(SizeTokens.IconButton.sizeLarge)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                CircleShape,
                            ),
                    )
                }

                // Press state indicator for better feedback
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
                    contentDescription = null, // Handled by parent semantics
                    tint = iconColor,
                    modifier = Modifier.size(SizeTokens.Icon.sizeLarge),
                )
            }
        }

        // Label with improved typography using semantic spacing
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
                        selectedIcon = QodeCommerceIcons.Coupon,
                        unselectedIcon = QodeCommerceIcons.Coupon,
                    ),
                    TabItem(
                        route = "feed",
                        label = "Feed",
                        selectedIcon = QodeCategoryIcons.Consulting,
                        unselectedIcon = QodeCategoryIcons.Consulting,
                    ),
                ),
                selectedRoute = "feed",
                onItemClick = {},
                showLabels = false,
            )
        }
    }
}
