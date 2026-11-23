package com.qodein.core.data.domain

import com.qodein.core.testing.fake.FakeAuthRepository
import com.qodein.core.testing.fake.FakeUserRepository
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.UserError
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import com.qodein.shared.domain.usecase.user.UpdateDisplayNameUseCase
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateDisplayNameUseCaseTest {

    private val authRepository = FakeAuthRepository()
    private val userRepository = FakeUserRepository()

    private suspend fun TestScope.updateDisplayName(name: String) =
        UpdateDisplayNameUseCase(userRepository, GetAuthStateUseCase(authRepository, backgroundScope))(name)

    @Test
    fun savesTrimmedName() =
        runTest {
            authRepository.signIn("alice-id")

            val result = updateDisplayName("  Alice  ")

            assertEquals(Result.Success("Alice"), result)
            assertEquals(mapOf("alice-id" to "Alice"), userRepository.displayNameUpdates)
        }

    @Test
    fun blankNameClearsIt() =
        runTest {
            authRepository.signIn("alice-id")

            val result = updateDisplayName("   ")

            assertEquals(Result.Success(null), result)
            assertEquals(mapOf("alice-id" to null), userRepository.displayNameUpdates)
        }

    @Test
    fun tooLongNameIsRejectedWithoutSaving() =
        runTest {
            authRepository.signIn("alice-id")

            val result = updateDisplayName("a".repeat(101))

            assertEquals(Result.Error(UserError.CreationFailure.DisplayNameTooLong), result)
            assertTrue(userRepository.displayNameUpdates.isEmpty())
        }

    @Test
    fun signedOutUserIsRejected() =
        runTest {
            val result = updateDisplayName("Alice")

            assertEquals(Result.Error(UserError.AuthenticationFailure.NoCredentialsAvailable), result)
            assertTrue(userRepository.displayNameUpdates.isEmpty())
        }
}
