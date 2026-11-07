package com.qodein.core.testing.fake

import com.qodein.shared.domain.repository.PostSubmissionScheduler
import com.qodein.shared.model.UserId

class FakePostSubmissionScheduler : PostSubmissionScheduler {
    data class Submission(
        val authorId: UserId,
        val authorName: String,
        val authorAvatarUrl: String?,
        val title: String,
        val content: String?,
        val imageUris: List<String>,
        val tags: List<String>
    )

    val submissions = mutableListOf<Submission>()

    override fun schedulePostSubmission(
        authorId: UserId,
        authorName: String,
        authorAvatarUrl: String?,
        title: String,
        content: String?,
        imageUris: List<String>,
        tags: List<String>
    ) {
        submissions += Submission(authorId, authorName, authorAvatarUrl, title, content, imageUris, tags)
    }
}
