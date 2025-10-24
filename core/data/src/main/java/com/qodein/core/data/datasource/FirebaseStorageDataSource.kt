package com.qodein.core.data.datasource

import android.net.Uri
import co.touchlab.kermit.Logger
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.StorageError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.model.StoragePath
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data source for uploading files to Firebase Storage.
 * Handles image uploads with comprehensive error handling.
 */
@Singleton
internal class FirebaseStorageDataSource @Inject constructor(private val storage: FirebaseStorage) {
    companion object {
        private const val TAG = "FirebaseStorageDS"
    }

    /**
     * Upload a single image to Firebase Storage.
     * Returns download URL on success.
     *
     * @param uri Local URI of the image to upload
     * @param storagePath Storage path enum (POST_IMAGES, AVATARS, etc.)
     * @return Result with download URL or specific error
     */
    suspend fun uploadImage(
        uri: Uri,
        storagePath: StoragePath
    ): Result<String, OperationError> =
        try {
            Logger.d(TAG) { "Starting upload to ${storagePath.path}" }

            val filename = "${UUID.randomUUID()}.jpg"
            val storageReference = storage.reference
                .child(storagePath.path)
                .child(filename)

            storageReference.putFile(uri).await()
            val downloadUrl = storageReference.downloadUrl.await().toString()

            Logger.i(TAG) { "Upload successful: $downloadUrl" }
            Result.Success(downloadUrl)
        } catch (e: StorageException) {
            Logger.e(TAG, e) { "Storage exception: code=${e.errorCode}" }
            val error = when (e.errorCode) {
                StorageException.ERROR_NOT_AUTHENTICATED -> SystemError.Unauthorized
                StorageException.ERROR_NOT_AUTHORIZED -> SystemError.PermissionDenied
                StorageException.ERROR_QUOTA_EXCEEDED -> StorageError.UploadFailure.QuotaExceeded
                StorageException.ERROR_RETRY_LIMIT_EXCEEDED -> SystemError.Offline
                StorageException.ERROR_INVALID_CHECKSUM -> StorageError.UploadFailure.CorruptedFile
                StorageException.ERROR_CANCELED -> StorageError.UploadFailure.UploadCancelled
                else -> SystemError.Unknown
            }
            Result.Error(error)
        } catch (e: IOException) {
            Logger.e(TAG, e) { "Network error during upload" }
            Result.Error(SystemError.Offline)
        } catch (e: SecurityException) {
            Logger.e(TAG, e) { "Permission denied to read file" }
            Result.Error(SystemError.PermissionDenied)
        } catch (e: Exception) {
            Logger.e(TAG, e) { "Unexpected error during upload" }
            Result.Error(SystemError.Unknown)
        }
}
