package com.qodein.core.data.domain

import com.qodein.core.testing.fake.FakePostRepository
import com.qodein.core.testing.fake.FakeStorageRepository
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.DatabaseError
import com.qodein.shared.common.error.PostError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.domain.usecase.post.SubmitPostUseCase
import com.qodein.shared.model.UserId
import com.qodein.shared.platform.PlatformUri
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SubmitPostUseCaseTest {

    private val postRepository = FakePostRepository()
    private val storageRepository = FakeStorageRepository()
    private val submitPost = SubmitPostUseCase(postRepository, storageRepository)

    private suspend fun submit(
        title: String = "Free coffee",
        images: List<String> = emptyList(),
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ) = submitPost(
        authorId = UserId("alice-id"),
        authorName = "Alice",
        title = title,
        content = "Use it before Friday",
        imageUris = images.map { PlatformUri.parse(it) },
        tags = listOf("coffee"),
        onProgress = onProgress,
    )

    @Test
    fun uploadsImagesThenSavesPostWithTheirUrls() =
        runTest {
            val progress = mutableListOf<Pair<Int, Int>>()

            val result = submit(images = listOf("content://images/1", "content://images/2")) { current, total ->
                progress += current to total
            }

            val post = postRepository.createdPosts.single()
            assertEquals(Result.Success(post.id.value), result)
            assertEquals(listOf("https://cdn.test/post_images/1", "https://cdn.test/post_images/2"), post.imageUrls)
            assertEquals(listOf(1 to 2, 2 to 2), progress)
        }

    @Test
    fun failedImageUploadStopsBeforeSavingPost() =
        runTest {
            storageRepository.uploadError = SystemError.Offline

            val result = submit(images = listOf("content://images/1"))

            assertEquals(Result.Error(SystemError.Offline), result)
            assertTrue(postRepository.createdPosts.isEmpty())
        }

    @Test
    fun invalidPostIsNotSaved() =
        runTest {
            val result = submit(title = "")

            assertEquals(Result.Error(PostError.CreationFailure.EmptyTitle), result)
            assertTrue(postRepository.createdPosts.isEmpty())
        }

    @Test
    fun repositoryErrorIsReturned() =
        runTest {
            postRepository.createError = DatabaseError.PermissionDenied

            assertEquals(Result.Error(DatabaseError.PermissionDenied), submit())
        }
}
