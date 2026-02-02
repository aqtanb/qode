package com.qodein.core.notifications

/**
 * Content type for upload notifications
 */
enum class UploadContentType {
    POST,
    PROMOCODE
}

/**
 * Interface for creating notifications in the app
 */
interface Notifier {
    fun showUploadProgress(
        uploadId: String,
        progress: Int,
        max: Int
    )
    fun showUploadSuccess(
        uploadId: String,
        contentType: UploadContentType,
        contentId: String
    )
    fun showUploadError(uploadId: String)
}
