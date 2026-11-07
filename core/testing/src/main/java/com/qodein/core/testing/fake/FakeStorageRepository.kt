package com.qodein.core.testing.fake

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.StorageRepository
import com.qodein.shared.model.StoragePath
import com.qodein.shared.platform.PlatformUri

class FakeStorageRepository : StorageRepository {
    val uploadedUris = mutableListOf<String>()

    /** When set, every upload fails with this error. */
    var uploadError: OperationError? = null

    override suspend fun uploadImage(
        uri: PlatformUri,
        storagePath: StoragePath
    ): Result<String, OperationError> {
        uploadError?.let { return Result.Error(it) }
        uploadedUris += uri.toString()
        return Result.Success("https://cdn.test/${storagePath.name.lowercase()}/${uploadedUris.size}")
    }
}
