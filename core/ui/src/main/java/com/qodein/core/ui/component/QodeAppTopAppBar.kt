package com.qodein.core.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.qodein.core.designsystem.component.QodeTopAppBar
import com.qodein.core.designsystem.component.QodeTopAppBarVariant
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.shared.model.User

/**
 * Screen types that determine top app bar behavior and appearance
 */
enum class TopAppBarScreenType {
    /** No top app bar shown (submission wizard, onboarding) */
    None,

    /** Main app screens with profile/settings actions */
    Main,

    /** Nested screens with back navigation and transparent background */
    Nested,

    /** Scroll-aware top bar that hides on scroll down */
    ScrollAware
}

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
 * Unified top app bar component for the entire Qode application.
 * Handles all screen types and behaviors in one consistent component.
 *
 * @param title Screen title to display
 * @param screenType Type of screen determining behavior and appearance
 * @param user Current user for profile avatar (null for unauthenticated)
 * @param modifier Modifier for styling
 * @param navigationIcon Optional navigation icon (typically back arrow or menu)
 * @param onNavigationClick Navigation icon click handler
 * @param actions Additional action buttons
 * @param onProfileClick Profile avatar click handler
 * @param onSettingsClick Settings action click handler
 * @param showProfile Whether to show profile action (default true for Main screens)
 * @param showSettings Whether to show settings action (default true for Main screens)
 * @param scrollState Required for ScrollAware screen type
 * @param scrollConfig Configuration for scroll-aware behavior
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QodeAppTopAppBar(
    title: String,
    screenType: TopAppBarScreenType,
    modifier: Modifier = Modifier,
    user: User? = null,
    navigationIcon: ImageVector? = null,
    onNavigationClick: (() -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    onProfileClick: ((User?) -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
    showProfile: Boolean = true,
    showSettings: Boolean = true,
    scrollState: ScrollState? = null,
    scrollConfig: ScrollAwareConfig = ScrollAwareConfig()
) {
    when (screenType) {
        TopAppBarScreenType.None -> {
            // No top bar rendered
        }

        TopAppBarScreenType.Main -> {
            MainTopAppBar(
                title = title,
                user = user,
                modifier = modifier,
                navigationIcon = navigationIcon,
                onNavigationClick = onNavigationClick,
                onProfileClick = onProfileClick,
                onSettingsClick = onSettingsClick,
                showProfile = showProfile,
                showSettings = showSettings,
            )
        }

        TopAppBarScreenType.Nested -> {
            QodeTopAppBar(
                title = title,
                navigationIcon = navigationIcon,
                onNavigationClick = onNavigationClick,
                customActions = actions,
                variant = QodeTopAppBarVariant.CenterAligned,
                navigationIconTint = MaterialTheme.colorScheme.onSurface,
                titleColor = MaterialTheme.colorScheme.onSurface,
                actionIconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                backgroundColor = MaterialTheme.colorScheme.surface,
                modifier = modifier,
            )
        }

        TopAppBarScreenType.ScrollAware -> {
            requireNotNull(scrollState) { "ScrollState is required for ScrollAware screen type" }

            val scrollBehaviorState = rememberScrollBehaviorState(
                scrollState = scrollState,
                config = scrollConfig,
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
                QodeTopAppBar(
                    title = title,
                    navigationIcon = navigationIcon,
                    onNavigationClick = onNavigationClick,
                    variant = QodeTopAppBarVariant.Transparent,
                    statusBarPadding = true,
                    navigationIconTint = MaterialTheme.colorScheme.onSurface,
                    titleColor = MaterialTheme.colorScheme.onSurface,
                    actionIconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                    customActions = {
                        // Custom actions from parameter
                        actions?.invoke(this)

                        // Add profile and settings for scroll-aware (Profile screen)
                        if (showProfile && onProfileClick != null) {
                            IconButton(onClick = { onProfileClick(user) }) {
                                ProfileAvatar(
                                    user = user,
                                    size = SizeTokens.Icon.sizeXLarge,
                                    contentDescription = "Profile",
                                )
                            }
                        }

                        if (showSettings && onSettingsClick != null) {
                            IconButton(onClick = onSettingsClick) {
                                Icon(
                                    imageVector = QodeNavigationIcons.Settings,
                                    contentDescription = "Settings",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    },
                )
            }
        }
    }
}

// MARK: - Scroll Behavior Implementation

/**
 * Scroll direction state for better state management
 */
private sealed interface ScrollDirection {
    data object Idle : ScrollDirection
    data class Down(val accumulated: Int) : ScrollDirection
    data class Up(val delta: Int) : ScrollDirection
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
