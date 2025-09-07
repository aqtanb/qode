package com.qodein.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

// MARK: - Domain Models

/**
 * Enterprise-level auto-hiding configuration with comprehensive customization
 */
data class AutoHideConfig(
    val sensitivity: HidingSensitivity = HidingSensitivity.NORMAL,
    val behavior: HidingBehavior = HidingBehavior.ACCUMULATIVE,
    val thresholds: HidingThresholds = HidingThresholds.default(),
    val animation: HidingAnimation = HidingAnimation.default()
) {
    companion object {
        val Default = AutoHideConfig()
        val Sensitive = AutoHideConfig(sensitivity = HidingSensitivity.HIGH)
        val Relaxed = AutoHideConfig(sensitivity = HidingSensitivity.LOW)
        val Immediate = AutoHideConfig(behavior = HidingBehavior.IMMEDIATE)
    }
}

/**
 * Sensitivity levels for hiding behavior
 */
enum class HidingSensitivity(val multiplier: Float) {
    LOW(0.5f),
    NORMAL(1.0f),
    HIGH(1.5f)
}

/**
 * Different hiding behavior strategies
 */
enum class HidingBehavior {
    /** Accumulate scroll distance before hiding */
    ACCUMULATIVE,

    /** Hide immediately on scroll down */
    IMMEDIATE,

    /** Hide only on fast scroll */
    VELOCITY_BASED
}

/**
 * Configurable thresholds for hiding logic
 */
data class HidingThresholds(val alwaysVisible: Dp, val hideAccumulation: Dp, val showOnScrollUp: Dp, val velocityThreshold: Dp) {
    companion object {
        fun default() =
            HidingThresholds(
                alwaysVisible = 50.dp,
                hideAccumulation = 150.dp,
                showOnScrollUp = 8.dp,
                velocityThreshold = 20.dp,
            )
    }
}

/**
 * Animation configuration for hiding transitions
 */
data class HidingAnimation(val hideStiffness: Float, val showStiffness: Float, val hideDamping: Float, val showDamping: Float) {
    companion object {
        fun default() =
            HidingAnimation(
                hideStiffness = Spring.StiffnessMedium,
                showStiffness = Spring.StiffnessMediumLow,
                hideDamping = Spring.DampingRatioNoBouncy,
                showDamping = Spring.DampingRatioMediumBouncy,
            )
    }
}

/**
 * Direction of auto-hide animation
 */
enum class AutoHideDirection {
    UP,
    DOWN
}

// MARK: - State Management

/**
 * Comprehensive scroll state representation
 */
sealed interface ScrollInfo {
    val position: Int
    val delta: Int
    val timestamp: Long

    data class Scrolling(override val position: Int, override val delta: Int, override val timestamp: Long, val velocity: Float) :
        ScrollInfo

    data class Idle(override val position: Int, override val timestamp: Long) : ScrollInfo {
        override val delta: Int = 0
    }

    data class AtTop(override val timestamp: Long) : ScrollInfo {
        override val position: Int = 0
        override val delta: Int = 0
    }
}

/**
 * Visibility state with transition information
 */
sealed interface VisibilityState {
    val isVisible: Boolean
    val reason: String
    val timestamp: Long

    data class Visible(override val reason: String, override val timestamp: Long = System.currentTimeMillis()) : VisibilityState {
        override val isVisible: Boolean = true
    }

    data class Hidden(override val reason: String, override val timestamp: Long = System.currentTimeMillis()) : VisibilityState {
        override val isVisible: Boolean = false
    }

    data class Transitioning(
        val targetVisible: Boolean,
        override val reason: String,
        override val timestamp: Long = System.currentTimeMillis()
    ) : VisibilityState {
        override val isVisible: Boolean = targetVisible
    }
}

// MARK: - Business Logic

/**
 * Pure business logic for auto-hiding decisions (testable)
 */
class AutoHidingLogic(private val config: AutoHideConfig) {

    private var accumulatedDistance = 0
    private var lastScrollPosition = 0
    private var lastUpdateTime = 0L

    fun processScrollUpdate(scrollInfo: ScrollInfo): VisibilityDecision {
        val currentTime = scrollInfo.timestamp

        return when (scrollInfo) {
            is ScrollInfo.AtTop -> {
                reset()
                VisibilityDecision.Show("At top of content")
            }

            is ScrollInfo.Idle -> {
                VisibilityDecision.Maintain("Content not scrolling")
            }

            is ScrollInfo.Scrolling -> {
                processScrolling(scrollInfo, currentTime)
            }
        }
    }

    private fun processScrolling(
        scroll: ScrollInfo.Scrolling,
        currentTime: Long
    ): VisibilityDecision {
        // Always show when near top
        if (scroll.position <= config.thresholds.alwaysVisible.value.toInt()) {
            reset()
            return VisibilityDecision.Show("Near top threshold")
        }

        return when (config.behavior) {
            HidingBehavior.IMMEDIATE -> processImmediateBehavior(scroll)
            HidingBehavior.ACCUMULATIVE -> processAccumulativeBehavior(scroll)
            HidingBehavior.VELOCITY_BASED -> processVelocityBehavior(scroll)
        }
    }

    private fun processImmediateBehavior(scroll: ScrollInfo.Scrolling): VisibilityDecision =
        when {
            scroll.delta > 0 -> VisibilityDecision.Hide("Scrolling down (immediate)")
            scroll.delta < -config.thresholds.showOnScrollUp.value.toInt() -> {
                reset()
                VisibilityDecision.Show("Scrolling up past threshold")
            }
            else -> VisibilityDecision.Maintain("Minor scroll movement")
        }

    private fun processAccumulativeBehavior(scroll: ScrollInfo.Scrolling): VisibilityDecision =
        when {
            scroll.delta > 0 -> {
                accumulatedDistance += scroll.delta
                val threshold = (config.thresholds.hideAccumulation.value * config.sensitivity.multiplier).toInt()

                if (accumulatedDistance >= threshold) {
                    VisibilityDecision.Hide("Accumulated scroll distance exceeded ($accumulatedDistance >= $threshold)")
                } else {
                    VisibilityDecision.Maintain("Accumulating scroll distance ($accumulatedDistance/$threshold)")
                }
            }

            scroll.delta < -config.thresholds.showOnScrollUp.value.toInt() -> {
                reset()
                VisibilityDecision.Show("Strong upward scroll")
            }

            else -> VisibilityDecision.Maintain("Minor scroll movement")
        }

    private fun processVelocityBehavior(scroll: ScrollInfo.Scrolling): VisibilityDecision {
        val velocityThreshold = config.thresholds.velocityThreshold.value

        return when {
            scroll.velocity > velocityThreshold -> VisibilityDecision.Hide("Fast downward scroll (velocity: ${scroll.velocity})")
            scroll.velocity < -velocityThreshold -> {
                reset()
                VisibilityDecision.Show("Fast upward scroll (velocity: ${scroll.velocity})")
            }
            else -> VisibilityDecision.Maintain("Slow scroll (velocity: ${scroll.velocity})")
        }
    }

    private fun reset() {
        accumulatedDistance = 0
    }

    fun getDebugInfo(): AutoHidingDebugInfo =
        AutoHidingDebugInfo(
            accumulatedDistance = accumulatedDistance,
            lastScrollPosition = lastScrollPosition,
            lastUpdateTime = lastUpdateTime,
            config = config,
        )
}

/**
 * Decision output from hiding logic
 */
sealed interface VisibilityDecision {
    val reason: String

    data class Show(override val reason: String) : VisibilityDecision
    data class Hide(override val reason: String) : VisibilityDecision
    data class Maintain(override val reason: String) : VisibilityDecision
}

/**
 * Debug information for troubleshooting
 */
data class AutoHidingDebugInfo(
    val accumulatedDistance: Int,
    val lastScrollPosition: Int,
    val lastUpdateTime: Long,
    val config: AutoHideConfig
)

// MARK: - Compose State Holder

/**
 * Enterprise-level auto-hiding state holder
 */
@Stable
class AutoHidingState(private val config: AutoHideConfig) {
    private val logic = AutoHidingLogic(config)

    var visibilityState by mutableStateOf<VisibilityState>(
        VisibilityState.Visible("Initial state"),
    )
        private set

    var debugInfo by mutableStateOf(logic.getDebugInfo())
        private set

    val isVisible: Boolean
        get() = visibilityState.isVisible

    fun updateScroll(scrollInfo: ScrollInfo) {
        val decision = logic.processScrollUpdate(scrollInfo)
        val newState = when (decision) {
            is VisibilityDecision.Show -> {
                if (!isVisible) {
                    VisibilityState.Visible(decision.reason)
                } else {
                    visibilityState
                }
            }

            is VisibilityDecision.Hide -> {
                if (isVisible) {
                    VisibilityState.Hidden(decision.reason)
                } else {
                    visibilityState
                }
            }

            is VisibilityDecision.Maintain -> visibilityState
        }

        if (newState != visibilityState) {
            visibilityState = newState
        }

        debugInfo = logic.getDebugInfo()
    }

    fun reset(reason: String = "Manual reset") {
        visibilityState = VisibilityState.Visible(reason)
        debugInfo = logic.getDebugInfo()
    }
}

// MARK: - Scroll Value Extractors

/**
 * Enterprise-level scroll value extraction strategies
 */
interface ScrollValueExtractor<T : ScrollableState> {
    fun extractScrollInfo(scrollState: T): ScrollInfo
}

class LazyListScrollExtractor : ScrollValueExtractor<LazyListState> {
    private var lastPosition = 0
    private var lastTimestamp = 0L
    private var isInitialized = false

    override fun extractScrollInfo(scrollState: LazyListState): ScrollInfo {
        val currentTime = System.currentTimeMillis()
        val layoutInfo = scrollState.layoutInfo

        // Check if at top (no items or first item at position 0)
        if (layoutInfo.totalItemsCount == 0 ||
            (scrollState.firstVisibleItemIndex == 0 && scrollState.firstVisibleItemScrollOffset == 0)
        ) {
            reset(currentTime)
            return ScrollInfo.AtTop(currentTime)
        }

        // Robust position calculation using index-based approach
        // This works reliably with variable item heights
        val currentPosition = scrollState.firstVisibleItemIndex * 1000 + scrollState.firstVisibleItemScrollOffset

        // Initialize on first valid scroll position to avoid wrong delta calculations
        if (!isInitialized) {
            lastPosition = currentPosition
            lastTimestamp = currentTime
            isInitialized = true
            return ScrollInfo.Idle(currentPosition, currentTime)
        }

        val delta = currentPosition - lastPosition
        val timeDelta = currentTime - lastTimestamp
        val velocity = if (timeDelta > 0) delta.toFloat() / timeDelta else 0f

        val result = if (delta != 0) {
            ScrollInfo.Scrolling(currentPosition, delta, currentTime, velocity)
        } else {
            ScrollInfo.Idle(currentPosition, currentTime)
        }

        lastPosition = currentPosition
        lastTimestamp = currentTime

        return result
    }

    private fun reset(currentTime: Long) {
        lastPosition = 0
        lastTimestamp = currentTime
        isInitialized = false
    }
}

class ScrollStateExtractor : ScrollValueExtractor<ScrollState> {
    private var lastPosition = 0
    private var lastTimestamp = 0L
    private var isInitialized = false

    override fun extractScrollInfo(scrollState: ScrollState): ScrollInfo {
        val currentTime = System.currentTimeMillis()
        val currentPosition = scrollState.value

        if (currentPosition <= 0) {
            reset(currentTime)
            return ScrollInfo.AtTop(currentTime)
        }

        // Initialize on first valid scroll position to avoid wrong delta calculations
        if (!isInitialized) {
            lastPosition = currentPosition
            lastTimestamp = currentTime
            isInitialized = true
            return ScrollInfo.Idle(currentPosition, currentTime)
        }

        val delta = currentPosition - lastPosition
        val timeDelta = currentTime - lastTimestamp
        val velocity = if (timeDelta > 0) delta.toFloat() / timeDelta else 0f

        val result = if (delta != 0) {
            ScrollInfo.Scrolling(currentPosition, delta, currentTime, velocity)
        } else {
            ScrollInfo.Idle(currentPosition, currentTime)
        }

        lastPosition = currentPosition
        lastTimestamp = currentTime

        return result
    }

    private fun reset(currentTime: Long) {
        lastPosition = 0
        lastTimestamp = currentTime
        isInitialized = false
    }
}

// MARK: - Compose Integration

/**
 * Master centralized function - all auto-hiding logic flows through here
 * This is the single source of truth for remember and LaunchedEffect logic
 */
@Composable
fun rememberAutoHidingState(
    scrollInfoFlow: Flow<ScrollInfo>,
    config: AutoHideConfig,
    key: Any
): AutoHidingState {
    val state = remember(key, config) {
        AutoHidingState(config)
    }

    LaunchedEffect(key, config) {
        // This is the ideal place for the reset side effect.
        // It runs whenever the 'key' changes.
        state.reset("LaunchedEffect started for new key")

        scrollInfoFlow
            .distinctUntilChanged()
            .collect { scrollInfo ->
                state.updateScroll(scrollInfo)
            }
    }

    return state
}

/**
 * Enterprise remember functions with proper lifecycle management
 */
@Composable
fun rememberAutoHidingState(
    scrollableState: ScrollableState?,
    config: AutoHideConfig = AutoHideConfig.Default
): AutoHidingState? =
    when (scrollableState) {
        is LazyListState -> rememberAutoHidingState(scrollableState, config)
        is ScrollState -> rememberAutoHidingState(scrollableState, config)
        is LazyGridState -> rememberAutoHidingState(scrollableState, config)
        null -> null
        else -> null
    }

@Composable
fun rememberAutoHidingState(
    lazyListState: LazyListState,
    config: AutoHideConfig = AutoHideConfig.Default
): AutoHidingState {
    val extractor = remember(lazyListState) { LazyListScrollExtractor() }

    return rememberAutoHidingState(
        scrollInfoFlow = snapshotFlow { extractor.extractScrollInfo(lazyListState) },
        config = config,
        key = lazyListState,
    )
}

@Composable
fun rememberAutoHidingState(
    scrollState: ScrollState,
    config: AutoHideConfig = AutoHideConfig.Default
): AutoHidingState {
    val extractor = remember(scrollState) { ScrollStateExtractor() }

    return rememberAutoHidingState(
        scrollInfoFlow = snapshotFlow { extractor.extractScrollInfo(scrollState) },
        config = config,
        key = scrollState,
    )
}

@Composable
fun rememberAutoHidingState(
    lazyGridState: LazyGridState,
    config: AutoHideConfig = AutoHideConfig.Default
): AutoHidingState {
    // Implement LazyGridState extractor similar to LazyListState
    return rememberAutoHidingState(
        scrollableState = lazyGridState as ScrollableState,
        config = config,
    ) ?: AutoHidingState(config)
}

// MARK: - UI Components

/**
 * Enterprise auto-hiding wrapper with comprehensive animation support
 */
@Composable
fun AutoHidingContent(
    state: AutoHidingState,
    modifier: Modifier = Modifier,
    direction: AutoHideDirection = AutoHideDirection.DOWN,
    content: @Composable () -> Unit
) {
    val config = remember { state.debugInfo.config }
    val animation = config.animation

    val (enterAnimation, exitAnimation) = when (direction) {
        AutoHideDirection.DOWN -> {
            slideInVertically(
                initialOffsetY = { -it },
                animationSpec = spring(
                    dampingRatio = animation.showDamping,
                    stiffness = animation.showStiffness,
                ),
            ) to slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = spring(
                    dampingRatio = animation.hideDamping,
                    stiffness = animation.hideStiffness,
                ),
            )
        }
        AutoHideDirection.UP -> {
            slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = animation.showDamping,
                    stiffness = animation.showStiffness,
                ),
            ) to slideOutVertically(
                targetOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = animation.hideDamping,
                    stiffness = animation.hideStiffness,
                ),
            )
        }
    }

    AnimatedVisibility(
        visible = state.isVisible,
        enter = enterAnimation,
        exit = exitAnimation,
        modifier = modifier,
    ) {
        content()
    }
}
