package com.qodein.qode.ui.container

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalUriHandler
import com.qodein.core.ui.component.ComingSoonDialog
import com.qodein.qode.ui.QodeAppViewModel
import com.qodein.qode.ui.state.AppUiEvents

/**
 * Container responsible for app-level dialog management.
 *
 * Handles:
 * - Coming Soon dialogs for unimplemented features
 * - App-level modal dialogs
 * - Dialog state management
 * - External link handling
 *
 * Benefits:
 * - Centralized dialog logic
 * - Consistent dialog behavior across app
 * - Easy to extend with new dialogs
 * - Clean separation from main UI logic
 */
@Composable
fun AppDialogsContainer(viewModel: QodeAppViewModel) {
    val uriHandler = LocalUriHandler.current

    // Local state for different dialogs
    var showGeneralComingSoon by remember { mutableStateOf(false) }
    var showInboxComingSoon by remember { mutableStateOf(false) }
    var showFavoritesComingSoon by remember { mutableStateOf(false) }

    // Observe UI events to trigger dialogs
    // Note: This could be improved with a proper event system
    // For now, we'll handle it in the main QodeApp

    // General Coming Soon Dialog
    if (showGeneralComingSoon) {
        ComingSoonDialog(
            onDismiss = { showGeneralComingSoon = false },
            onTelegramClick = {
                uriHandler.openUri("https://www.t.me/qodeinhq")
            },
        )
    }

    // Inbox Coming Soon Dialog
    if (showInboxComingSoon) {
        ComingSoonDialog(
            onDismiss = { showInboxComingSoon = false },
            onTelegramClick = {
                uriHandler.openUri("https://www.t.me/qodeinhq")
            },
        )
    }

    // Favorites Coming Soon Dialog
    if (showFavoritesComingSoon) {
        ComingSoonDialog(
            onDismiss = { showFavoritesComingSoon = false },
            onTelegramClick = {
                uriHandler.openUri("https://www.t.me/qodeinhq")
            },
        )
    }
}

/**
 * Dialog state management functions
 *
 * These could be moved to a proper state holder if the dialog logic becomes more complex
 */
@Composable
fun rememberDialogState() =
    remember {
        DialogState()
    }

class DialogState {
    var showGeneralComingSoon by mutableStateOf(false)
        private set
    var showInboxComingSoon by mutableStateOf(false)
        private set
    var showFavoritesComingSoon by mutableStateOf(false)
        private set

    fun showComingSoon() {
        showGeneralComingSoon = true
    }

    fun showInboxComingSoon() {
        showInboxComingSoon = true
    }

    fun showFavoritesComingSoon() {
        showFavoritesComingSoon = true
    }

    fun hideComingSoon() {
        showGeneralComingSoon = false
    }

    fun hideInboxComingSoon() {
        showInboxComingSoon = false
    }

    fun hideFavoritesComingSoon() {
        showFavoritesComingSoon = false
    }

    fun handleUiEvent(event: AppUiEvents) {
        when (event) {
            AppUiEvents.ShowComingSoonDialog -> showComingSoon()
            AppUiEvents.ShowInboxComingSoon -> showInboxComingSoon()
            AppUiEvents.ShowFavoritesComingSoon -> showFavoritesComingSoon()
            AppUiEvents.DismissComingSoonDialog -> hideComingSoon()
            else -> { /* Other events are not dialog-related */ }
        }
    }
}

/**
 * Improved dialog container that uses state management
 */
@Composable
fun AppDialogsContainer(dialogState: DialogState) {
    val uriHandler = LocalUriHandler.current

    // General Coming Soon Dialog
    if (dialogState.showGeneralComingSoon) {
        ComingSoonDialog(
            onDismiss = { dialogState.hideComingSoon() },
            onTelegramClick = {
                uriHandler.openUri("https://www.t.me/qodeinhq")
            },
        )
    }

    // Inbox Coming Soon Dialog
    if (dialogState.showInboxComingSoon) {
        ComingSoonDialog(
            onDismiss = { dialogState.hideInboxComingSoon() },
            onTelegramClick = {
                uriHandler.openUri("https://www.t.me/qodeinhq")
            },
        )
    }

    // Favorites Coming Soon Dialog
    if (dialogState.showFavoritesComingSoon) {
        ComingSoonDialog(
            onDismiss = { dialogState.hideFavoritesComingSoon() },
            onTelegramClick = {
                uriHandler.openUri("https://www.t.me/qodeinhq")
            },
        )
    }
}
