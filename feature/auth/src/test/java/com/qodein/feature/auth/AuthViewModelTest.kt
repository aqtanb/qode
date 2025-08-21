package com.qodein.feature.auth

import app.cash.turbine.test
import com.qodein.core.testing.data.TestUsers
import com.qodein.shared.domain.usecase.auth.SignInWithGoogleUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
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

/**
 * Comprehensive unit tests for AuthViewModel following enterprise patterns
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private lateinit var signInWithGoogleUseCase: SignInWithGoogleUseCase
    private lateinit var viewModel: AuthViewModel
    private lateinit var testDispatcher: TestDispatcher

    private val testUser = TestUsers.sampleUser

    @Before
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        signInWithGoogleUseCase = mockk()
        viewModel = AuthViewModel(signInWithGoogleUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // MARK: - Initial State Tests

    @Test
    fun init_startsWithIdleState() =
        runTest(testDispatcher) {
            // Then
            viewModel.state.test {
                val initialState = awaitItem()
                assertTrue(initialState is AuthUiState.Idle)
            }
        }

    // MARK: - Action Handling Tests

    @Test
    fun handleAction_whenSignInWithGoogleClicked_startsSignInFlow() =
        runTest(testDispatcher) {
            // Given
            every { signInWithGoogleUseCase() } returns flowOf(Result.success(testUser))

            // When
            viewModel.handleAction(AuthAction.SignInWithGoogleClicked)
            advanceUntilIdle()

            // Then
            verify { signInWithGoogleUseCase() }
        }

    @Test
    fun handleAction_whenRetryClicked_startsSignInFlow() =
        runTest(testDispatcher) {
            // Given
            every { signInWithGoogleUseCase() } returns flowOf(Result.success(testUser))

            // When
            viewModel.handleAction(AuthAction.RetryClicked)
            advanceUntilIdle()

            // Then
            verify { signInWithGoogleUseCase() }
        }

    @Test
    fun handleAction_whenTermsOfServiceClicked_emitsNavigationEvent() =
        runTest(testDispatcher) {
            // When & Then
            viewModel.events.test {
                viewModel.handleAction(AuthAction.TermsOfServiceClicked)
                advanceUntilIdle()

                val event = awaitItem()
                assertTrue(event is AuthEvent.TermsOfServiceRequested)
            }
        }

    @Test
    fun handleAction_whenPrivacyPolicyClicked_emitsNavigationEvent() =
        runTest(testDispatcher) {
            // When & Then
            viewModel.events.test {
                viewModel.handleAction(AuthAction.PrivacyPolicyClicked)
                advanceUntilIdle()

                val event = awaitItem()
                assertTrue(event is AuthEvent.PrivacyPolicyRequested)
            }
        }

    // MARK: - Sign-In Flow Tests

    @Test
    fun signInWithGoogle_whenSuccessful_emitsLoadingThenSuccessState() =
        runTest(testDispatcher) {
            // Given
            every { signInWithGoogleUseCase() } returns flowOf(Result.success(testUser))

            // When & Then
            viewModel.state.test {
                // Initial state
                val initialState = awaitItem()
                assertTrue(initialState is AuthUiState.Idle)

                // Trigger sign in
                viewModel.handleAction(AuthAction.SignInWithGoogleClicked)
                advanceUntilIdle()

                // Loading state
                val loadingState = awaitItem()
                assertTrue(loadingState is AuthUiState.Loading)

                // Success state
                val successState = awaitItem()
                assertTrue(successState is AuthUiState.Success)
                assertEquals(testUser, (successState as AuthUiState.Success).user)
            }
        }

    @Test
    fun signInWithGoogle_whenSuccessful_emitsNavigateToHomeEvent() =
        runTest(testDispatcher) {
            // Given
            every { signInWithGoogleUseCase() } returns flowOf(Result.success(testUser))

            // When & Then
            viewModel.events.test {
                viewModel.handleAction(AuthAction.SignInWithGoogleClicked)
                advanceUntilIdle()

                val event = awaitItem()
                assertTrue(event is AuthEvent.SignedIn)
            }
        }

    @Test
    fun signInWithGoogle_whenSecurityException_emitsNonRetryableError() =
        runTest(testDispatcher) {
            // Given
            val securityException = SecurityException("Sign-in was cancelled or rejected")
            every { signInWithGoogleUseCase() } returns flowOf(Result.failure(securityException))

            // When & Then
            viewModel.state.test {
                // Skip initial state
                awaitItem()

                viewModel.handleAction(AuthAction.SignInWithGoogleClicked)
                advanceUntilIdle()

                // Skip loading state
                awaitItem()

                // Error state
                val errorState = awaitItem()
                assertTrue(errorState is AuthUiState.Error)
                assertEquals(securityException, (errorState as AuthUiState.Error).exception)
                assertEquals(false, errorState.isRetryable) // SecurityException is not retryable
            }
        }

    @Test
    fun signInWithGoogle_whenIOException_emitsRetryableError() =
        runTest(testDispatcher) {
            // Given
            val ioException = IOException("Network error")
            every { signInWithGoogleUseCase() } returns flowOf(Result.failure(ioException))

            // When & Then
            viewModel.state.test {
                // Skip initial state
                awaitItem()

                viewModel.handleAction(AuthAction.SignInWithGoogleClicked)
                advanceUntilIdle()

                // Skip loading state
                awaitItem()

                // Error state
                val errorState = awaitItem()
                assertTrue(errorState is AuthUiState.Error)
                assertEquals(ioException, (errorState as AuthUiState.Error).exception)
                assertEquals(true, errorState.isRetryable) // IOException is retryable
            }
        }

    @Test
    fun signInWithGoogle_whenIllegalStateException_emitsRetryableError() =
        runTest(testDispatcher) {
            // Given
            val illegalStateException = IllegalStateException("Google Play Services unavailable")
            every { signInWithGoogleUseCase() } returns flowOf(Result.failure(illegalStateException))

            // When & Then
            viewModel.state.test {
                // Skip initial state
                awaitItem()

                viewModel.handleAction(AuthAction.SignInWithGoogleClicked)
                advanceUntilIdle()

                // Skip loading state
                awaitItem()

                // Error state
                val errorState = awaitItem()
                assertTrue(errorState is AuthUiState.Error)
                assertEquals(illegalStateException, (errorState as AuthUiState.Error).exception)
                assertEquals(true, errorState.isRetryable) // IllegalStateException is retryable
            }
        }

    @Test
    fun signInWithGoogle_whenGenericException_emitsRetryableError() =
        runTest(testDispatcher) {
            // Given
            val genericException = RuntimeException("Unknown error")
            every { signInWithGoogleUseCase() } returns flowOf(Result.failure(genericException))

            // When & Then
            viewModel.state.test {
                // Skip initial state
                awaitItem()

                viewModel.handleAction(AuthAction.SignInWithGoogleClicked)
                advanceUntilIdle()

                // Skip loading state
                awaitItem()

                // Error state
                val errorState = awaitItem()
                assertTrue(errorState is AuthUiState.Error)
                assertEquals(genericException, (errorState as AuthUiState.Error).exception)
                assertEquals(true, errorState.isRetryable) // Generic exceptions are retryable
            }
        }

    // MARK: - Error Handling and Recovery Tests

    @Test
    fun clearError_resetsStateToIdle() =
        runTest(testDispatcher) {
            // Given
            val exception = IOException("Network error")
            every { signInWithGoogleUseCase() } returns flowOf(Result.failure(exception))

            // When
            viewModel.state.test {
                // Skip initial state
                awaitItem()

                // Trigger error
                viewModel.handleAction(AuthAction.SignInWithGoogleClicked)
                advanceUntilIdle()

                // Skip loading state
                awaitItem()

                // Verify error state
                val errorState = awaitItem()
                assertTrue(errorState is AuthUiState.Error)

                // Clear error
                viewModel.clearError()
                advanceUntilIdle()

                // Verify idle state
                val idleState = awaitItem()
                assertTrue(idleState is AuthUiState.Idle)
            }
        }

    @Test
    fun retry_afterError_startsNewSignInFlow() =
        runTest(testDispatcher) {
            // Given
            val exception = SocketTimeoutException("Connection timeout")
            every { signInWithGoogleUseCase() } returnsMany listOf(
                flowOf(Result.failure(exception)),
                flowOf(Result.success(testUser)),
            )

            // When & Then
            viewModel.state.test {
                // Initial state
                awaitItem()

                // First attempt - fails
                viewModel.handleAction(AuthAction.SignInWithGoogleClicked)
                advanceUntilIdle()

                // Loading state
                awaitItem()

                // Error state
                val errorState = awaitItem()
                assertTrue(errorState is AuthUiState.Error)

                // Retry - succeeds
                viewModel.handleAction(AuthAction.RetryClicked)
                advanceUntilIdle()

                // Loading state again
                val loadingState = awaitItem()
                assertTrue(loadingState is AuthUiState.Loading)

                // Success state
                val successState = awaitItem()
                assertTrue(successState is AuthUiState.Success)
                assertEquals(testUser, (successState as AuthUiState.Success).user)
            }

            // Verify both calls were made
            verify(exactly = 2) { signInWithGoogleUseCase() }
        }

    // MARK: - Job Management Tests

    @Test
    fun signInWithGoogle_cancelsExistingJobBeforeStartingNew() =
        runTest(testDispatcher) {
            // Given
            every { signInWithGoogleUseCase() } returns flowOf(Result.success(testUser))

            viewModel.state.test {
                // Initial state
                val initialState = awaitItem()
                assertTrue(initialState is AuthUiState.Idle)

                // When - Trigger first sign in
                viewModel.handleAction(AuthAction.SignInWithGoogleClicked)
                // When - Immediately trigger second sign in
                viewModel.handleAction(AuthAction.SignInWithGoogleClicked)
                advanceUntilIdle()

                // Then - Only one final state change is expected after all work is done
                val firstLoading = awaitItem()
                assertTrue(firstLoading is AuthUiState.Loading)

                val finalSuccess = awaitItem()
                assertTrue(finalSuccess is AuthUiState.Success)
                assertEquals(testUser, (finalSuccess as AuthUiState.Success).user)

                // No other events should be found in the stream
                expectNoEvents()
            }

            verify(atLeast = 1) { signInWithGoogleUseCase() }
        }

    // MARK: - State Transition Tests

    @Test
    fun stateTransition_idleToLoadingToSuccessWithEvent() =
        runTest(testDispatcher) {
            // Given
            every { signInWithGoogleUseCase() } returns flowOf(Result.success(testUser))

            // Test both state and events in parallel
            viewModel.state.test {
                val initialState = awaitItem()
                assertTrue(initialState is AuthUiState.Idle)

                // Start testing events in parallel
                val eventTurbine = viewModel.events.testIn(this@runTest)

                viewModel.handleAction(AuthAction.SignInWithGoogleClicked)
                advanceUntilIdle()

                // State transitions
                val loadingState = awaitItem()
                assertTrue(loadingState is AuthUiState.Loading)

                val successState = awaitItem()
                assertTrue(successState is AuthUiState.Success)

                // Verify event was emitted
                val event = eventTurbine.awaitItem()
                assertTrue(event is AuthEvent.SignedIn)

                eventTurbine.cancel()
            }
        }

    @Test
    fun stateTransition_idleToLoadingToErrorWithRetryability() =
        runTest(testDispatcher) {
            // Given
            val networkException = SocketTimeoutException("Timeout")
            every { signInWithGoogleUseCase() } returns flowOf(Result.failure(networkException))

            // When & Then
            viewModel.state.test {
                // Initial state
                val initialState = awaitItem()
                assertTrue(initialState is AuthUiState.Idle)

                viewModel.handleAction(AuthAction.SignInWithGoogleClicked)
                advanceUntilIdle()

                // Loading state
                val loadingState = awaitItem()
                assertTrue(loadingState is AuthUiState.Loading)

                // Error state with correct retryability
                val errorState = awaitItem()
                assertTrue(errorState is AuthUiState.Error)
                assertEquals(networkException, (errorState as AuthUiState.Error).exception)
                assertEquals(true, errorState.isRetryable)
            }
        }

    // MARK: - Edge Cases and Resilience Tests

    @Test
    fun multipleQuickActions_handleGracefully() =
        runTest(testDispatcher) {
            // Given
            every { signInWithGoogleUseCase() } returns flowOf(Result.success(testUser))

            // When - Multiple rapid actions
            repeat(5) {
                viewModel.handleAction(AuthAction.SignInWithGoogleClicked)
                viewModel.handleAction(AuthAction.RetryClicked)
            }
            advanceUntilIdle()

            // Then - Should handle gracefully (at least one call made)
            verify(atLeast = 1) { signInWithGoogleUseCase() }
        }

    @Test
    fun clearError_fromNonErrorState_noSideEffects() =
        runTest(testDispatcher) {
            // Given
            every { signInWithGoogleUseCase() } returns flowOf(Result.success(testUser))

            // When
            viewModel.clearError() // Clear error when not in error state
            advanceUntilIdle()

            // Then - Should remain in idle state
            viewModel.state.test {
                val state = awaitItem()
                assertTrue(state is AuthUiState.Idle)
            }
        }
}
