package com.qodein.shared.domain.usecase.legal

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.LegalDocumentRepository
import com.qodein.shared.model.DocumentType
import com.qodein.shared.model.LegalDocument

/**
 * Use case for retrieving legal documents (Terms of Service, Privacy Policy).
 */
class GetLegalDocumentUseCase(private val legalDocumentRepository: LegalDocumentRepository) {
    suspend operator fun invoke(type: DocumentType): Result<LegalDocument, OperationError> = legalDocumentRepository.getLegalDocument(type)
}
