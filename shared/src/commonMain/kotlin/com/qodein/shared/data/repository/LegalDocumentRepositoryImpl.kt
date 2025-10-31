package com.qodein.shared.data.repository

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.data.datasource.GithubMarkdownDataSource
import com.qodein.shared.data.datasource.PRIVACY_URL
import com.qodein.shared.data.datasource.TERMS_URL
import com.qodein.shared.domain.repository.LegalDocumentRepository
import com.qodein.shared.model.DocumentType
import com.qodein.shared.model.LegalDocument
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.io.IOException

class LegalDocumentRepositoryImpl(private val dataSource: GithubMarkdownDataSource) : LegalDocumentRepository {
    override suspend fun getLegalDocument(type: DocumentType): Result<LegalDocument, OperationError> =
        try {
            val content = when (type) {
                DocumentType.PrivacyPolicy -> dataSource.fetchMarkdown(PRIVACY_URL)
                DocumentType.TermsOfService -> dataSource.fetchMarkdown(TERMS_URL)
            }
            Result.Success(LegalDocument(type, content))
        } catch (e: HttpRequestTimeoutException) {
            Result.Error(SystemError.Offline)
        } catch (e: ConnectTimeoutException) {
            Result.Error(SystemError.ServiceDown)
        } catch (e: SocketTimeoutException) {
            Result.Error(SystemError.PermissionDenied)
        } catch (e: IOException) {
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Result.Error(SystemError.Unknown)
        }
}
