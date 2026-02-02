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

class SubmitPostUseCase(private val postRepository: PostRepository, private val storageRepository: StorageRepository) {
    suspend operator fun invoke(
        authorId: UserId,
        authorName: String,
        title: String,
        content: String? = null,
        imageUris: List<PlatformUri> = emptyList(),
        tags: List<String> = emptyList(),
        authorAvatarUrl: String? = null,
        onProgress: (current: Int, total: Int) -> Unit = { _, _ -> }
    ): Result<String, OperationError> {
        Logger.i { "Creating post: $title by $authorName with ${imageUris.size} images" }

        val imageUrls = mutableListOf<String>()
        imageUris.forEachIndexed { index, uri ->
            Logger.d { "Uploading image ${index + 1}/${imageUris.size}: $uri" }
            onProgress(index + 1, imageUris.size)

            when (val result = storageRepository.uploadImage(uri, StoragePath.POST_IMAGES)) {
                is Result.Error -> {
                    Logger.e { "Image upload failed: ${result.error}" }
                    return Result.Error(result.error)
                }
                is Result.Success -> {
                    Logger.d { "Image uploaded successfully: ${result.data}" }
                    imageUrls.add(result.data)
                }
            }
        }

        return when (
            val createResult = Post.create(
                authorId = authorId,
                authorName = authorName,
                title = title,
                content = content ?: "",
                imageUrls = imageUrls,
                tags = tags,
                authorAvatarUrl = authorAvatarUrl,
            )
        ) {
            is Result.Success -> {
                Logger.d { "Post validation passed, delegating to repository" }
                val post = createResult.data
                when (val result = postRepository.createPost(post)) {
                    is Result.Success -> Result.Success(post.id.value)
                    is Result.Error -> Result.Error(result.error)
                }
            }
            is Result.Error -> {
                Logger.e { "Post validation failed: ${createResult.error}" }
                Result.Error(createResult.error)
            }
        }
    }
}
