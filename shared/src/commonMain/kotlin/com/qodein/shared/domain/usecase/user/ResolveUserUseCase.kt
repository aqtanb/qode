package com.qodein.shared.domain.usecase.user

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.UserError
import com.qodein.shared.domain.repository.UserRepository
import com.qodein.shared.model.GoogleAuthResult
import com.qodein.shared.model.User
import com.qodein.shared.model.UserProfile

/**
 * Ensures that a Firebase-authenticated user has a corresponding domain User entry.
 * Converts the Google auth payload to domain models and creates the user document on first sign-in.
 */
class ResolveUserUseCase(private val userRepository: UserRepository) {

    suspend operator fun invoke(authUser: GoogleAuthResult): Result<User, OperationError> =
        when (val existingUser = userRepository.getUserById(authUser.uid)) {
            is Result.Success -> existingUser
            is Result.Error -> {
                if (existingUser.error is UserError.ProfileFailure.NotFound) {
                    createUser(authUser)
                } else {
                    Result.Error(existingUser.error)
                }
            }
        }

    private suspend fun createUser(authUser: GoogleAuthResult): Result<User, OperationError> {
        val userProfile = when (
            val profileResult = UserProfile.create(
                displayName = authUser.displayName,
                photoUrl = authUser.photoUrl,
            )
        ) {
            is Result.Error -> return Result.Error(profileResult.error)
            is Result.Success -> profileResult.data
        }

        val newUser = when (
            val userCreateResult = User.create(
                id = authUser.uid,
                email = authUser.email,
                profile = userProfile,
            )
        ) {
            is Result.Error -> return Result.Error(userCreateResult.error)
            is Result.Success -> userCreateResult.data
        }

        return when (val createResult = userRepository.createUser(newUser)) {
            is Result.Success -> Result.Success(newUser)
            is Result.Error -> Result.Error(createResult.error)
        }
    }
}
