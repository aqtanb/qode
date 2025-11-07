package com.qodein.core.data.repository

import com.qodein.core.data.datasource.FirebaseStorageDataSource
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.StorageRepository
import com.qodein.shared.model.StoragePath
import com.qodein.shared.platform.PlatformUri

internal class StorageRepositoryImpl constructor(private val firebaseStorage: FirebaseStorageDataSource) : StorageRepository {
    override suspend fun uploadImage(
        uri: PlatformUri,
        storagePath: StoragePath
    ): Result<String, OperationError> = firebaseStorage.uploadImage(uri.uri, storagePath)
}
