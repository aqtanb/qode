package com.qodein.core.data.repository

import android.content.Context
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.StorageError
import com.qodein.shared.data.datasource.SupabaseStorageDataSource
import com.qodein.shared.data.util.remoteCall
import com.qodein.shared.domain.repository.StorageRepository
import com.qodein.shared.model.StoragePath
import com.qodein.shared.platform.PlatformUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class StorageRepositoryImpl(private val context: Context, private val dataSource: SupabaseStorageDataSource) : StorageRepository {
    override suspend fun uploadImage(
        uri: PlatformUri,
        storagePath: StoragePath
    ): Result<String, OperationError> {
        val resolver = context.contentResolver
        val bytes = withContext(Dispatchers.IO) { resolver.openInputStream(uri.uri)?.use { it.readBytes() } }
            ?: return Result.Error(StorageError.UploadFailure.CorruptedFile)
        val mimeType = resolver.getType(uri.uri) ?: DEFAULT_MIME_TYPE

        return remoteCall("Upload image") { dataSource.uploadImage(storagePath.path, bytes, mimeType) }
    }

    private companion object {
        const val DEFAULT_MIME_TYPE = "image/jpeg"
    }
}
