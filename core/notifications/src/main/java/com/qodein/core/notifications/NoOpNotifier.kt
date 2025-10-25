package com.qodein.core.notifications

import javax.inject.Inject

/**
 * Implementation of [Notifier] which does nothing. Useful for tests and previews.
 */
internal class NoOpNotifier @Inject constructor() : Notifier {
    override fun showUploadProgress(
        uploadId: String,
        progress: Int,
        max: Int
    ) = Unit

    override fun showUploadSuccess(uploadId: String) = Unit

    override fun showUploadError(uploadId: String) = Unit
}
