package com.qodein.core.testing.fake

import com.qodein.core.notifications.Notifier
import com.qodein.core.notifications.UploadContentType

class FakeNotifier : Notifier {
    data class Progress(val uploadId: String, val progress: Int, val max: Int)
    data class Success(val uploadId: String, val contentType: UploadContentType, val contentId: String)

    val progress = mutableListOf<Progress>()
    val successes = mutableListOf<Success>()
    val errors = mutableListOf<String>()

    override fun showUploadProgress(
        uploadId: String,
        progress: Int,
        max: Int
    ) {
        this.progress += Progress(uploadId, progress, max)
    }

    override fun showUploadSuccess(
        uploadId: String,
        contentType: UploadContentType,
        contentId: String
    ) {
        successes += Success(uploadId, contentType, contentId)
    }

    override fun showUploadError(uploadId: String) {
        errors += uploadId
    }
}
