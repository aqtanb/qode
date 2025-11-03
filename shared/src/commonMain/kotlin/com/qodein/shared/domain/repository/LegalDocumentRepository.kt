package com.qodein.shared.domain.repository

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.DocumentType
import com.qodein.shared.model.LegalDocument

interface LegalDocumentRepository {
    suspend fun getLegalDocument(type: DocumentType): Result<LegalDocument, OperationError>
}
