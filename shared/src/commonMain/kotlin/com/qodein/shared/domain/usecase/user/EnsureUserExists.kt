package com.qodein.shared.domain.usecase.user

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.UserError
import com.qodein.shared.domain.repository.UserRepository
import com.qodein.shared.model.GoogleAuthResult
import com.qodein.shared.model.UserResolutionResult

class EnsureUserExists(private val userRepository: UserRepository) {

    suspend operator fun invoke(authUser: GoogleAuthResult): Result<UserResolutionResult, OperationError> =
        when (val existingUser = userRepository.getUserById(authUser.uid)) {
            is Result.Success -> Result.Success(UserResolutionResult.ExistingUser(existingUser.data))
            is Result.Error -> {
                if (existingUser.error is UserError.ProfileFailure.NotFound) {
                    Result.Success(UserResolutionResult.NewUserNeedsConsent(authUser))
                } else {
                    Result.Error(existingUser.error)
                }
            }
        }
}
