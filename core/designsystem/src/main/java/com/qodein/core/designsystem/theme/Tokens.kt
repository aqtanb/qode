package com.qodein.core.designsystem.theme

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.dp

/**
 * Foundation spacing tokens using semantic names
 * Following 8dp grid system
 */
@Immutable
object SpacingTokens {
    val none = 0.dp // dividers, tight separators
    val xxxs = 2.dp // hairline gaps, icon nudges
    val xxs = 4.dp // compact chip spacing, dense lists
    val xs = 8.dp // small gaps, icon + text, compact padding
    val sm = 12.dp // button inner padding, medium chips
    val md = 16.dp // default padding, list items
    val lg = 24.dp // section spacing, cards margin
    val xl = 32.dp // screen padding, large components
    val xxl = 40.dp // modal spacing, wide gutters
    val xxxl = 48.dp // page section breaks, toolbars
    val huge = 64.dp // hero sections, large dialog content
    val massive = 80.dp // landing screens, wide layouts
    val gigantic = 96.dp // banners, big empty states
}

/**
 * Component size tokens with semantic grouping
 */
@Immutable
object SizeTokens {

    @Immutable
    object Button {
        val heightSmall = 32.dp
        val heightMedium = 40.dp
        val heightLarge = 48.dp
    }

    @Immutable
    object IconButton {
        val sizeSmall = 32.dp
        val sizeMedium = 40.dp
        val sizeLarge = 48.dp
    }

    @Immutable
    object Icon {
        val sizeSmall = 16.dp
        val sizeMedium = 20.dp
        val sizeLarge = 24.dp
        val sizeXLarge = 32.dp
    }

    @Immutable
    object TextField {
        val height = 56.dp
        val heightSmall = 40.dp
    }

    @Immutable
    object Card {
        val minHeight = 72.dp
    }

    @Immutable
    object Chip {
        val height = 32.dp
        val heightSmall = 24.dp
    }

    @Immutable
    object AppBar {
        val height = 64.dp
        val heightSmall = 48.dp
    }

    @Immutable
    object Avatar {
        val sizeSmall = 32.dp
        val sizeMedium = 48.dp
        val sizeLarge = 72.dp
        val sizeXLarge = 120.dp
    }

    @Immutable
    object Decoration {
        val sizeXSmall = 8.dp
        val sizeSmall = 12.dp
        val sizeMedium = 16.dp
        val sizeLarge = 24.dp
        val sizeXLarge = 32.dp
    }

    @Immutable
    object Controller {
        val pillWidth = 220.dp
        val pillHeight = 80.dp
        val pillHeightWithLabels = 96.dp

        val containerHeight = 160.dp
        val containerHeightWithLabels = 180.dp
    }

    @Immutable
    object Fab {
        val size = 56.dp
        val sizeSmall = 48.dp
        val iconSize = 24.dp
    }
}

/**
 * Shape tokens for corners and borders
 */
@Immutable
object ShapeTokens {
    @Immutable
    object Corner {
        val none = 0.dp // dividers, tables, separators
        val small = 4.dp // badges, tiny chips, tooltips
        val medium = 8.dp // buttons, small cards, text fields
        val large = 12.dp // cards, bottom sheets, drawers
        val extraLarge = 16.dp // dialogs, large containers, modals
        val full = 999.dp // pills/circles: chips, tags, search bars, avatars, icons, pill buttons
    }

    @Immutable
    object Border {
        val thin = 1.dp
        val medium = 2.dp
        val thick = 3.dp
    }
}

/**
 * Motion and interaction tokens
 */
@Immutable
object MotionTokens {
    @Immutable
    object Scale {
        const val PRESSED = 0.95f
        const val ICON_PRESSED = 0.90f
        const val HOVER = 1.05f
    }

    @Immutable
    object Duration {
        const val FAST = 150
        const val MEDIUM = 300
        const val SLOW = 500
        const val EXTRA_SLOW = 1000
    }

    @Immutable
    object Easing {
        // Spring animations for natural feel
        val standard = spring<Float>()
        val emphasized = spring<Float>(dampingRatio = 0.8f, stiffness = 380f)
        val decelerated = spring<Float>(dampingRatio = 1f, stiffness = 400f)
    }
}

/**
 * Opacity tokens for various states
 */
@Immutable
object OpacityTokens {
    // Disabled states
    const val DISABLED = 0.38f
    const val DISABLED_CONTAINER = 0.12f

    // Interactive states
    const val HOVER = 0.08f
    const val PRESSED = 0.12f
    const val FOCUS = 0.12f
    const val SELECTED = 0.16f

    // Overlay states
    const val OVERLAY_LIGHT = 0.16f
    const val OVERLAY_DARK = 0.32f
    const val SCRIM = 0.32f
    const val DIVIDER = 0.12f
}

/**
 * Pre-configured animation specifications
 */
@Immutable
object AnimationTokens {
    @Immutable
    object Spec {
        val fast: AnimationSpec<Float> = tween(
            durationMillis = MotionTokens.Duration.FAST,
            easing = FastOutSlowInEasing,
        )

        val medium: AnimationSpec<Float> = tween(
            durationMillis = MotionTokens.Duration.MEDIUM,
            easing = FastOutSlowInEasing,
        )

        val slow: AnimationSpec<Float> = tween(
            durationMillis = MotionTokens.Duration.SLOW,
            easing = FastOutSlowInEasing,
        )

        // Spring animations for more natural feel
        val spring: AnimationSpec<Float> = MotionTokens.Easing.standard
        val emphasized: AnimationSpec<Float> = MotionTokens.Easing.emphasized
    }
}

/**
 * Elevation tokens for Material Design
 */
@Immutable
object ElevationTokens {
    val none = 0.dp
    val small = 1.dp
    val medium = 3.dp
    val large = 6.dp
    val extraLarge = 12.dp
}
