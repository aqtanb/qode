package com.qodein.shared.domain.repository

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.StoragePath
import com.qodein.shared.platform.PlatformUri

/**
 * Repository for file storage operations.
 * Abstracts platform-specific storage implementations (Firebase Storage, AWS S3, etc.).
 */
interface StorageRepository {
    /**
     * Upload an image to cloud storage.
     *
     * @param uri Platform-specific URI of the image to upload
     * @param storagePath Storage path category (POST_IMAGES, AVATARS, etc.)
     * @return Download URL on success, or specific error
     */
    suspend fun uploadImage(
        uri: PlatformUri,
        storagePath: StoragePath
    ): Result<String, OperationError>
}
