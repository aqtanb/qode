package com.qodein.shared.domain.repository

import com.qodein.shared.model.UserId

/**
 * Platform-agnostic interface for scheduling background post submissions.
 * Implementation should handle persistence and retry logic.
 */
interface PostSubmissionScheduler {
    /**
     * Schedules a post submission to be uploaded in the background.
     * Returns immediately, upload happens asynchronously with notifications.
     */
    fun schedulePostSubmission(
        authorId: UserId,
        authorName: String,
        authorAvatarUrl: String?,
        title: String,
        content: String?,
        imageUris: List<String>,
        tags: List<String>
    )
}
