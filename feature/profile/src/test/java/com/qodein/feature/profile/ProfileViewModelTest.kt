package com.qodein.feature.profile

import app.cash.turbine.test
import com.qodein.core.analytics.AnalyticsHelper
import com.qodein.core.testing.data.TestUsers
import com.qodein.shared.common.result.ErrorType
import com.qodein.shared.common.result.Result
import com.qodein.shared.domain.AuthState
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import com.qodein.shared.domain.usecase.auth.SignOutUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
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

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private lateinit var getAuthStateUseCase: GetAuthStateUseCase
    private lateinit var signOutUseCase: SignOutUseCase
    private lateinit var analyticsHelper: AnalyticsHelper
    private lateinit var viewModel: ProfileViewModel
    private lateinit var testDispatcher: TestDispatcher

    private val testUser = TestUsers.sampleUser

    @Before
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)

        getAuthStateUseCase = mockk()
        signOutUseCase = mockk()
        analyticsHelper = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_whenUserAuthenticated_emitsSuccessState() =
        runTest {
            every { getAuthStateUseCase() } returns flowOf(Result.Success(AuthState.Authenticated(testUser)))

            viewModel = ProfileViewModel(getAuthStateUseCase, signOutUseCase, analyticsHelper)
            advanceUntilIdle()

            val finalState = viewModel.state.value
            assertTrue(finalState is ProfileUiState.Success)
            assertEquals(testUser, (finalState as ProfileUiState.Success).user)
        }

    @Test
    fun init_whenUserUnauthenticated_emitsErrorState() =
        runTest {
            every { getAuthStateUseCase() } returns flowOf(Result.Success(AuthState.Unauthenticated))

            viewModel = ProfileViewModel(getAuthStateUseCase, signOutUseCase, analyticsHelper)
            advanceUntilIdle()

            val finalState = viewModel.state.value
            assertTrue(finalState is ProfileUiState.Error)
            assertEquals(ErrorType.SERVICE_UNAVAILABLE_GENERAL, (finalState as ProfileUiState.Error).errorType)
        }

    @Test
    fun init_whenAuthStateFails_emitsCorrectStateTransition() =
        runTest {
            val testException = RuntimeException("Network error")
            every { getAuthStateUseCase() } returns flow { emit(Result.Error(testException)) }

            val localViewModel = ProfileViewModel(getAuthStateUseCase, signOutUseCase, analyticsHelper)

            localViewModel.state.test {
                assertEquals(ProfileUiState.Loading, awaitItem())
                advanceUntilIdle()
                val errorState = awaitItem() as ProfileUiState.Error
                assertTrue(errorState is ProfileUiState.Error)
                assertEquals(ErrorType.NETWORK_GENERAL, errorState.errorType)
            }
        }

    @Test
    fun handleAction_whenRetryClicked_callsAuthStateManager() =
        runTest {
            every { getAuthStateUseCase() } returns flowOf(Result.Success(AuthState.Authenticated(testUser)))
            viewModel = ProfileViewModel(getAuthStateUseCase, signOutUseCase, analyticsHelper)
            advanceUntilIdle()

            viewModel.handleAction(ProfileAction.RetryClicked)
            advanceUntilIdle()

            verify(atLeast = 2) { getAuthStateUseCase() }
        }

    @Test
    fun handleAction_whenSignOutSucceeds_emitsNavigationEvent() =
        runTest {
            every { getAuthStateUseCase() } returns flowOf(Result.Success(AuthState.Authenticated(testUser)))
            every { signOutUseCase() } returns flowOf(Result.Success(Unit))
            viewModel = ProfileViewModel(getAuthStateUseCase, signOutUseCase, analyticsHelper)
            advanceUntilIdle()

            viewModel.events.test {
                viewModel.handleAction(ProfileAction.SignOutClicked)
                advanceUntilIdle()
                assertEquals(ProfileEvent.SignedOut, awaitItem())
                verify { signOutUseCase() }
            }
        }

    @Test
    fun handleAction_whenSignOutFails_showsErrorAndRestartsAuthMonitoring() =
        runTest {
            // Given
            val testException = IOException("Network connection failed")
            every { getAuthStateUseCase() } returns flowOf(Result.Success(AuthState.Authenticated(testUser)))
            every { signOutUseCase() } returns flowOf(Result.Error(testException))
            viewModel = ProfileViewModel(getAuthStateUseCase, signOutUseCase, analyticsHelper)
            advanceUntilIdle()

            // When & Then
            viewModel.state.test {
                // 1. Initial state is Success
                assertTrue(awaitItem() is ProfileUiState.Success)

                // 2. Action is dispatched
                viewModel.handleAction(ProfileAction.SignOutClicked)

                // 3. State becomes Loading during the sign-out attempt
                assertTrue(awaitItem() is ProfileUiState.Loading)

                // 4. State becomes Error because the use case fails
                val errorState = awaitItem() as ProfileUiState.Error
                assertTrue(errorState is ProfileUiState.Error)
                assertEquals(ErrorType.NETWORK_GENERAL, errorState.errorType)

                // 5. Run the recovery logic (checkAuthState)
                advanceUntilIdle()

                // 6. The state recovers to Success. The intermediate Loading state is
                //    conflated by the StateFlow and is not asserted here.
                val recoveredState = awaitItem() as ProfileUiState.Success
                assertTrue(recoveredState is ProfileUiState.Success)
                assertEquals(testUser, recoveredState.user)
            }

            verify { signOutUseCase() }
            verify(atLeast = 2) { getAuthStateUseCase() }
        }

    @Test
    fun handleAction_whenEditProfileClicked_emitsNavigateToEditProfileEvent() =
        runTest {
            every { getAuthStateUseCase() } returns flowOf(Result.Success(AuthState.Authenticated(testUser)))
            viewModel = ProfileViewModel(getAuthStateUseCase, signOutUseCase, analyticsHelper)
            advanceUntilIdle()

            viewModel.events.test {
                viewModel.handleAction(ProfileAction.EditProfileClicked)
                assertEquals(ProfileEvent.EditProfileRequested, awaitItem())
            }
        }

    @Test
    fun handleAction_whenAchievementsClicked_emitsNavigateToAchievementsEvent() =
        runTest {
            every { getAuthStateUseCase() } returns flowOf(Result.Success(AuthState.Authenticated(testUser)))
            viewModel = ProfileViewModel(getAuthStateUseCase, signOutUseCase, analyticsHelper)
            advanceUntilIdle()

            viewModel.events.test {
                viewModel.handleAction(ProfileAction.AchievementsClicked)
                assertEquals(ProfileEvent.AchievementsRequested, awaitItem())
            }
        }

    @Test
    fun handleAction_whenUserJourneyClicked_emitsNavigateToUserJourneyEvent() =
        runTest {
            every { getAuthStateUseCase() } returns flowOf(Result.Success(AuthState.Authenticated(testUser)))
            viewModel = ProfileViewModel(getAuthStateUseCase, signOutUseCase, analyticsHelper)
            advanceUntilIdle()

            viewModel.events.test {
                viewModel.handleAction(ProfileAction.UserJourneyClicked)
                assertEquals(ProfileEvent.UserJourneyRequested, awaitItem())
            }
        }

    @Test
    fun stateTransition_errorToLoadingToSuccessOnRetry() =
        runTest {
            val errorFlow = flow<Result<AuthState>> { emit(Result.Error(IOException("Network error"))) }
            val successFlow = flowOf(Result.Success(AuthState.Authenticated(testUser)))
            every { getAuthStateUseCase() }.returns(errorFlow).andThen(successFlow)

            val localViewModel = ProfileViewModel(getAuthStateUseCase, signOutUseCase, analyticsHelper)

            localViewModel.state.test {
                assertEquals(ProfileUiState.Loading, awaitItem())
                advanceUntilIdle()
                assertTrue(awaitItem() is ProfileUiState.Error)

                localViewModel.handleAction(ProfileAction.RetryClicked)

                assertEquals(ProfileUiState.Loading, awaitItem())
                advanceUntilIdle()
                val successState = awaitItem()
                assertTrue(successState is ProfileUiState.Success)
                assertEquals(testUser, (successState as ProfileUiState.Success).user)
            }
        }
}
