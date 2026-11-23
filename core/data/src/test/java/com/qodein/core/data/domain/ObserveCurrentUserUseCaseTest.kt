package com.qodein.core.data.domain

import app.cash.turbine.test
import com.qodein.core.testing.fake.FakeAuthRepository
import com.qodein.core.testing.fake.FakeUserRepository
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.UserError
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import com.qodein.shared.domain.usecase.user.AcceptConsentAndCreateUserUseCase
import com.qodein.shared.domain.usecase.user.ObserveCurrentUserUseCase
import com.qodein.shared.model.GoogleAuthResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Clock

class ObserveCurrentUserUseCaseTest {

    private val authRepository = FakeAuthRepository()
    private val userRepository = FakeUserRepository()

    @Test
    fun newUserAppearsAfterConsentWithoutResubscribing() =
        runTest {
            val observeCurrentUser = ObserveCurrentUserUseCase(
                GetAuthStateUseCase(authRepository, backgroundScope),
                userRepository,
                backgroundScope,
            )

            observeCurrentUser().test {
                assertEquals(Result.Error(UserError.AuthenticationFailure.NoCredentialsAvailable), awaitItem())

                authRepository.signIn("new-id")
                assertEquals(Result.Error(UserError.ProfileFailure.NotFound), awaitItem())

                AcceptConsentAndCreateUserUseCase(userRepository, Clock.System)(
                    GoogleAuthResult(uid = "new-id", email = null, displayName = null, photoUrl = null),
                    displayName = "Newbie",
                )
                val user = (awaitItem() as Result.Success).data
                assertEquals("new-id", user.id.value)
                assertEquals("Newbie", user.profile.displayName)
            }
        }
}
