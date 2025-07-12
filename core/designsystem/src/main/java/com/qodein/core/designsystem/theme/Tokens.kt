package com.qodein.core.designsystem.theme

import androidx.compose.ui.unit.dp

/**
 * Design tokens for spacing following 8dp grid system
 */
object QodeSpacing {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
    val xxxl = 64.dp
}

/**
 * Design tokens for component sizing
 */
object QodeSize {
    // Button heights
    val buttonHeightSmall = 32.dp
    val buttonHeightMedium = 40.dp
    val buttonHeightLarge = 48.dp

    // Icon button sizes
    val iconButtonSmall = 32.dp
    val iconButtonMedium = 40.dp
    val iconButtonLarge = 48.dp

    // TextField heights
    val textFieldHeight = 56.dp
    val textFieldHeightSmall = 40.dp

    // Chip heights
    val chipHeight = 32.dp
    val chipHeightSmall = 24.dp

    // Icon sizes
    val iconSmall = 16.dp
    val iconMedium = 24.dp
    val iconLarge = 32.dp

    // Card min heights
    val cardMinHeight = 72.dp

    // Common component widths
    val minButtonWidth = 64.dp
    val maxButtonWidth = 280.dp
}

/**
 * Design tokens for corner radius
 */
object QodeCorners {
    val none = 0.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 28.dp
    val full = 50 // Percent for fully rounded
}

/**
 * Design tokens for elevation
 */
object QodeElevation {
    val none = 0.dp
    val xs = 1.dp
    val sm = 2.dp
    val md = 4.dp
    val lg = 8.dp
    val xl = 12.dp
}

/**
 * Design tokens for border widths
 */
object QodeBorder {
    val thin = 1.dp
    val medium = 2.dp
    val thick = 3.dp
}

/**
 * Animation durations in milliseconds
 */
object QodeAnimation {
    const val FAST = 150
    const val MEDIUM = 300
    const val SLOW = 500
}
