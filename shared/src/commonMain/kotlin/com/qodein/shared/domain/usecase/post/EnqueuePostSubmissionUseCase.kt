package com.qodein.shared.domain.usecase.post

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.PostSubmissionScheduler
import com.qodein.shared.domain.usecase.user.GetUserByIdUseCase
import com.qodein.shared.model.UserId

class EnqueuePostSubmissionUseCase(
    private val postSubmissionScheduler: PostSubmissionScheduler,
    private val getUserByIdUseCase: GetUserByIdUseCase
) {
    suspend operator fun invoke(
        authorId: UserId,
        title: String,
        content: String?,
        imageUris: List<String>,
        tags: List<String>
    ): Result<Unit, OperationError> =
        when (val userResult = getUserByIdUseCase(authorId.value)) {
            is Result.Error -> Result.Error(userResult.error)
            is Result.Success -> {
                val user = userResult.data
                postSubmissionScheduler.schedulePostSubmission(
                    authorId = authorId,
                    authorName = user.displayName ?: "",
                    authorAvatarUrl = user.profile.photoUrl,
                    title = title,
                    content = content,
                    imageUris = imageUris,
                    tags = tags,
                )
                Result.Success(Unit)
            }
        }
}
