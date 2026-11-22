package com.qodein.shared.ios

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.StorageError
import com.qodein.shared.data.datasource.SupabaseStorageDataSource
import com.qodein.shared.data.util.remoteCall
import com.qodein.shared.domain.repository.StorageRepository
import com.qodein.shared.model.StoragePath
import com.qodein.shared.platform.PlatformUri
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfFile
import platform.posix.memcpy

/** Uploads a file the Swift side already prepared as JPEG; [PlatformUri.url] is its local path. */
internal class IosStorageRepository(private val dataSource: SupabaseStorageDataSource) : StorageRepository {
    override suspend fun uploadImage(
        uri: PlatformUri,
        storagePath: StoragePath
    ): Result<String, OperationError> {
        val bytes = NSData.dataWithContentsOfFile(uri.url)?.toByteArray()
            ?: return Result.Error(StorageError.UploadFailure.CorruptedFile)
        return remoteCall("Upload image") { dataSource.uploadImage(storagePath.path, bytes, "image/jpeg") }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val size = length.toInt()
    val bytes = ByteArray(size)
    if (size > 0) bytes.usePinned { memcpy(it.addressOf(0), this.bytes, length) }
    return bytes
}
