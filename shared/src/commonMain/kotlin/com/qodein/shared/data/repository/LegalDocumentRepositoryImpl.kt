package com.qodein.shared.data.repository

import co.touchlab.kermit.Logger
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

    companion object {
        private const val TAG = "LegalDocumentRepo"
    }

    override suspend fun getLegalDocument(type: DocumentType): Result<LegalDocument, OperationError> =
        try {
            Logger.d(TAG) { "Fetching legal document: $type" }
            val content = when (type) {
                DocumentType.PrivacyPolicy -> dataSource.fetchMarkdown(PRIVACY_URL)
                DocumentType.TermsOfService -> dataSource.fetchMarkdown(TERMS_URL)
            }
            Logger.i(TAG) { "Successfully fetched $type (${content.length} chars)" }
            Result.Success(LegalDocument(type, content))
        } catch (e: HttpRequestTimeoutException) {
            Logger.e(TAG, e) { "HTTP request timeout for $type" }
            Result.Error(SystemError.Offline)
        } catch (e: ConnectTimeoutException) {
            Logger.e(TAG, e) { "Connection timeout for $type" }
            Result.Error(SystemError.ServiceDown)
        } catch (e: SocketTimeoutException) {
            Logger.e(TAG, e) { "Socket timeout for $type" }
            Result.Error(SystemError.Offline)
        } catch (e: IOException) {
            Logger.e(TAG, e) { "I/O error fetching $type: ${e.message}" }
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Logger.e(TAG, e) { "Unexpected error fetching $type: ${e::class.simpleName} - ${e.message}" }
            Result.Error(SystemError.Unknown)
        }
}
