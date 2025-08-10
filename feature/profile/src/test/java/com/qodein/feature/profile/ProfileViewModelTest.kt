package com.qodein.feature.profile

import app.cash.turbine.test
import com.qodein.core.domain.usecase.auth.GetCurrentUserUseCase
import com.qodein.core.domain.usecase.auth.SignOutUseCase
import com.qodein.core.model.Email
import com.qodein.core.model.User
import com.qodein.core.model.UserId
import com.qodein.core.model.UserPreferences
import com.qodein.core.model.UserProfile
import com.qodein.core.model.UserStats
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private lateinit var getCurrentUserUseCase: GetCurrentUserUseCase
    private lateinit var signOutUseCase: SignOutUseCase
    private lateinit var viewModel: ProfileViewModel
    private lateinit var testDispatcher: TestDispatcher

    private val testUser = User(
        id = UserId("test_user_id"),
        email = Email("john.doe@example.com"),
        profile = UserProfile(
            username = "johndoe",
            firstName = "John",
            lastName = "Doe",
            bio = "Test user",
            photoUrl = "https://example.com/profile.jpg",
            birthday = null,
            gender = null,
        ),
        stats = UserStats.initial(UserId("test_user_id")),
        preferences = UserPreferences.default(UserId("test_user_id")),
    )

    @Before
    fun setUp() {
        testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)

        getCurrentUserUseCase = mockk()
        signOutUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_whenGetCurrentUserSucceeds_emitsSuccessState() =
        runTest {
            // Given
            every { getCurrentUserUseCase() } returns flowOf(Result.success(testUser))

            // When
            viewModel = ProfileViewModel(getCurrentUserUseCase, signOutUseCase)
            advanceUntilIdle()

            // Then
            viewModel.state.test {
                val state = awaitItem()
                assertTrue(state is ProfileUiState.Success)
                assertEquals(testUser, (state as ProfileUiState.Success).user)
            }
        }

    @Test
    fun init_whenGetCurrentUserReturnsNull_emitsErrorState() =
        runTest {
            // Given
            every { getCurrentUserUseCase() } returns flowOf(Result.success(null))

            // When
            viewModel = ProfileViewModel(getCurrentUserUseCase, signOutUseCase)
            advanceUntilIdle()

            // Then
            viewModel.state.test {
                val state = awaitItem()
                assertTrue(state is ProfileUiState.Error)
                assertTrue((state as ProfileUiState.Error).isRetryable)
                assertTrue(state.exception is IllegalStateException)
            }
        }

    @Test
    fun init_whenGetCurrentUserFails_emitsErrorState() =
        runTest {
            // Given
            val testException = RuntimeException("Network error")
            every { getCurrentUserUseCase() } returns flowOf(Result.failure(testException))

            // When
            viewModel = ProfileViewModel(getCurrentUserUseCase, signOutUseCase)
            advanceUntilIdle()

            // Then
            viewModel.state.test {
                val state = awaitItem()
                assertTrue(state is ProfileUiState.Error)
                assertTrue((state as ProfileUiState.Error).isRetryable)
                assertEquals(testException, state.exception)
            }
        }

    @Test
    fun handleAction_whenRetryClicked_callsGetCurrentUser() =
        runTest {
            // Given
            every { getCurrentUserUseCase() } returns flowOf(Result.success(testUser))
            viewModel = ProfileViewModel(getCurrentUserUseCase, signOutUseCase)
            advanceUntilIdle()

            // When
            viewModel.handleAction(ProfileAction.RetryClicked)
            advanceUntilIdle()

            // Then
            verify(atLeast = 2) { getCurrentUserUseCase() } // init + retry
        }

    @Test
    fun handleAction_whenSignOutSucceeds_callsSignOutUseCase() =
        runTest {
            // Given
            every { getCurrentUserUseCase() } returns flowOf(Result.success(testUser))
            every { signOutUseCase() } returns flowOf(Result.success(Unit))
            viewModel = ProfileViewModel(getCurrentUserUseCase, signOutUseCase)
            advanceUntilIdle()

            // When
            viewModel.handleAction(ProfileAction.SignOutClicked)
            advanceUntilIdle()

            // Then
            verify { signOutUseCase() }
        }

    @Test
    fun handleAction_whenSignOutFails_emitsErrorState() =
        runTest {
            // Given
            val testException = RuntimeException("Sign out failed")
            every { getCurrentUserUseCase() } returns flowOf(Result.success(testUser))
            every { signOutUseCase() } returns flowOf(Result.failure(testException))
            viewModel = ProfileViewModel(getCurrentUserUseCase, signOutUseCase)
            advanceUntilIdle()

            // When
            viewModel.handleAction(ProfileAction.SignOutClicked)
            advanceUntilIdle()

            // Then
            verify { signOutUseCase() }
            viewModel.state.test {
                val state = awaitItem()
                assertTrue(state is ProfileUiState.Error)
                assertTrue((state as ProfileUiState.Error).isRetryable)
                assertEquals(testException, state.exception)
            }
        }
}
