package com.qodein.core.ui.animation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.delay

/**
 * Predefined animation configurations for consistent motion design
 */
object QodeAnimationSpecs {
    val slideSpring = spring<IntOffset>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow,
    )

    val fadeInFast = tween<Float>(600)
    val fadeInMedium = tween<Float>(800, 200)
    val fadeInSlow = tween<Float>(1000, 400)
    val fadeInExtraSlow = tween<Float>(1200, 600)
}

/**
 * Direction for slide-in animations
 */
enum class SlideDirection {
    Up,
    Down,
    Left,
    Right
}

/**
 * Animated visibility wrapper with staggered entry animation
 * Perfect for profile sections that appear in sequence
 *
 * @param visible Whether the content should be visible
 * @param delayMs Delay before animation starts (for staggering)
 * @param slideDirection Direction of the slide animation
 * @param fadeSpec Animation spec for fade transition
 * @param content The composable content to animate
 */
@Composable
fun QodeStaggeredEntry(
    visible: Boolean,
    delayMs: Long = 0L,
    slideDirection: SlideDirection = SlideDirection.Up,
    fadeSpec: FiniteAnimationSpec<Float> = QodeAnimationSpecs.fadeInFast,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isVisible by remember(visible) { mutableStateOf(false) }

    LaunchedEffect(visible) {
        if (visible) {
            delay(delayMs)
            isVisible = true
        } else {
            isVisible = false
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { offset ->
                when (slideDirection) {
                    SlideDirection.Up -> offset / 2
                    SlideDirection.Down -> -offset / 2
                    else -> 0
                }
            },
            animationSpec = QodeAnimationSpecs.slideSpring,
        ) + fadeIn(animationSpec = fadeSpec),
        modifier = modifier,
    ) {
        content()
    }
}

/**
 * Profile header animation wrapper
 * Slides from top with medium bounce
 */
@Composable
fun QodeAnimatedProfileHeader(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    QodeStaggeredEntry(
        visible = visible,
        delayMs = 100L,
        slideDirection = SlideDirection.Down,
        fadeSpec = QodeAnimationSpecs.fadeInFast,
        modifier = modifier,
        content = content,
    )
}

/**
 * Stats section animation wrapper
 * Slides from bottom with slight delay
 */
@Composable
fun QodeAnimatedStatsSection(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    QodeStaggeredEntry(
        visible = visible,
        delayMs = 200L,
        slideDirection = SlideDirection.Up,
        fadeSpec = QodeAnimationSpecs.fadeInMedium,
        modifier = modifier,
        content = content,
    )
}

/**
 * Activity feed animation wrapper
 * Slides from bottom with medium delay
 */
@Composable
fun QodeAnimatedActivityFeed(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    QodeStaggeredEntry(
        visible = visible,
        delayMs = 400L,
        slideDirection = SlideDirection.Up,
        fadeSpec = QodeAnimationSpecs.fadeInSlow,
        modifier = modifier,
        content = content,
    )
}

/**
 * Action button animation wrapper
 * Simple fade in with long delay
 */
@Composable
fun QodeAnimatedActionButton(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isVisible by remember(visible) { mutableStateOf(false) }

    LaunchedEffect(visible) {
        if (visible) {
            delay(600L)
            isVisible = true
        } else {
            isVisible = false
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = QodeAnimationSpecs.fadeInExtraSlow),
        modifier = modifier,
    ) {
        content()
    }
}
