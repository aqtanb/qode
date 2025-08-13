package com.qodein.core.domain.usecase.auth

import app.cash.turbine.test
import com.qodein.core.domain.AuthState
import com.qodein.core.domain.auth.AuthStateManager
import com.qodein.core.model.Email
import com.qodein.core.model.User
import com.qodein.core.model.UserId
import com.qodein.core.model.UserPreferences
import com.qodein.core.model.UserProfile
import com.qodein.core.model.UserStats
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [GetAuthStateUseCase] following Google's official Android testing style.
 *
 * Test naming convention: subjectUnderTest_actionOrInput_resultState
 * Example: invoke_whenAuthStateManagerEmitsAuthenticated_emitsSuccessResult
 */
class GetAuthStateUseCaseTest {

    @MockK
    private lateinit var authStateManager: AuthStateManager

    private lateinit var getAuthStateUseCase: GetAuthStateUseCase

    private val authenticatedUser = User(
        id = UserId("test-user-id"),
        email = Email("test@example.com"),
        profile = UserProfile.createSafe(
            username = "testuser",
            firstName = "Test",
            lastName = "User",
        ).getOrThrow(),
        stats = UserStats.initial(UserId("test-user-id")),
        preferences = UserPreferences.default(UserId("test-user-id")),
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        getAuthStateUseCase = GetAuthStateUseCase(authStateManager)
    }

    // invoke() - Success Cases Tests

    @Test
    fun invoke_whenAuthStateManagerEmitsAuthenticated_emitsSuccessResult() =
        runTest {
            // Given
            val authState = AuthState.Authenticated(authenticatedUser)
            every { authStateManager.getAuthState() } returns flowOf(authState)

            // When & Then
            getAuthStateUseCase().test {
                val result = awaitItem()
                assert(result.isSuccess)
                assert(result.getOrNull() == authState)
                awaitComplete()
            }

            verify { authStateManager.getAuthState() }
        }

    @Test
    fun invoke_whenAuthStateManagerEmitsUnauthenticated_emitsSuccessResult() =
        runTest {
            // Given
            val authState = AuthState.Unauthenticated
            every { authStateManager.getAuthState() } returns flowOf(authState)

            // When & Then
            getAuthStateUseCase().test {
                val result = awaitItem()
                assert(result.isSuccess)
                assert(result.getOrNull() == authState)
                awaitComplete()
            }

            verify { authStateManager.getAuthState() }
        }

    @Test
    fun invoke_whenAuthStateManagerEmitsLoading_emitsSuccessResult() =
        runTest {
            // Given
            val authState = AuthState.Loading
            every { authStateManager.getAuthState() } returns flowOf(authState)

            // When & Then
            getAuthStateUseCase().test {
                val result = awaitItem()
                assert(result.isSuccess)
                assert(result.getOrNull() == authState)
                awaitComplete()
            }

            verify { authStateManager.getAuthState() }
        }

    @Test
    fun invoke_whenAuthStateManagerEmitsMultipleStates_emitsAllSuccessResults() =
        runTest {
            // Given
            val states = listOf(
                AuthState.Loading,
                AuthState.Unauthenticated,
                AuthState.Authenticated(authenticatedUser),
            )
            every { authStateManager.getAuthState() } returns flowOf(*states.toTypedArray())

            // When & Then
            getAuthStateUseCase().test {
                states.forEach { expectedState ->
                    val result = awaitItem()
                    assert(result.isSuccess)
                    assert(result.getOrNull() == expectedState)
                }
                awaitComplete()
            }

            verify { authStateManager.getAuthState() }
        }

    // invoke() - Error Cases Tests

    @Test
    fun invoke_whenAuthStateManagerThrowsException_emitsFailureResult() =
        runTest {
            // Given
            val exception = RuntimeException("Auth state error")
            every { authStateManager.getAuthState() } returns flow { throw exception }

            // When & Then
            getAuthStateUseCase().test {
                val result = awaitItem()
                assert(result.isFailure)
                assert(result.exceptionOrNull() == exception)
                awaitComplete()
            }

            verify { authStateManager.getAuthState() }
        }

    @Test
    fun invoke_whenAuthStateManagerThrowsIOException_emitsFailureResult() =
        runTest {
            // Given
            val exception = java.io.IOException("Network error")
            every { authStateManager.getAuthState() } returns flow { throw exception }

            // When & Then
            getAuthStateUseCase().test {
                val result = awaitItem()
                assert(result.isFailure)
                assert(result.exceptionOrNull() == exception)
                awaitComplete()
            }

            verify { authStateManager.getAuthState() }
        }

    @Test
    fun invoke_whenAuthStateManagerThrowsSecurityException_emitsFailureResult() =
        runTest {
            // Given
            val exception = SecurityException("Access denied")
            every { authStateManager.getAuthState() } returns flow { throw exception }

            // When & Then
            getAuthStateUseCase().test {
                val result = awaitItem()
                assert(result.isFailure)
                assert(result.exceptionOrNull() == exception)
                awaitComplete()
            }

            verify { authStateManager.getAuthState() }
        }

    // invoke() - Mixed Success and Error Tests

    @Test
    fun invoke_whenAuthStateManagerEmitsSuccessThenThrowsException_emitsSuccessThenFailure() =
        runTest {
            // Given
            val authState = AuthState.Authenticated(authenticatedUser)
            val exception = RuntimeException("Subsequent error")
            every { authStateManager.getAuthState() } returns flow {
                emit(authState)
                throw exception
            }

            // When & Then
            getAuthStateUseCase().test {
                // First emission: success
                val firstResult = awaitItem()
                assert(firstResult.isSuccess)
                assert(firstResult.getOrNull() == authState)

                // Second emission: failure
                val secondResult = awaitItem()
                assert(secondResult.isFailure)
                assert(secondResult.exceptionOrNull() == exception)

                awaitComplete()
            }

            verify { authStateManager.getAuthState() }
        }

    // Result Pattern Tests

    @Test
    fun invoke_resultPattern_followsNIAConventions() =
        runTest {
            // Given
            val authState = AuthState.Authenticated(authenticatedUser)
            every { authStateManager.getAuthState() } returns flowOf(authState)

            // When & Then
            getAuthStateUseCase().test {
                val result = awaitItem()

                // Verify Result pattern properties
                assert(result.isSuccess)
                assert(!result.isFailure)
                assert(result.getOrNull() != null)
                assert(result.exceptionOrNull() == null)

                // Verify getOrElse works correctly
                val fallbackState = AuthState.Unauthenticated
                assert(result.getOrElse { fallbackState } == authState)

                awaitComplete()
            }

            verify { authStateManager.getAuthState() }
        }

    @Test
    fun invoke_resultPatternForFailure_followsNIAConventions() =
        runTest {
            // Given
            val exception = RuntimeException("Test error")
            every { authStateManager.getAuthState() } returns flow { throw exception }

            // When & Then
            getAuthStateUseCase().test {
                val result = awaitItem()

                // Verify Result pattern properties for failure
                assert(!result.isSuccess)
                assert(result.isFailure)
                assert(result.getOrNull() == null)
                assert(result.exceptionOrNull() == exception)

                // Verify getOrElse works correctly for failure
                val fallbackState = AuthState.Unauthenticated
                assert(result.getOrElse { fallbackState } == fallbackState)

                awaitComplete()
            }

            verify { authStateManager.getAuthState() }
        }

    // Dependency Injection Tests

    @Test
    fun getAuthStateUseCase_whenInstantiated_dependsOnAuthStateManager() {
        // Given & When
        val useCase = GetAuthStateUseCase(authStateManager)

        // Then - Should not throw any exceptions and should be properly initialized
        assert(useCase != null)
        // The actual dependency is verified through the other tests that call manager methods
    }

    // Performance Tests

    @Test
    fun invoke_whenCalledMultipleTimes_eachCallCreatesIndependentFlow() =
        runTest {
            // Given
            val authState = AuthState.Authenticated(authenticatedUser)
            every { authStateManager.getAuthState() } returns flowOf(authState)

            // When
            val flow1 = getAuthStateUseCase()
            val flow2 = getAuthStateUseCase()

            // Then - Each invocation should create a separate flow
            flow1.test {
                val result1 = awaitItem()
                assert(result1.isSuccess)
                assert(result1.getOrNull() == authState)
                awaitComplete()
            }

            flow2.test {
                val result2 = awaitItem()
                assert(result2.isSuccess)
                assert(result2.getOrNull() == authState)
                awaitComplete()
            }

            // Each call should invoke the manager
            verify(exactly = 2) { authStateManager.getAuthState() }
        }
}
