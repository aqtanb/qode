package com.qodein.feature.auth

import android.content.Context
import com.qodein.shared.model.DocumentType

/**
 * Sign-in screen specific actions - UI interaction events.
 *
 * Handles screen-specific user interactions and navigation actions.
 */
sealed interface AuthAction {
    data class AuthWithGoogleClicked(val activityContext: Context) : AuthAction
    data object AuthErrorDismissed : AuthAction

    data class LegalDocumentClicked(val documentType: DocumentType) : AuthAction
    data class LegalDocumentRetryClicked(val documentType: DocumentType) : AuthAction
    data object LegalDocumentDismissed : AuthAction
}
