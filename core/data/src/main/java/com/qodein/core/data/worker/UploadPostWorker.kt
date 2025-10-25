package com.qodein.core.data.worker

import android.content.Context
import androidx.core.net.toUri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.qodein.shared.common.Result
import com.qodein.shared.domain.repository.PostRepository
import com.qodein.shared.domain.repository.StorageRepository
import com.qodein.shared.model.Post
import com.qodein.shared.model.StoragePath
import com.qodein.shared.model.UserId
import com.qodein.shared.platform.PlatformUri
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlin.collections.emptyList

@HiltWorker
class UploadPostWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val storageRepository: StorageRepository,
    private val postRepository: PostRepository
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val title = inputData.getString(KEY_TITLE) ?: return Result.failure()
        val content = inputData.getString(KEY_CONTENT)
        val imageUris = inputData.getStringArray(KEY_IMAGE_URI)?.toList() ?: emptyList()
        val tags = inputData.getStringArray(KEY_TAGS)?.toList() ?: emptyList()
        val authorId = inputData.getString(KEY_AUTHOR_ID) ?: return Result.failure()
        val authorUsername = inputData.getString(KEY_AUTHOR_USERNAME) ?: return Result.failure()
        val authorAvatarUrl = inputData.getString(KEY_AUTHOR_AVATAR_URL)

        val imageUrls = mutableListOf<String>()
        imageUris.forEach { uriString ->
            val uri = PlatformUri(uriString.toUri())
            when (val result = storageRepository.uploadImage(uri, StoragePath.POST_IMAGES)) {
                is Result.Error -> {
                    return Result.failure()
                }

                is com.qodein.shared.common.Result.Success -> {
                    imageUrls.add(result.data)
                }
            }
        }

        val post = Post.create(
            authorId = UserId(authorId),
            authorUsername = authorUsername,
            title = title,
            content = content,
            imageUrls = imageUrls,
            tags = tags,
            authorAvatarUrl = authorAvatarUrl,
        )
        when (post) {
            is com.qodein.shared.common.Result.Error -> {
                return Result.failure()
            }

            is com.qodein.shared.common.Result.Success -> {
                return when (val result = postRepository.createPost(post.data)) {
                    is Result.Error -> {
                        Result.failure()
                    }

                    is com.qodein.shared.common.Result.Success -> {
                        Result.success()
                    }
                }
            }
        }
    }
    companion object {
        const val KEY_TITLE = "title"
        const val KEY_CONTENT = "content"
        const val KEY_IMAGE_URI = "imageUri"
        const val KEY_TAGS = "tags"
        const val KEY_AUTHOR_ID = "authorId"
        const val KEY_AUTHOR_USERNAME = "authorUsername"
        const val KEY_AUTHOR_AVATAR_URL = "authorAvatarUrl"
    }
}
