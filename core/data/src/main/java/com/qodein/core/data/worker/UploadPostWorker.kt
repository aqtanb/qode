package com.qodein.core.data.worker

import android.content.Context
import androidx.core.net.toUri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.qodein.core.notifications.Notifier
import com.qodein.shared.domain.usecase.post.SubmitPostUseCase
import com.qodein.shared.model.UserId
import com.qodein.shared.platform.PlatformUri
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import com.qodein.shared.common.Result as DomainResult

private const val KEY_TITLE = "title"
private const val KEY_CONTENT = "content"
private const val KEY_IMAGE_URI = "imageUri"
private const val KEY_TAGS = "tags"
private const val KEY_AUTHOR_ID = "authorId"
private const val KEY_AUTHOR_USERNAME = "authorUsername"
private const val KEY_AUTHOR_AVATAR_URL = "authorAvatarUrl"

@HiltWorker
class UploadPostWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val submitPostUseCase: SubmitPostUseCase,
    private val notifier: Notifier
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): ListenableWorker.Result {
        val uploadId = id.toString()

        val title = inputData.getString(KEY_TITLE) ?: return ListenableWorker.Result.failure()
        val content = inputData.getString(KEY_CONTENT)
        val imageUriStrings = inputData.getStringArray(KEY_IMAGE_URI)?.toList() ?: emptyList()
        val tags = inputData.getStringArray(KEY_TAGS)?.toList() ?: emptyList()
        val authorId = inputData.getString(KEY_AUTHOR_ID) ?: return ListenableWorker.Result.failure()
        val authorUsername = inputData.getString(KEY_AUTHOR_USERNAME) ?: return ListenableWorker.Result.failure()
        val authorAvatarUrl = inputData.getString(KEY_AUTHOR_AVATAR_URL)

        val imageUris = imageUriStrings.map { PlatformUri(it.toUri()) }

        when (
            val result = submitPostUseCase(
                authorId = UserId(authorId),
                authorUsername = authorUsername,
                title = title,
                content = content,
                imageUris = imageUris,
                tags = tags,
                authorAvatarUrl = authorAvatarUrl,
                onProgress = { current, total ->
                    notifier.showUploadProgress(uploadId, current, total)
                },
            )
        ) {
            is DomainResult.Error -> {
                notifier.showUploadError(uploadId)
                return ListenableWorker.Result.failure()
            }
            is DomainResult.Success -> {
                notifier.showUploadSuccess(uploadId)
                return ListenableWorker.Result.success()
            }
        }
    }

    companion object {
        fun createWorkRequest(
            title: String,
            content: String?,
            imageUris: List<String>,
            tags: List<String>,
            authorId: String,
            authorUsername: String,
            authorAvatarUrl: String?
        ): OneTimeWorkRequest {
            val inputData = workDataOf(
                KEY_TITLE to title,
                KEY_CONTENT to content,
                KEY_IMAGE_URI to imageUris.toTypedArray(),
                KEY_TAGS to tags.toTypedArray(),
                KEY_AUTHOR_ID to authorId,
                KEY_AUTHOR_USERNAME to authorUsername,
                KEY_AUTHOR_AVATAR_URL to authorAvatarUrl,
            )

            return OneTimeWorkRequestBuilder<UploadPostWorker>()
                .setInputData(inputData)
                .build()
        }
    }
}
