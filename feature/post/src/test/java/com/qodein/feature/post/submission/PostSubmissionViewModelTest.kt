package com.qodein.feature.post.submission

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.qodein.core.testing.data.UserMother
import com.qodein.core.testing.fake.FakeAuthRepository
import com.qodein.core.testing.fake.FakeNotificationPermissionChecker
import com.qodein.core.testing.fake.FakePostSubmissionScheduler
import com.qodein.core.testing.fake.FakeUserRepository
import com.qodein.core.testing.rule.MainDispatcherRule
import com.qodein.core.testing.util.assertIsType
import com.qodein.shared.common.permission.NotificationPermissionState
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import com.qodein.shared.domain.usecase.post.EnqueuePostSubmissionUseCase
import com.qodein.shared.domain.usecase.user.GetUserByIdUseCase
import com.qodein.shared.model.Post
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PostSubmissionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val alice = UserMother.alice()
    private val authRepository = FakeAuthRepository()
    private val userRepository = FakeUserRepository().apply { users[alice.id.value] = alice }
    private val scheduler = FakePostSubmissionScheduler()
    private val permissionChecker = FakeNotificationPermissionChecker()

    private fun TestScope.viewModel(): PostSubmissionViewModel {
        val viewModel = PostSubmissionViewModel(
            application = ApplicationProvider.getApplicationContext<Application>(),
            getAuthStateUseCase = GetAuthStateUseCase(authRepository, backgroundScope),
            enqueuePostSubmissionUseCase = EnqueuePostSubmissionUseCase(scheduler, GetUserByIdUseCase(userRepository)),
            notificationPermissionChecker = permissionChecker,
        )
        runCurrent()
        return viewModel
    }

    @Test
    fun titleIsCutToMaxLength() =
        runTest {
            val viewModel = viewModel()

            viewModel.onAction(PostSubmissionAction.UpdateTitle("a".repeat(Post.TITLE_MAX_LENGTH + 5)))

            assertEquals(Post.TITLE_MAX_LENGTH, viewModel.uiState.value.title.length)
            assertTrue(viewModel.uiState.value.isTitleValid)
        }

    @Test
    fun tagInputIsFilteredWhileTyping() =
        runTest {
            val viewModel = viewModel()

            viewModel.onAction(PostSubmissionAction.UpdateTag("Coffee#1"))

            assertEquals("coffee1", viewModel.uiState.value.tagInput)
            assertTrue(viewModel.uiState.value.tags.isEmpty())
        }

    @Test
    fun spaceOrCommaCommitsTagOnce() =
        runTest {
            val viewModel = viewModel()

            viewModel.onAction(PostSubmissionAction.UpdateTag("Coffee "))
            viewModel.onAction(PostSubmissionAction.UpdateTag("coffee,"))
            viewModel.onAction(PostSubmissionAction.UpdateTag("tea "))

            assertEquals(listOf("coffee", "tea"), viewModel.uiState.value.tags)
            assertEquals("", viewModel.uiState.value.tagInput)

            viewModel.onAction(PostSubmissionAction.RemoveTag("coffee"))

            assertEquals(listOf("tea"), viewModel.uiState.value.tags)
        }

    @Test
    fun blankTitleCannotBeSubmitted() =
        runTest {
            val viewModel = viewModel()

            viewModel.onAction(PostSubmissionAction.UpdateTitle("   "))

            assertFalse(viewModel.uiState.value.canSubmit)
        }

    @Test
    fun submitWhileSignedInSchedulesPostAndNavigates() =
        runTest {
            authRepository.signIn(alice.id.value)
            val viewModel = viewModel()
            viewModel.onAction(PostSubmissionAction.UpdateTitle("Free coffee"))
            viewModel.onAction(PostSubmissionAction.UpdateContent("Use it before Friday"))
            viewModel.onAction(PostSubmissionAction.UpdateTag("coffee "))

            viewModel.events.test {
                viewModel.onAction(PostSubmissionAction.Submit)

                assertEquals(PostSubmissionEvent.PostSubmitted, awaitItem())
            }

            val submission = scheduler.submissions.single()
            assertEquals("Free coffee", submission.title)
            assertEquals("Use it before Friday", submission.content)
            assertEquals(listOf("coffee"), submission.tags)
            assertEquals("Alice", submission.authorName)
            assertTrue(viewModel.uiState.value.isSubmitting)
        }

    @Test
    fun submitWhileSignedOutDoesNothing() =
        runTest {
            val viewModel = viewModel()
            viewModel.onAction(PostSubmissionAction.UpdateTitle("Free coffee"))

            viewModel.onAction(PostSubmissionAction.Submit)
            runCurrent()

            assertTrue(scheduler.submissions.isEmpty())
            assertFalse(viewModel.uiState.value.isSubmitting)
        }

    @Test
    fun deniedNotificationPermissionAsksBeforeSubmitting() =
        runTest {
            authRepository.signIn(alice.id.value)
            permissionChecker.state = NotificationPermissionState.Denied
            val viewModel = viewModel()
            viewModel.onAction(PostSubmissionAction.UpdateTitle("Free coffee"))

            viewModel.events.test {
                viewModel.onAction(PostSubmissionAction.Submit)
                assertEquals(PostSubmissionEvent.RequestNotificationPermission, awaitItem())
                assertTrue(scheduler.submissions.isEmpty())

                viewModel.onPermissionResult(granted = true)
                assertEquals(PostSubmissionEvent.PostSubmitted, awaitItem())
            }
            assertEquals(1, scheduler.submissions.size)
        }

    @Test
    fun failedSchedulingShowsErrorAndAllowsRetry() =
        runTest {
            authRepository.signIn("ghost")
            val viewModel = viewModel()
            viewModel.onAction(PostSubmissionAction.UpdateTitle("Free coffee"))

            viewModel.events.test {
                viewModel.onAction(PostSubmissionAction.Submit)

                awaitItem().assertIsType<PostSubmissionEvent.ShowError>()
            }
            assertFalse(viewModel.uiState.value.isSubmitting)
            assertTrue(viewModel.uiState.value.canSubmit)
        }

    @Test
    fun pickImagesRequestsPicker() =
        runTest {
            val viewModel = viewModel()

            viewModel.events.test {
                viewModel.onAction(PostSubmissionAction.PickImages)

                assertEquals(PostSubmissionEvent.PickImagesRequested, awaitItem())
            }
        }
}
