package com.qodein.core.data.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.qodein.core.notifications.UploadContentType
import com.qodein.core.testing.fake.FakeNotifier
import com.qodein.core.testing.fake.FakePostRepository
import com.qodein.core.testing.fake.FakeStorageRepository
import com.qodein.shared.common.error.DatabaseError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.domain.usecase.post.SubmitPostUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UploadPostWorkerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val postRepository = FakePostRepository()
    private val storageRepository = FakeStorageRepository()
    private val notifier = FakeNotifier()

    private val factory = object : WorkerFactory() {
        override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters
        ) = UploadPostWorker(appContext, workerParameters, SubmitPostUseCase(postRepository, storageRepository), notifier)
    }

    private fun worker(
        title: String = "Free coffee",
        imageUris: List<String> = emptyList(),
        runAttemptCount: Int = 0
    ): UploadPostWorker {
        val input = UploadPostWorker.createWorkRequest(
            title = title,
            content = "Use it before Friday",
            imageUris = imageUris,
            tags = listOf("coffee"),
            authorId = "alice-id",
            authorUsername = "Alice",
            authorAvatarUrl = null,
        ).workSpec.input
        return TestListenableWorkerBuilder<UploadPostWorker>(context)
            .setWorkerFactory(factory)
            .setInputData(input)
            .setRunAttemptCount(runAttemptCount)
            .build()
    }

    @Test
    fun uploadsImagesCreatesPostAndNotifiesSuccess() =
        runTest {
            val worker = worker(imageUris = listOf("content://images/1", "content://images/2"))

            val result = worker.doWork()

            assertEquals(ListenableWorker.Result.success(), result)
            assertEquals(listOf("content://images/1", "content://images/2"), storageRepository.uploadedUris)
            val post = postRepository.createdPosts.single()
            assertEquals("Free coffee", post.title)
            assertEquals(2, post.imageUrls.size)
            assertEquals(listOf(1, 2), notifier.progress.map { it.progress })
            assertEquals(UploadContentType.POST, notifier.successes.single().contentType)
            assertEquals(post.id.value, notifier.successes.single().contentId)
        }

    @Test
    fun offlineRetriesWithoutErrorNotification() =
        runTest {
            storageRepository.uploadError = SystemError.Offline

            val result = worker(imageUris = listOf("content://images/1")).doWork()

            assertEquals(ListenableWorker.Result.retry(), result)
            assertTrue(notifier.errors.isEmpty())
            assertTrue(postRepository.createdPosts.isEmpty())
        }

    @Test
    fun serverErrorOnCreateRetries() =
        runTest {
            postRepository.createError = DatabaseError.Unavailable

            assertEquals(ListenableWorker.Result.retry(), worker().doWork())
        }

    @Test
    fun lastAttemptFailsAndNotifiesError() =
        runTest {
            storageRepository.uploadError = SystemError.Offline
            val worker = worker(
                imageUris = listOf("content://images/1"),
                runAttemptCount = UploadFailurePolicy.MAX_ATTEMPTS - 1,
            )

            val result = worker.doWork()

            assertEquals(ListenableWorker.Result.failure(), result)
            assertEquals(listOf(worker.id.toString()), notifier.errors)
        }

    @Test
    fun validationErrorFailsImmediately() =
        runTest {
            val worker = worker(title = "   ")

            val result = worker.doWork()

            assertEquals(ListenableWorker.Result.failure(), result)
            assertEquals(1, notifier.errors.size)
            assertTrue(postRepository.createdPosts.isEmpty())
        }

    @Test
    fun permissionDeniedFailsImmediately() =
        runTest {
            postRepository.createError = DatabaseError.PermissionDenied

            assertEquals(ListenableWorker.Result.failure(), worker().doWork())
            assertEquals(1, notifier.errors.size)
        }

    @Test
    fun missingInputFails() =
        runTest {
            val worker = TestListenableWorkerBuilder<UploadPostWorker>(context)
                .setWorkerFactory(factory)
                .setInputData(workDataOf("title" to "No author"))
                .build()

            assertEquals(ListenableWorker.Result.failure(), worker.doWork())
            assertTrue(postRepository.createdPosts.isEmpty())
        }
}
