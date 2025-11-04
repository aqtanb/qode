package com.qodein.feature.auth

import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.DocumentType
import com.qodein.shared.model.LegalDocument

/**
 * Sign-in screen UI state - handles screen-specific concerns.
 *
 * Used by AuthScreen to manage sign-in UI states and navigation events.
 */
data class AuthUiState(val isSigningIn: Boolean = false, val error: OperationError? = null)

/**
 * UI state for legal document bottom sheets (Terms of Service, Privacy Policy).
 * Manages the display state and content for legal document bottom sheets.
 */
sealed interface LegalDocumentUiState {
    /**
     * Bottom sheet is not visible
     */
    data object Closed : LegalDocumentUiState

    /**
     * Document is being fetched
     * @param documentType The type of document being loaded (for UI labels)
     */
    data class Loading(val documentType: DocumentType) : LegalDocumentUiState

    /**
     * Document loaded successfully and ready to display
     * @param document The legal document with markdown content
     */
    data class Content(val document: LegalDocument) : LegalDocumentUiState

    /**
     * Failed to load document
     * @param documentType The document type that failed (for retry logic)
     * @param errorType The specific error that occurred
     */
    data class Error(val documentType: DocumentType, val errorType: OperationError) : LegalDocumentUiState
}
