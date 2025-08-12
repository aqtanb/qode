package com.qodein.core.designsystem.theme

import androidx.compose.animation.core.AnimationSpec
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
    // Screen-level spacing
    val screenPadding = 16.dp
    val sectionSpacing = 24.dp

    @Immutable
    object Button {
        val horizontalPaddingSmall = 12.dp
        val horizontalPadding = 16.dp
        val horizontalPaddingLarge = 24.dp
        val iconSpacing = 8.dp
    }

    @Immutable
    object Card {
        val padding = 16.dp
        val margin = 8.dp
        val headerPadding = 16.dp
    }

    @Immutable
    object TextField {
        val horizontalPadding = 16.dp
        val verticalPadding = 12.dp
    }

    @Immutable
    object List {
        val itemSpacing = 8.dp
        val sectionSpacing = 16.dp
    }

    @Immutable
    object Chip {
        val horizontalPadding = 12.dp
        val spacing = 8.dp
    }

    // Generic spacing (use sparingly)
    val xs = 4.dp // Removed xxs as discussed
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
    val xxxl = 64.dp
}

/**
 * Component size tokens with semantic grouping
 */
@Immutable
object SizeTokens {
    // Shared sizing
    val minTouchTarget = 48.dp
    val maxContentWidth = 280.dp

    @Immutable
    object Button {
        val heightSmall = 32.dp
        val heightMedium = 40.dp
        val heightLarge = 48.dp
        val widthMin = 64.dp
        val widthMax = 280.dp
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
        val sizeXXLarge = 48.dp
    }
}

/**
 * Shape tokens for corners and borders
 */
@Immutable
object ShapeTokens {
    @Immutable
    object Corner {
        val none = 0.dp
        val small = 4.dp
        val medium = 8.dp
        val large = 12.dp
        val extraLarge = 16.dp
        val xxl = 28.dp
        val full = 999.dp
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
            easing = androidx.compose.animation.core.FastOutSlowInEasing,
        )

        val medium: AnimationSpec<Float> = tween(
            durationMillis = MotionTokens.Duration.MEDIUM,
            easing = androidx.compose.animation.core.FastOutSlowInEasing,
        )

        val slow: AnimationSpec<Float> = tween(
            durationMillis = MotionTokens.Duration.SLOW,
            easing = androidx.compose.animation.core.FastOutSlowInEasing,
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

// =============================================================================
// BACKWARD COMPATIBILITY - DEPRECATED
// These will be removed in next major version
// =============================================================================

@Deprecated(
    "Use SpacingTokens with semantic names instead",
    ReplaceWith("SpacingTokens"),
)
object QodeSpacing {
    @Deprecated("Use SpacingTokens.xs", ReplaceWith("SpacingTokens.xs"))
    val xs = SpacingTokens.xs

    @Deprecated("Use SpacingTokens.sm", ReplaceWith("SpacingTokens.sm"))
    val sm = SpacingTokens.sm

    @Deprecated("Use SpacingTokens.md", ReplaceWith("SpacingTokens.md"))
    val md = SpacingTokens.md

    @Deprecated("Use SpacingTokens.lg", ReplaceWith("SpacingTokens.lg"))
    val lg = SpacingTokens.lg

    @Deprecated("Use SpacingTokens.xl", ReplaceWith("SpacingTokens.xl"))
    val xl = SpacingTokens.xl

    @Deprecated("Use SpacingTokens.xxl", ReplaceWith("SpacingTokens.xxl"))
    val xxl = SpacingTokens.xxl

    @Deprecated("Use SpacingTokens.xxxl", ReplaceWith("SpacingTokens.xxxl"))
    val xxxl = SpacingTokens.xxxl
}

@Deprecated(
    "Use SizeTokens with semantic names instead",
    ReplaceWith("SizeTokens"),
)
object QodeSize {
    @Deprecated("Use SizeTokens.Button.heightSmall", ReplaceWith("SizeTokens.Button.heightSmall"))
    val buttonHeightSmall = SizeTokens.Button.heightSmall

    @Deprecated("Use SizeTokens.Button.heightMedium", ReplaceWith("SizeTokens.Button.heightMedium"))
    val buttonHeightMedium = SizeTokens.Button.heightMedium

    @Deprecated("Use SizeTokens.Button.heightLarge", ReplaceWith("SizeTokens.Button.heightLarge"))
    val buttonHeightLarge = SizeTokens.Button.heightLarge

    @Deprecated("Use SizeTokens.IconButton.sizeSmall", ReplaceWith("SizeTokens.IconButton.sizeSmall"))
    val iconButtonSmall = SizeTokens.IconButton.sizeSmall

    @Deprecated("Use SizeTokens.IconButton.sizeMedium", ReplaceWith("SizeTokens.IconButton.sizeMedium"))
    val iconButtonMedium = SizeTokens.IconButton.sizeMedium

    @Deprecated("Use SizeTokens.IconButton.sizeLarge", ReplaceWith("SizeTokens.IconButton.sizeLarge"))
    val iconButtonLarge = SizeTokens.IconButton.sizeLarge

    @Deprecated("Use SizeTokens.TextField.height", ReplaceWith("SizeTokens.TextField.height"))
    val textFieldHeight = SizeTokens.TextField.height

    @Deprecated("Use SizeTokens.TextField.heightSmall", ReplaceWith("SizeTokens.TextField.heightSmall"))
    val textFieldHeightSmall = SizeTokens.TextField.heightSmall

    @Deprecated("Use SizeTokens.Chip.height", ReplaceWith("SizeTokens.Chip.height"))
    val chipHeight = SizeTokens.Chip.height

    @Deprecated("Use SizeTokens.Chip.heightSmall", ReplaceWith("SizeTokens.Chip.heightSmall"))
    val chipHeightSmall = SizeTokens.Chip.heightSmall

    @Deprecated("Use SizeTokens.Icon.sizeSmall", ReplaceWith("SizeTokens.Icon.sizeSmall"))
    val iconSmall = SizeTokens.Icon.sizeSmall

    @Deprecated("Use SizeTokens.Icon.sizeMedium", ReplaceWith("SizeTokens.Icon.sizeMedium"))
    val iconMedium = SizeTokens.Icon.sizeMedium

    @Deprecated("Use SizeTokens.Icon.sizeLarge", ReplaceWith("SizeTokens.Icon.sizeLarge"))
    val iconLarge = SizeTokens.Icon.sizeLarge

    @Deprecated("Use SizeTokens.Card.minHeight", ReplaceWith("SizeTokens.Card.minHeight"))
    val cardMinHeight = SizeTokens.Card.minHeight

    @Deprecated("Use SizeTokens.Button.widthMin", ReplaceWith("SizeTokens.Button.widthMin"))
    val minButtonWidth = SizeTokens.Button.widthMin

    @Deprecated("Use SizeTokens.Button.widthMax", ReplaceWith("SizeTokens.Button.widthMax"))
    val maxButtonWidth = SizeTokens.Button.widthMax
}

@Deprecated(
    "Use ShapeTokens instead",
    ReplaceWith("ShapeTokens"),
)
object QodeCorners {
    @Deprecated("Use ShapeTokens.Corner.none", ReplaceWith("ShapeTokens.Corner.none"))
    val none = ShapeTokens.Corner.none

    @Deprecated("Use ShapeTokens.Corner.small", ReplaceWith("ShapeTokens.Corner.small"))
    val xs = ShapeTokens.Corner.small

    @Deprecated("Use ShapeTokens.Corner.small", ReplaceWith("ShapeTokens.Corner.small"))
    val sm = ShapeTokens.Corner.small

    @Deprecated("Use ShapeTokens.Corner.medium", ReplaceWith("ShapeTokens.Corner.medium"))
    val md = ShapeTokens.Corner.medium

    @Deprecated("Use ShapeTokens.Corner.large", ReplaceWith("ShapeTokens.Corner.large"))
    val lg = ShapeTokens.Corner.large

    @Deprecated("Use ShapeTokens.Corner.extraLarge", ReplaceWith("ShapeTokens.Corner.extraLarge"))
    val xl = ShapeTokens.Corner.extraLarge

    @Deprecated("Use ShapeTokens.Corner.xxl", ReplaceWith("ShapeTokens.Corner.xxl"))
    val xxl = ShapeTokens.Corner.xxl

    @Deprecated("Use ShapeTokens.Corner.FULL", ReplaceWith("ShapeTokens.Corner.FULL"))
    val full = ShapeTokens.Corner.full
}

@Deprecated(
    "Use ElevationTokens instead",
    ReplaceWith("ElevationTokens"),
)
object QodeElevation {
    @Deprecated("Use ElevationTokens.none", ReplaceWith("ElevationTokens.none"))
    val none = ElevationTokens.none

    @Deprecated("Use ElevationTokens.small", ReplaceWith("ElevationTokens.small"))
    val xs = ElevationTokens.small

    @Deprecated("Use ElevationTokens.small", ReplaceWith("ElevationTokens.small"))
    val sm = ElevationTokens.small

    @Deprecated("Use ElevationTokens.medium", ReplaceWith("ElevationTokens.medium"))
    val md = ElevationTokens.medium

    @Deprecated("Use ElevationTokens.large", ReplaceWith("ElevationTokens.large"))
    val lg = ElevationTokens.large

    @Deprecated("Use ElevationTokens.extraLarge", ReplaceWith("ElevationTokens.extraLarge"))
    val xl = ElevationTokens.extraLarge
}

@Deprecated(
    "Use ShapeTokens.Border instead",
    ReplaceWith("ShapeTokens.Border"),
)
object QodeBorder {
    @Deprecated("Use ShapeTokens.Border.thin", ReplaceWith("ShapeTokens.Border.thin"))
    val thin = ShapeTokens.Border.thin

    @Deprecated("Use ShapeTokens.Border.medium", ReplaceWith("ShapeTokens.Border.medium"))
    val medium = ShapeTokens.Border.medium

    @Deprecated("Use ShapeTokens.Border.thick", ReplaceWith("ShapeTokens.Border.thick"))
    val thick = ShapeTokens.Border.thick
}

@Deprecated(
    "Use MotionTokens.Duration instead",
    ReplaceWith("MotionTokens.Duration"),
)
object QodeAnimation {
    @Deprecated("Use MotionTokens.Duration.FAST", ReplaceWith("MotionTokens.Duration.FAST"))
    const val FAST = MotionTokens.Duration.FAST

    @Deprecated("Use MotionTokens.Duration.MEDIUM", ReplaceWith("MotionTokens.Duration.MEDIUM"))
    const val MEDIUM = MotionTokens.Duration.MEDIUM

    @Deprecated("Use MotionTokens.Duration.SLOW", ReplaceWith("MotionTokens.Duration.SLOW"))
    const val SLOW = MotionTokens.Duration.SLOW
}
