package com.qodein.qode.ui.state

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.qodein.core.designsystem.icon.QodeActionIcons

/**
 * Smart defaults system for top bar configuration.
 *
 * Simplified approach:
 * - Basic: Just title, navigation, and actions (no profile/settings clutter)
 * - Main screens handle profile/settings manually
 * - Nested screens stay clean and simple
 */
sealed class TopBarConfig {

    /**
     * No top bar - used for screens that handle their own UI completely
     * Examples: Home screen, fullscreen content
     */
    data object None : TopBarConfig()

    /**
     * Main screen top bar - for top-level destinations with titles
     * Shows profile/settings buttons, no back navigation
     *
     * Examples: Search screen, Inbox screen
     * @param title The main title displayed in the top bar
     * @param subtitle Optional subtitle text
     * @param actions Optional custom actions in the trailing section
     */
    data class MainWithTitle(val title: String, val subtitle: String? = null, val actions: (@Composable RowScope.() -> Unit)? = null) :
        TopBarConfig()

    /**
     * Basic top bar with smart defaults - for nested screens
     * Shows back navigation, no profile/settings buttons
     *
     * Examples: Settings, form steps, detail screens
     * @param title The main title displayed in the top bar
     * @param subtitle Optional subtitle text
     * @param navigationIcon Icon for navigation action (defaults to back arrow)
     * @param actions Optional custom actions in the trailing section
     */
    data class Basic(
        val title: String,
        val subtitle: String? = null,
        val navigationIcon: ImageVector = QodeActionIcons.Back,
        val actions: (@Composable RowScope.() -> Unit)? = null
    ) : TopBarConfig()

    /**
     * Fully custom top bar content - for special screens
     * Examples: Custom search bars, special patterns
     */
    data class Custom(val content: @Composable () -> Unit) : TopBarConfig()
}

/**
 * Common configurations for quick access
 */
object CommonTopBarConfigs {

    fun settings() = TopBarConfig.Basic(title = "Settings")

    fun basicWithTitle(title: String) = TopBarConfig.Basic(title = title)

    fun formStep(
        stepTitle: String,
        stepSubtitle: String? = null,
        progress: (@Composable RowScope.() -> Unit)? = null
    ) = TopBarConfig.Basic(
        title = stepTitle,
        subtitle = stepSubtitle,
        actions = progress,
    )
}

/**
 * Extension functions for easy usage
 */
fun String.asBasicTopBar() = TopBarConfig.Basic(title = this)

fun String.asFormStep(
    subtitle: String? = null,
    progress: (@Composable RowScope.() -> Unit)? = null
) = CommonTopBarConfigs.formStep(this, subtitle, progress)
