package com.qodein.shared.common.error

/**
 * Domain errors for legal documents (Privacy Policy, Terms of Service).
 */
sealed interface LegalDocumentError : OperationError {
    /** Requested document does not exist at the source */
    data object NotFound : LegalDocumentError

    /** Upstream service is unavailable or unreachable */
    data object Unavailable : LegalDocumentError
}
