package com.qodein.core.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.component.QodeTransparentTopAppBar
import com.qodein.core.designsystem.component.TopAppBarAction

/**
 * Configuration for scroll-aware behavior
 */
data class ScrollAwareConfig(
    val alwaysVisibleThreshold: Dp = 50.dp,
    val hideAccumulationThreshold: Dp = 150.dp,
    val showOnScrollUpThreshold: Dp = 8.dp,
    val enableDebouncing: Boolean = true,
    val debounceDelayMs: Long = 50L
)

/**
 * Scroll direction state for better state management
 */
private sealed interface ScrollDirection {
    data object Idle : ScrollDirection
    data class Down(val accumulated: Int) : ScrollDirection
    data class Up(val delta: Int) : ScrollDirection
}

/**
 * Enterprise-level scroll-aware TopAppBar with configurable behavior
 *
 * This component provides smooth, user-friendly hide/show behavior based on scroll patterns.
 * Built for production use with proper state management and performance optimization.
 *
 * @param scrollState The ScrollState to monitor for scroll direction changes
 * @param modifier Modifier to be applied to the component
 * @param title Optional title to display in the center
 * @param navigationIcon Optional navigation icon (typically back arrow)
 * @param onNavigationClick Called when navigation icon is clicked
 * @param actions List of action buttons to display on the right
 * @param config Configuration for scroll behavior thresholds and timing
 * @param navigationIconTint Tint color for the navigation icon
 * @param titleColor Color for the title text
 * @param actionIconTint Tint color for action icons
 */
@Composable
fun AutoHidingTopAppBar(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    title: String? = null,
    navigationIcon: ImageVector? = null,
    onNavigationClick: (() -> Unit)? = null,
    actions: List<TopAppBarAction> = emptyList(),
    config: ScrollAwareConfig = ScrollAwareConfig(),
    navigationIconTint: Color = MaterialTheme.colorScheme.onSurface,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    actionIconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val scrollBehaviorState = rememberScrollBehaviorState(
        scrollState = scrollState,
        config = config,
    )

    AnimatedVisibility(
        visible = scrollBehaviorState.isVisible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
        ),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        ),
        modifier = modifier,
    ) {
        QodeTransparentTopAppBar(
            title = title,
            navigationIcon = navigationIcon,
            onNavigationClick = onNavigationClick,
            actions = actions,
            navigationIconTint = navigationIconTint,
            titleColor = titleColor,
            actionIconTint = actionIconTint,
        )
    }
}

/**
 * State holder for scroll behavior logic
 */
@Stable
private class ScrollBehaviorState(private val config: ScrollAwareConfig) {
    var isVisible by mutableStateOf(true)
        private set

    private var previousScrollValue = 0
    private var accumulatedScrollDown = 0
    private var lastUpdateTime = 0L

    fun updateScrollState(currentScrollValue: Int) {
        val currentTime = System.currentTimeMillis()

        // Debouncing to prevent excessive updates
        if (config.enableDebouncing && currentTime - lastUpdateTime < config.debounceDelayMs) {
            return
        }

        val scrollDelta = currentScrollValue - previousScrollValue
        val scrollDirection = determineScrollDirection(currentScrollValue, scrollDelta)

        updateVisibility(scrollDirection)

        previousScrollValue = currentScrollValue
        lastUpdateTime = currentTime
    }

    private fun determineScrollDirection(
        currentScrollValue: Int,
        scrollDelta: Int
    ): ScrollDirection =
        when {
            // Always show when near top
            currentScrollValue <= config.alwaysVisibleThreshold.value.toInt() -> {
                accumulatedScrollDown = 0
                ScrollDirection.Idle
            }
            // Scrolling down
            scrollDelta > 0 -> {
                accumulatedScrollDown += scrollDelta
                ScrollDirection.Down(accumulatedScrollDown)
            }
            // Scrolling up with sufficient delta
            scrollDelta < -config.showOnScrollUpThreshold.value.toInt() -> {
                accumulatedScrollDown = 0
                ScrollDirection.Up(kotlin.math.abs(scrollDelta))
            }
            // Minor movements - maintain current state
            else -> ScrollDirection.Idle
        }

    private fun updateVisibility(direction: ScrollDirection) {
        when (direction) {
            is ScrollDirection.Idle -> {
                // Near top - always show
                if (accumulatedScrollDown == 0) {
                    isVisible = true
                }
            }
            is ScrollDirection.Down -> {
                if (direction.accumulated >= config.hideAccumulationThreshold.value.toInt()) {
                    isVisible = false
                }
            }
            is ScrollDirection.Up -> {
                isVisible = true
            }
        }
    }
}

/**
 * Remember function for scroll behavior state
 */
@Composable
private fun rememberScrollBehaviorState(
    scrollState: ScrollState,
    config: ScrollAwareConfig
): ScrollBehaviorState {
    val behaviorState = remember { ScrollBehaviorState(config) }

    LaunchedEffect(scrollState.value) {
        behaviorState.updateScrollState(scrollState.value)
    }

    return behaviorState
}
