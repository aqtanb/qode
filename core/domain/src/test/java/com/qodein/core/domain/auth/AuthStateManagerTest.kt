package com.qodein.core.domain.auth

import app.cash.turbine.test
import com.qodein.core.domain.AuthState
import com.qodein.core.domain.repository.AuthRepository
import com.qodein.core.testing.data.TestUsers
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [AuthStateManager] following Google's official Android testing style.
 *
 * Test naming convention: subjectUnderTest_actionOrInput_resultState
 * Example: getAuthState_whenUserAuthenticated_emitsAuthenticatedState
 */
class AuthStateManagerTest {

    @MockK
    private lateinit var authRepository: AuthRepository

    private lateinit var authStateManager: AuthStateManager

    private val authenticatedUser = TestUsers.sampleUser

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        authStateManager = AuthStateManager(authRepository)
    }

    // getAuthState Tests

    @Test
    fun getAuthState_whenUserAuthenticated_emitsAuthenticatedState() =
        runTest {
            // Given
            every { authRepository.getAuthStateFlow() } returns flowOf(authenticatedUser)

            // When & Then
            authStateManager.getAuthState().test {
                val emittedState = awaitItem()
                assert(emittedState is AuthState.Authenticated)
                assert((emittedState as AuthState.Authenticated).user == authenticatedUser)
                awaitComplete()
            }

            verify { authRepository.getAuthStateFlow() }
        }

    @Test
    fun getAuthState_whenUserUnauthenticated_emitsUnauthenticatedState() =
        runTest {
            // Given
            every { authRepository.getAuthStateFlow() } returns flowOf(null)

            // When & Then
            authStateManager.getAuthState().test {
                val emittedState = awaitItem()
                assert(emittedState == AuthState.Unauthenticated)
                awaitComplete()
            }

            verify { authRepository.getAuthStateFlow() }
        }

    @Test
    fun getAuthState_whenUserChangesFromUnauthenticatedToAuthenticated_emitsCorrectSequence() =
        runTest {
            // Given
            every { authRepository.getAuthStateFlow() } returns flowOf(null, authenticatedUser)

            // When & Then
            authStateManager.getAuthState().test {
                // First emission: unauthenticated
                val firstState = awaitItem()
                assert(firstState == AuthState.Unauthenticated)

                // Second emission: authenticated
                val secondState = awaitItem()
                assert(secondState is AuthState.Authenticated)
                assert((secondState as AuthState.Authenticated).user == authenticatedUser)

                awaitComplete()
            }

            verify { authRepository.getAuthStateFlow() }
        }

    @Test
    fun getAuthState_whenUserChangesFromAuthenticatedToUnauthenticated_emitsCorrectSequence() =
        runTest {
            // Given
            every { authRepository.getAuthStateFlow() } returns flowOf(authenticatedUser, null)

            // When & Then
            authStateManager.getAuthState().test {
                // First emission: authenticated
                val firstState = awaitItem()
                assert(firstState is AuthState.Authenticated)
                assert((firstState as AuthState.Authenticated).user == authenticatedUser)

                // Second emission: unauthenticated
                val secondState = awaitItem()
                assert(secondState == AuthState.Unauthenticated)

                awaitComplete()
            }

            verify { authRepository.getAuthStateFlow() }
        }

    // isUserAuthenticated Tests

    @Test
    fun isUserAuthenticated_whenUserSignedIn_returnsTrue() {
        // Given
        every { authRepository.isSignedIn() } returns true

        // When
        val result = authStateManager.isUserAuthenticated()

        // Then
        assert(result == true)
        verify { authRepository.isSignedIn() }
    }

    @Test
    fun isUserAuthenticated_whenUserNotSignedIn_returnsFalse() {
        // Given
        every { authRepository.isSignedIn() } returns false

        // When
        val result = authStateManager.isUserAuthenticated()

        // Then
        assert(result == false)
        verify { authRepository.isSignedIn() }
    }

    // Integration Tests

    @Test
    fun getAuthState_whenMultipleUsers_emitsCorrectStates() =
        runTest {
            // Given
            val anotherUser = TestUsers.powerUser

            every { authRepository.getAuthStateFlow() } returns flowOf(
                null,
                authenticatedUser,
                null,
                anotherUser,
            )

            // When & Then
            authStateManager.getAuthState().test {
                // First: unauthenticated
                assert(awaitItem() == AuthState.Unauthenticated)

                // Second: first user authenticated
                val secondState = awaitItem()
                assert(secondState is AuthState.Authenticated)
                assert((secondState as AuthState.Authenticated).user == authenticatedUser)

                // Third: unauthenticated again
                assert(awaitItem() == AuthState.Unauthenticated)

                // Fourth: second user authenticated
                val fourthState = awaitItem()
                assert(fourthState is AuthState.Authenticated)
                assert((fourthState as AuthState.Authenticated).user == anotherUser)

                awaitComplete()
            }

            verify { authRepository.getAuthStateFlow() }
        }

    // Error Handling Tests (if repository throws exceptions)

    @Test
    fun getAuthState_whenRepositoryThrowsException_propagatesException() =
        runTest {
            // Given
            val exception = RuntimeException("Repository error")
            every { authRepository.getAuthStateFlow() } throws exception

            // When & Then
            try {
                authStateManager.getAuthState().test {
                    awaitError()
                }
            } catch (e: Exception) {
                assert(e == exception)
            }

            verify { authRepository.getAuthStateFlow() }
        }

    @Test
    fun isUserAuthenticated_whenRepositoryThrowsException_propagatesException() {
        // Given
        val exception = RuntimeException("Repository error")
        every { authRepository.isSignedIn() } throws exception

        // When & Then
        try {
            authStateManager.isUserAuthenticated()
            assert(false) { "Expected exception to be thrown" }
        } catch (e: Exception) {
            assert(e == exception)
        }

        verify { authRepository.isSignedIn() }
    }

    // Dependency Injection Tests

    @Test
    fun authStateManager_whenInstantiated_dependsOnAuthRepository() {
        // Given & When
        val manager = AuthStateManager(authRepository)

        // Then - Should not throw any exceptions and should be properly initialized
        assert(manager != null)
        // The actual dependency is verified through the other tests that call repository methods
    }
}
