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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException

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
    fun handleAction_whenSignOutSucceeds_emitsNavigationEvent() =
        runTest {
            // Given
            every { getCurrentUserUseCase() } returns flowOf(Result.success(testUser))
            every { signOutUseCase() } returns flowOf(Result.success(Unit))
            viewModel = ProfileViewModel(getCurrentUserUseCase, signOutUseCase)
            advanceUntilIdle()

            // When & Then - collect events before triggering action
            viewModel.events.test {
                viewModel.handleAction(ProfileAction.SignOutClicked)
                advanceUntilIdle()

                val event = awaitItem()
                assertEquals(ProfileEvent.NavigateToSignOut, event)

                verify { signOutUseCase() }
            }
        }

    @Test
    fun handleAction_whenSignOutFails_showsErrorAndRestartsAuthMonitoring() =
        runTest {
            // Given
            val testException = IOException("Network connection failed")
            every { getCurrentUserUseCase() } returns flowOf(Result.success(testUser))
            every { signOutUseCase() } returns flowOf(Result.failure(testException))
            viewModel = ProfileViewModel(getCurrentUserUseCase, signOutUseCase)
            advanceUntilIdle()

            // When
            viewModel.state.test {
                // Skip initial success state
                awaitItem()

                viewModel.handleAction(ProfileAction.SignOutClicked)
                advanceUntilIdle()

                // Then - should show loading then success (auth restarted)
                val loadingState = awaitItem()
                assertTrue(loadingState is ProfileUiState.Loading)

                val successState = awaitItem()
                assertTrue(successState is ProfileUiState.Success)
                assertEquals(testUser, (successState as ProfileUiState.Success).user)
            }

            // Verify sign out was attempted and auth monitoring restarted
            verify { signOutUseCase() }
            verify(atLeast = 2) { getCurrentUserUseCase() } // init + restart after failure
        }

    // MARK: - Navigation Events Tests

    @Test
    fun handleAction_whenEditProfileClicked_emitsNavigateToEditProfileEvent() =
        runTest {
            // Given
            every { getCurrentUserUseCase() } returns flowOf(Result.success(testUser))
            viewModel = ProfileViewModel(getCurrentUserUseCase, signOutUseCase)
            advanceUntilIdle()

            // When & Then
            viewModel.events.test {
                viewModel.handleAction(ProfileAction.EditProfileClicked)
                advanceUntilIdle()

                val event = awaitItem()
                assertEquals(ProfileEvent.NavigateToEditProfile, event)
            }
        }

    @Test
    fun handleAction_whenAchievementsClicked_emitsNavigateToAchievementsEvent() =
        runTest {
            // Given
            every { getCurrentUserUseCase() } returns flowOf(Result.success(testUser))
            viewModel = ProfileViewModel(getCurrentUserUseCase, signOutUseCase)
            advanceUntilIdle()

            // When & Then
            viewModel.events.test {
                viewModel.handleAction(ProfileAction.AchievementsClicked)
                advanceUntilIdle()

                val event = awaitItem()
                assertEquals(ProfileEvent.NavigateToAchievements, event)
            }
        }

    @Test
    fun handleAction_whenUserJourneyClicked_emitsNavigateToUserJourneyEvent() =
        runTest {
            // Given
            every { getCurrentUserUseCase() } returns flowOf(Result.success(testUser))
            viewModel = ProfileViewModel(getCurrentUserUseCase, signOutUseCase)
            advanceUntilIdle()

            // When & Then
            viewModel.events.test {
                viewModel.handleAction(ProfileAction.UserJourneyClicked)
                advanceUntilIdle()

                val event = awaitItem()
                assertEquals(ProfileEvent.NavigateToUserJourney, event)
            }
        }

    // MARK: - State Transitions Tests

    @Test
    fun stateTransition_loadingToSuccessToLoadingDuringSignOut() =
        runTest {
            // Given
            every { getCurrentUserUseCase() } returns flowOf(Result.success(testUser))
            every { signOutUseCase() } returns flowOf(Result.success(Unit))
            viewModel = ProfileViewModel(getCurrentUserUseCase, signOutUseCase)

            // When & Then
            viewModel.state.test {
                // Initial loading then success
                var state = awaitItem()
                assertTrue(state is ProfileUiState.Success)
                assertEquals(testUser, (state as ProfileUiState.Success).user)

                // Trigger sign out
                viewModel.handleAction(ProfileAction.SignOutClicked)
                advanceUntilIdle()

                // Should show loading during sign out
                state = awaitItem()
                assertTrue(state is ProfileUiState.Loading)

                // No further states since navigation happens
                expectNoEvents()
            }
        }

    @Test
    fun stateTransition_errorToLoadingToSuccessOnRetry() =
        runTest {
            // Given - first call fails, second succeeds
            val exception = SocketTimeoutException("Connection timeout")
            every { getCurrentUserUseCase() } returnsMany listOf(
                flowOf(Result.failure(exception)),
                flowOf(Result.success(testUser)),
            )
            viewModel = ProfileViewModel(getCurrentUserUseCase, signOutUseCase)

            // When & Then
            viewModel.state.test {
                // Initial error state
                var state = awaitItem()
                assertTrue(state is ProfileUiState.Error)
                assertEquals(exception, (state as ProfileUiState.Error).exception)

                // Retry action
                viewModel.handleAction(ProfileAction.RetryClicked)
                advanceUntilIdle()

                // Should show loading then success
                state = awaitItem()
                assertTrue(state is ProfileUiState.Loading)

                state = awaitItem()
                assertTrue(state is ProfileUiState.Success)
                assertEquals(testUser, (state as ProfileUiState.Success).user)
            }
        }

    // MARK: - Job Cancellation Tests

    @Test
    fun signOut_cancelsAuthJobToPreventRaceConditions() =
        runTest {
            // Given - slow auth flow that would normally emit null during sign out
            every { getCurrentUserUseCase() } returns flow {
                emit(Result.success(testUser))
                delay(100) // Simulate delay
                emit(Result.success(null)) // This would normally cause error
            }
            every { signOutUseCase() } returns flowOf(Result.success(Unit))

            viewModel = ProfileViewModel(getCurrentUserUseCase, signOutUseCase)
            advanceUntilIdle()

            // When
            viewModel.state.test {
                // Skip initial success state
                awaitItem()

                viewModel.handleAction(ProfileAction.SignOutClicked)

                // Should only see loading state, no error from cancelled auth job
                val loadingState = awaitItem()
                assertTrue(loadingState is ProfileUiState.Loading)

                advanceTimeBy(200) // Let the cancelled job "complete"
                expectNoEvents() // No error state should appear
            }
        }

    @Test
    fun retryAction_cancelsExistingAuthJobBeforeStartingNew() =
        runTest {
            // Given - first auth call is slow, second is fast
            every { getCurrentUserUseCase() } returnsMany listOf(
                flow {
                    emit(Result.success(testUser))
                    delay(1000) // Very slow
                    emit(Result.success(testUser))
                },
                flowOf(Result.success(testUser)), // Fast retry
            )

            viewModel = ProfileViewModel(getCurrentUserUseCase, signOutUseCase)
            advanceUntilIdle()

            // When - immediately retry without waiting for first to complete
            viewModel.state.test {
                awaitItem() // Initial success

                viewModel.handleAction(ProfileAction.RetryClicked)

                val loadingState = awaitItem()
                assertTrue(loadingState is ProfileUiState.Loading)

                val successState = awaitItem()
                assertTrue(successState is ProfileUiState.Success)

                // Advance time to let the cancelled job "complete"
                advanceTimeBy(2000)
                expectNoEvents() // No duplicate events from cancelled job
            }
        }

    // MARK: - Error Handling Tests

    @Test
    fun multipleErrorTypes_handledAppropriately() =
        runTest {
            val testCases = listOf(
                IOException("Network error"),
                IllegalStateException("Invalid state"),
                SecurityException("Authentication failed"),
                RuntimeException("Unknown error"),
            )

            testCases.forEach { exception ->
                // Given
                every { getCurrentUserUseCase() } returns flowOf(Result.failure(exception))
                val testViewModel = ProfileViewModel(getCurrentUserUseCase, signOutUseCase)

                // When & Then
                testViewModel.state.test {
                    val state = awaitItem()
                    assertTrue("Failed for $exception", state is ProfileUiState.Error)
                    val errorState = state as ProfileUiState.Error
                    assertEquals(exception, errorState.exception)
                    assertTrue(errorState.isRetryable)
                }
            }
        }

    // MARK: - Concurrent Operations Tests

    @Test
    fun concurrentActions_handledCorrectly() =
        runTest {
            // Given
            every { getCurrentUserUseCase() } returns flowOf(Result.success(testUser))
            every { signOutUseCase() } returns flowOf(Result.success(Unit))
            viewModel = ProfileViewModel(getCurrentUserUseCase, signOutUseCase)
            advanceUntilIdle()

            // When - multiple rapid actions
            viewModel.events.test {
                viewModel.handleAction(ProfileAction.EditProfileClicked)
                viewModel.handleAction(ProfileAction.AchievementsClicked)
                viewModel.handleAction(ProfileAction.SignOutClicked)
                advanceUntilIdle()

                // Then - all events should be emitted in order
                assertEquals(ProfileEvent.NavigateToEditProfile, awaitItem())
                assertEquals(ProfileEvent.NavigateToAchievements, awaitItem())
                assertEquals(ProfileEvent.NavigateToSignOut, awaitItem())
            }
        }

    // MARK: - Integration Tests

    @Test
    fun fullUserJourney_fromLoadingToSuccessToSignOut() =
        runTest {
            // Given
            every { getCurrentUserUseCase() } returns flowOf(Result.success(testUser))
            every { signOutUseCase() } returns flowOf(Result.success(Unit))
            viewModel = ProfileViewModel(getCurrentUserUseCase, signOutUseCase)
            advanceUntilIdle()

            // Then - verify state flow
            viewModel.state.test {
                val successState = awaitItem()
                assertTrue(successState is ProfileUiState.Success)
                assertEquals(testUser, (successState as ProfileUiState.Success).user)

                // Trigger sign out and verify loading state
                viewModel.handleAction(ProfileAction.SignOutClicked)
                advanceUntilIdle()

                val loadingState = awaitItem()
                assertTrue(loadingState is ProfileUiState.Loading)

                expectNoEvents() // No further states since navigation happens
            }

            // Verify navigation event separately
            viewModel.events.test {
                viewModel.handleAction(ProfileAction.SignOutClicked)
                advanceUntilIdle()

                val event = awaitItem()
                assertEquals(ProfileEvent.NavigateToSignOut, event)
            }

            // Verify proper use case calls
            verify(atLeast = 1) { getCurrentUserUseCase() }
            verify(atLeast = 1) { signOutUseCase() }
        }
}
