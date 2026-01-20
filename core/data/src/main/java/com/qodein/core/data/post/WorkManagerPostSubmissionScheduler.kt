package com.qodein.core.data.post

import androidx.work.WorkManager
import com.qodein.core.data.worker.UploadPostWorker
import com.qodein.shared.domain.repository.PostSubmissionScheduler
import com.qodein.shared.model.UserId

class WorkManagerPostSubmissionScheduler(private val workManager: WorkManager) : PostSubmissionScheduler {

    override fun schedulePostSubmission(
        authorId: UserId,
        authorName: String,
        authorAvatarUrl: String?,
        title: String,
        content: String?,
        imageUris: List<String>,
        tags: List<String>
    ) {
        val workRequest = UploadPostWorker.createWorkRequest(
            title = title,
            content = content,
            imageUris = imageUris,
            tags = tags,
            authorId = authorId.value,
            authorUsername = authorName,
            authorAvatarUrl = authorAvatarUrl,
        )
        workManager.enqueue(workRequest)
    }
}
