package com.qodein.shared.data.datasource

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.storage.Storage
import io.ktor.http.ContentType
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class SupabaseStorageDataSource(private val storage: Storage, private val auth: Auth) {
    /** Files go under the user's ID, which the bucket policy requires. */
    @OptIn(ExperimentalUuidApi::class)
    suspend fun uploadImage(
        bucket: String,
        bytes: ByteArray,
        mimeType: String
    ): String {
        val userId = requireNotNull(auth.currentUserOrNull()?.id) { "Upload needs a signed-in user" }
        val path = "$userId/${Uuid.random()}"
        val files = storage.from(bucket)
        files.upload(path, bytes) { contentType = ContentType.parse(mimeType) }
        return files.publicUrl(path)
    }
}
