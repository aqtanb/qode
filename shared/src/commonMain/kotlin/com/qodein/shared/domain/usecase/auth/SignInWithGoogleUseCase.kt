package com.qodein.shared.domain.usecase.auth

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.UserError
import com.qodein.shared.domain.repository.AuthRepository
import com.qodein.shared.domain.repository.UserRepository
import com.qodein.shared.model.User
import com.qodein.shared.model.UserProfile

class SignInWithGoogleUseCase(private val authRepository: AuthRepository, private val userRepository: UserRepository) {
    suspend operator fun invoke(idToken: String): Result<Unit, OperationError> {
        when (val authResult = authRepository.signInWithGoogle(idToken)) {
            is Result.Error -> return Result.Error(authResult.error)
            is Result.Success -> {
                val authUser = authResult.data
                when (val userResult = userRepository.getUserById(authResult.data.uid)) {
                    is Result.Success -> return Result.Success(Unit)
                    is Result.Error -> {
                        if (userResult.error is UserError.ProfileFailure.NotFound) {
                            val userProfile = when (
                                val profileResult = UserProfile.create(
                                    displayName = authUser.displayName,
                                    photoUrl = authUser.photoUrl,
                                )
                            ) {
                                is Result.Error -> return Result.Error(userResult.error)
                                is Result.Success -> profileResult.data
                            }
                            val user = when (
                                val userCreateResult = User.create(
                                    id = authUser.uid,
                                    email = authUser.email,
                                    profile = userProfile,
                                )
                            ) {
                                is Result.Error -> return Result.Error(userCreateResult.error)
                                is Result.Success -> userCreateResult.data
                            }
                            return when (val createResult = userRepository.createUser(user)) {
                                is Result.Success -> Result.Success(Unit)
                                is Result.Error -> Result.Error(createResult.error)
                            }
                        }
                        return Result.Error(userResult.error)
                    }
                }
            }
        }
    }
}
