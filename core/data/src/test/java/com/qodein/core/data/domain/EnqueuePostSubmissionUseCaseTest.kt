package com.qodein.core.data.domain

import com.qodein.core.testing.data.UserMother
import com.qodein.core.testing.fake.FakePostSubmissionScheduler
import com.qodein.core.testing.fake.FakeUserRepository
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.UserError
import com.qodein.shared.domain.usecase.post.EnqueuePostSubmissionUseCase
import com.qodein.shared.domain.usecase.user.GetUserByIdUseCase
import com.qodein.shared.model.UserId
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EnqueuePostSubmissionUseCaseTest {

    private val scheduler = FakePostSubmissionScheduler()
    private val userRepository = FakeUserRepository()
    private val enqueue = EnqueuePostSubmissionUseCase(scheduler, GetUserByIdUseCase(userRepository))
    private val alice = UserMother.alice()

    private suspend fun enqueueAs(userId: UserId) =
        enqueue(authorId = userId, title = "Free coffee", content = null, imageUris = emptyList(), tags = emptyList())

    @Test
    fun schedulesWithAuthorProfile() =
        runTest {
            userRepository.users[alice.id.value] = alice

            val result = enqueueAs(alice.id)

            assertEquals(Result.Success(Unit), result)
            val submission = scheduler.submissions.single()
            assertEquals("Alice", submission.authorName)
            assertEquals(alice.profile.photoUrl, submission.authorAvatarUrl)
        }

    @Test
    fun unknownAuthorSchedulesNothing() =
        runTest {
            val result = enqueueAs(UserId("ghost"))

            assertEquals(Result.Error(UserError.ProfileFailure.NotFound), result)
            assertTrue(scheduler.submissions.isEmpty())
        }
}
