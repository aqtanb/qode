package com.qodein.shared.domain.usecase.post

import co.touchlab.kermit.Logger
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.PostRepository
import com.qodein.shared.model.Post
import com.qodein.shared.model.UserId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Use case for creating posts with domain validation.
 *
 * Validates using Post.create() factory, then delegates to repository.
 */
class CreatePostUseCase(private val postRepository: PostRepository) {

    companion object {
        private const val TAG = "CreatePostUseCase"
    }

    operator fun invoke(
        authorId: UserId,
        authorUsername: String,
        title: String,
        content: String,
        imageUrls: List<String> = emptyList(),
        tags: List<String> = emptyList(),
        authorAvatarUrl: String? = null
    ): Flow<Result<Post, OperationError>> =
        flow {
            Logger.i(TAG) { "Creating post: $title by $authorUsername" }

            // Domain-level validation via Post.create()
            when (
                val createResult = Post.create(
                    authorId = authorId,
                    authorUsername = authorUsername,
                    title = title,
                    content = content,
                    imageUrls = imageUrls,
                    tags = tags,
                    authorAvatarUrl = authorAvatarUrl,
                )
            ) {
                is Result.Success -> {
                    Logger.d(TAG) { "Post validation passed, delegating to repository" }
                    postRepository.createPost(createResult.data).collect { result ->
                        emit(result)
                    }
                }
                is Result.Error -> {
                    Logger.e(TAG) { "Post validation failed: ${createResult.error}" }
                    emit(Result.Error(createResult.error))
                }
            }
        }
}
