package com.qodein.shared.domain.usecase.auth

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.AuthRepository
import com.qodein.shared.domain.repository.UserRepository
import com.qodein.shared.model.User
import kotlinx.coroutines.flow.first

class SignInWithGoogleUseCase(private val authRepository: AuthRepository, private val userRepository: UserRepository) {
    suspend operator fun invoke(): Result<User, OperationError> {
        // First, authenticate with Google
        val authResult = authRepository.signInWithGoogle()

        // If auth succeeds, ensure user exists in Firestore
        return when (authResult) {
            is Result.Success -> {
                val user = authResult.data
                // Wait for user creation to complete
                val createResult = userRepository.createUser(user).first()
                when (createResult) {
                    is Result.Success -> authResult // Return original auth result with user
                    is Result.Error -> Result.Error(createResult.error) // Propagate creation error
                }
            }
            is Result.Error -> authResult
        }
    }
}
