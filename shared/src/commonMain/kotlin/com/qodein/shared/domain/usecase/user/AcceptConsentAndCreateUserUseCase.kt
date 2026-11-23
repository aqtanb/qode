package com.qodein.shared.domain.usecase.user

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.UserRepository
import com.qodein.shared.model.GoogleAuthResult
import com.qodein.shared.model.User
import com.qodein.shared.model.UserConsent
import com.qodein.shared.model.UserProfile
import kotlin.time.Clock

class AcceptConsentAndCreateUserUseCase(private val userRepository: UserRepository, private val clock: Clock) {

    /** [displayName] is what the user typed in the consent step; it defaults to the provider's name. */
    suspend operator fun invoke(
        authUser: GoogleAuthResult,
        displayName: String? = authUser.displayName
    ): Result<User, OperationError> {
        val userProfile = UserProfile.create(
            displayName = displayName,
            photoUrl = authUser.photoUrl,
        ).let { result ->
            if (result is Result.Error) return Result.Error(result.error)
            (result as Result.Success).data
        }

        val consent = UserConsent(
            legalPoliciesAcceptedAt = clock.now().toEpochMilliseconds(),
        )

        val newUser = User.create(
            id = authUser.uid,
            profile = userProfile,
            consent = consent,
        ).let { result ->
            if (result is Result.Error) return Result.Error(result.error)
            (result as Result.Success).data
        }

        return when (val createResult = userRepository.createUser(newUser)) {
            is Result.Success -> Result.Success(newUser)
            is Result.Error -> Result.Error(createResult.error)
        }
    }
}
