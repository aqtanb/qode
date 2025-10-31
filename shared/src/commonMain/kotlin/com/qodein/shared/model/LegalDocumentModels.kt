package com.qodein.shared.model

import kotlinx.serialization.Serializable

/**
 * Type of legal document to fetch and display.
 */
@Serializable
sealed class DocumentType {
    @Serializable
    data object TermsOfService : DocumentType()

    @Serializable
    data object PrivacyPolicy : DocumentType()
}

/**
 * Legal document containing the content.
 */
@Serializable
data class LegalDocument(val type: DocumentType, val content: String) {
    init {
        require(content.isNotBlank()) { "Legal document content cannot be blank" }
    }
}
