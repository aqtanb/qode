package com.qodein.core.designsystem

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    name = "Phone",
    device = "spec:width=360dp,height=640dp,dpi=480",
)
@Preview(
    name = "Phone Landscape",
    device = "spec:width=640dp,height=360dp,dpi=480",
)
@Preview(
    name = "Foldable",
    device = "spec:width=673dp,height=841dp,dpi=480",
)
@Preview(
    name = "Tablet",
    device = "spec:width=1280dp,height=800dp,dpi=480",
)
annotation class DevicePreviews

/**
 * Multi-preview annotation for different theme configurations.
 * Shows both light and dark themes with dynamic colors.
 */
@Preview(
    name = "Light Theme",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
    backgroundColor = 0xFFFFF6ED,
)
@Preview(
    name = "Dark Theme",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    backgroundColor = 0xFF1A110F,
)
annotation class ThemePreviews

/**
 * Multi-preview annotation for different font scales.
 * Essential for accessibility testing.
 */
@Preview(
    name = "Default Font",
    fontScale = 1.0f,
)
@Preview(
    name = "Large Font",
    fontScale = 1.5f,
)
@Preview(
    name = "Extra Large Font",
    fontScale = 2.0f,
)
annotation class FontScalePreviews

/**
 * Preview annotation specifically for mobile devices with common configurations.
 */
@Preview(
    name = "Phone Portrait",
    device = "spec:width=360dp,height=640dp,dpi=480",
    showSystemUi = true,
)
@Preview(
    name = "Phone Landscape",
    device = "spec:width=640dp,height=360dp,dpi=480",
    showSystemUi = true,
)
annotation class MobilePreviews

/**
 * Preview annotation for tablet-specific layouts.
 */
@Preview(
    name = "Tablet Portrait",
    device = "spec:width=800dp,height=1280dp,dpi=480",
    showSystemUi = true,
)
@Preview(
    name = "Tablet Landscape",
    device = "spec:width=1280dp,height=800dp,dpi=480",
    showSystemUi = true,
)
annotation class TabletPreviews

/**
 * Preview annotation for component-level testing.
 * Focuses on the component without system UI.
 */
@Preview(
    name = "Component Light",
    showBackground = true,
    backgroundColor = 0xFFFFFBFE,
)
@Preview(
    name = "Component Dark",
    showBackground = true,
    backgroundColor = 0xFF1C1B1F,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
annotation class ComponentPreviews
