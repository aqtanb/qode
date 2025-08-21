package com.qodein.qode.ui.container

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.qodein.qode.ui.QodeAppViewModel
import com.qodein.shared.model.Theme

/**
 * Container responsible for theme management and status bar appearance.
 *
 * Simplified approach since we always use translucent overlay:
 * - Theme determines both status bar content color and overlay color
 * - No need for surface luminance calculation
 * - Consistent theme-based approach
 */
@Composable
fun AppThemeContainer(
    viewModel: QodeAppViewModel,
    content: @Composable (statusBarOverlayColor: Color) -> Unit
) {
    // Theme state observation
    val themeState by viewModel.themeState.collectAsStateWithLifecycle()
    val systemDarkTheme = isSystemInDarkTheme()

    // System references
    val view = LocalView.current
    val context = LocalContext.current

    // Determine theme-based status bar appearance
    val isDarkTheme = when (themeState) {
        Theme.LIGHT -> false
        Theme.DARK -> true
        Theme.SYSTEM -> systemDarkTheme
    }

    // Set status bar appearance based on theme
    SideEffect {
        val window = (context as Activity).window
        val windowInsetsController = WindowCompat.getInsetsController(window, view)

        // Dark theme = light status bar content, Light theme = dark status bar content
        windowInsetsController.isAppearanceLightStatusBars = !isDarkTheme
    }

    // Status bar overlay color matches the theme logic
    val statusBarOverlayColor = if (isDarkTheme) {
        Color.Black.copy(alpha = 0.3f) // Dark overlay for light status text
    } else {
        Color.White.copy(alpha = 0.3f) // Light overlay for dark status text
    }

    content(statusBarOverlayColor)
}
