package com.qodein.feature.profile

import app.cash.turbine.test
import com.qodein.core.domain.AuthState
import com.qodein.core.domain.auth.AuthStateManager
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
import kotlinx.coroutines.flow.flow
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
import java.io.IOException
import java.net.SocketTimeoutException

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private lateinit var authStateManager: AuthStateManager
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
            isGenerated = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        ),
        stats = UserStats.initial(UserId("test_user_id")),
        preferences = UserPreferences.default(UserId("test_user_id")),
    )

    @Before
    fun setUp() {
        testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)

        authStateManager = mockk()
        signOutUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_whenUserAuthenticated_emitsSuccessState() =
        runTest {
            // Given
            every { authStateManager.getAuthState() } returns flowOf(AuthState.Authenticated(testUser))

            // When
            viewModel = ProfileViewModel(authStateManager, signOutUseCase)
            advanceUntilIdle()

            // Then
            viewModel.state.test {
                val state = awaitItem()
                assertTrue(state is ProfileUiState.Success)
                assertEquals(testUser, (state as ProfileUiState.Success).user)
            }
        }

    @Test
    fun init_whenUserUnauthenticated_emitsErrorState() =
        runTest {
            // Given
            every { authStateManager.getAuthState() } returns flowOf(AuthState.Unauthenticated)

            // When
            viewModel = ProfileViewModel(authStateManager, signOutUseCase)
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
    fun init_whenAuthStateFails_emitsErrorState() =
        runTest {
            // Given
            val testException = RuntimeException("Network error")
            every { authStateManager.getAuthState() } returns flow { throw testException }

            // When
            viewModel = ProfileViewModel(authStateManager, signOutUseCase)
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
    fun handleAction_whenRetryClicked_callsAuthStateManager() =
        runTest {
            // Given
            every { authStateManager.getAuthState() } returns flowOf(AuthState.Authenticated(testUser))
            viewModel = ProfileViewModel(authStateManager, signOutUseCase)
            advanceUntilIdle()

            // When
            viewModel.handleAction(ProfileAction.RetryClicked)
            advanceUntilIdle()

            // Then
            verify(atLeast = 2) { authStateManager.getAuthState() } // init + retry
        }

    @Test
    fun handleAction_whenSignOutSucceeds_emitsNavigationEvent() =
        runTest {
            // Given
            every { authStateManager.getAuthState() } returns flowOf(AuthState.Authenticated(testUser))
            every { signOutUseCase() } returns flowOf(Result.success(Unit))
            viewModel = ProfileViewModel(authStateManager, signOutUseCase)
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
            every { authStateManager.getAuthState() } returns flowOf(AuthState.Authenticated(testUser))
            every { signOutUseCase() } returns flowOf(Result.failure(testException))
            viewModel = ProfileViewModel(authStateManager, signOutUseCase)
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
            verify(atLeast = 2) { authStateManager.getAuthState() } // init + restart after failure
        }

    // MARK: - Navigation Events Tests

    @Test
    fun handleAction_whenEditProfileClicked_emitsNavigateToEditProfileEvent() =
        runTest {
            // Given
            every { authStateManager.getAuthState() } returns flowOf(AuthState.Authenticated(testUser))
            viewModel = ProfileViewModel(authStateManager, signOutUseCase)
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
            every { authStateManager.getAuthState() } returns flowOf(AuthState.Authenticated(testUser))
            viewModel = ProfileViewModel(authStateManager, signOutUseCase)
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
            every { authStateManager.getAuthState() } returns flowOf(AuthState.Authenticated(testUser))
            viewModel = ProfileViewModel(authStateManager, signOutUseCase)
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
            every { authStateManager.getAuthState() } returns flowOf(AuthState.Authenticated(testUser))
            every { signOutUseCase() } returns flowOf(Result.success(Unit))
            viewModel = ProfileViewModel(authStateManager, signOutUseCase)

            // When & Then
            viewModel.state.test {
                // Initial success state
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
            every { authStateManager.getAuthState() } returnsMany listOf(
                flow { throw exception },
                flowOf(AuthState.Authenticated(testUser)),
            )
            viewModel = ProfileViewModel(authStateManager, signOutUseCase)

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

    // MARK: - Integration Tests

    @Test
    fun fullUserJourney_fromLoadingToSuccessToSignOut() =
        runTest {
            // Given
            every { authStateManager.getAuthState() } returns flowOf(AuthState.Authenticated(testUser))
            every { signOutUseCase() } returns flowOf(Result.success(Unit))
            viewModel = ProfileViewModel(authStateManager, signOutUseCase)
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
            verify(atLeast = 1) { authStateManager.getAuthState() }
            verify(atLeast = 1) { signOutUseCase() }
        }
}
