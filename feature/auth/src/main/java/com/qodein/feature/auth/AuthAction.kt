package com.qodein.feature.auth

import com.qodein.shared.model.DocumentType

/**
 * Sign-in screen specific actions - UI interaction events.
 *
 * Handles screen-specific user interactions and navigation actions.
 */
sealed interface AuthAction {
    data object AuthWithGoogleClicked : AuthAction
    data object AuthRetryClicked : AuthAction
    data object AuthErrorDismissed : AuthAction

    data class LegalDocumentClicked(val documentType: DocumentType) : AuthAction
    data class LegalDocumentRetryClicked(val documentType: DocumentType) : AuthAction
    data object LegalDocumentDismissed : AuthAction
}
