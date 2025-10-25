package com.qodein.core.notifications

/**
 * Interface for creating notifications in the app
 */
interface Notifier {
    fun showUploadProgress(
        uploadId: String,
        progress: Int,
        max: Int
    )
    fun showUploadSuccess(uploadId: String)
    fun showUploadError(
        uploadId: String,
        errorMessage: String
    )
}
