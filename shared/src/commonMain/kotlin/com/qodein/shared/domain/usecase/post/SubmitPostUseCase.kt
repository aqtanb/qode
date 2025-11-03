package com.qodein.shared.domain.usecase.post

import co.touchlab.kermit.Logger
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.PostRepository
import com.qodein.shared.domain.repository.StorageRepository
import com.qodein.shared.model.Post
import com.qodein.shared.model.StoragePath
import com.qodein.shared.model.UserId
import com.qodein.shared.platform.PlatformUri

/**
 * Use case for creating posts with domain validation and image upload.
 *
 * Uploads images to storage, validates using Post.create() factory, then delegates to repository.
 */
class SubmitPostUseCase(private val postRepository: PostRepository, private val storageRepository: StorageRepository) {

    companion object {
        private const val TAG = "SubmitPostUseCase"
    }

    suspend operator fun invoke(
        authorId: UserId,
        authorUsername: String,
        title: String,
        content: String? = null,
        imageUris: List<PlatformUri> = emptyList(),
        tags: List<String> = emptyList(),
        authorAvatarUrl: String? = null,
        onProgress: (current: Int, total: Int) -> Unit = { _, _ -> }
    ): Result<Post, OperationError> {
        Logger.i(TAG) { "Creating post: $title by $authorUsername with ${imageUris.size} images" }

        val imageUrls = mutableListOf<String>()
        imageUris.forEachIndexed { index, uri ->
            Logger.d(TAG) { "Uploading image ${index + 1}/${imageUris.size}: $uri" }
            onProgress(index + 1, imageUris.size)

            when (val result = storageRepository.uploadImage(uri, StoragePath.POST_IMAGES)) {
                is Result.Error -> {
                    Logger.e(TAG) { "Image upload failed: ${result.error}" }
                    return Result.Error(result.error)
                }
                is Result.Success -> {
                    Logger.d(TAG) { "Image uploaded successfully: ${result.data}" }
                    imageUrls.add(result.data)
                }
            }
        }

        return when (
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
                postRepository.createPost(createResult.data)
            }
            is Result.Error -> {
                Logger.e(TAG) { "Post validation failed: ${createResult.error}" }
                Result.Error(createResult.error)
            }
        }
    }
}
