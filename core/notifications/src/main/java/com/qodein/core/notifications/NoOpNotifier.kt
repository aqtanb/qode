package com.qodein.core.notifications

/**
 * Implementation of [Notifier] which does nothing. Useful for tests and previews.
 */
internal class NoOpNotifier : Notifier {
    override fun showUploadProgress(
        uploadId: String,
        progress: Int,
        max: Int
    ) = Unit

    override fun showUploadSuccess(uploadId: String) = Unit

    override fun showUploadError(uploadId: String) = Unit
}
