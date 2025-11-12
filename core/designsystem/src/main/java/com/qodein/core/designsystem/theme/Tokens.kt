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
    /** 0dp - No spacing: dividers, flush layouts */
    val none = 0.dp

    /** 2dp - Minimal spacing: hairline gaps, icon nudges */
    val xxxs = 2.dp

    /** 4dp - Extra small: compact chip spacing, dense lists */
    val xxs = 4.dp

    /** 8dp - Small: icon + text spacing, compact padding */
    val xs = 8.dp

    /** 12dp - Small-medium: button inner padding, comfortable gaps */
    val sm = 12.dp

    /** 16dp - Medium (default): card padding, list item spacing */
    val md = 16.dp

    /** 24dp - Large: section spacing, card margins */
    val lg = 24.dp

    /** 32dp - Extra large: screen edge padding, large components */
    val xl = 32.dp

    /** 40dp - Double extra large: modal spacing, wide gutters */
    val xxl = 40.dp

    /** 48dp - Triple extra large: page section breaks */
    val xxxl = 48.dp

    /** 64dp - Huge: hero sections, large dialog content */
    val huge = 64.dp

    /** 80dp - Massive: landing screens, wide layouts */
    val massive = 80.dp

    /** 96dp - Gigantic: banners, big empty states, space for floating controller */
    val gigantic = 96.dp
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
    object Chip {
        val height = 32.dp
        val sizeSmall = 16.dp
        val sizeMedium = 24.dp
        val sizeLarge = 32.dp
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

    @Immutable
    object Selector {
        val height = 64.dp
        val padding = SpacingTokens.lg
        val shape = ShapeTokens.Corner.full
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
        fun <T> fast(): AnimationSpec<T> =
            tween(
                durationMillis = MotionTokens.Duration.FAST,
                easing = FastOutSlowInEasing,
            )

        fun <T> medium(): AnimationSpec<T> =
            tween(
                durationMillis = MotionTokens.Duration.MEDIUM,
                easing = FastOutSlowInEasing,
            )

        fun <T> slow(): AnimationSpec<T> =
            tween(
                durationMillis = MotionTokens.Duration.SLOW,
                easing = FastOutSlowInEasing,
            )

        // Spring animations for more natural feel
        fun <T> spring(): AnimationSpec<T> = spring()
        fun <T> emphasized(): AnimationSpec<T> = spring(dampingRatio = 0.8f, stiffness = 380f)
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
