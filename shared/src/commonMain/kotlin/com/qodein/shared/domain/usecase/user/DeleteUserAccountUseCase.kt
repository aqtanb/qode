package com.qodein.shared.domain.usecase.user

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.UserError
import com.qodein.shared.domain.AuthState
import com.qodein.shared.domain.repository.UserRepository
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import kotlinx.coroutines.flow.first

/**
 * Deletes the current user's account and all associated data.
 *
 * This operation:
 * 1. Validates user is authenticated
 * 2. Calls Cloud Function to delete all user data
 * 3. Deletes Firebase Auth account
 *
 * This operation is irreversible.
 */
class DeleteUserAccountUseCase(private val userRepository: UserRepository, private val getAuthStateUseCase: GetAuthStateUseCase) {
    suspend operator fun invoke(): Result<Unit, OperationError> {
        val authState = getAuthStateUseCase().first()
        val currentUserId = when (authState) {
            is AuthState.Authenticated -> authState.userId.value
            AuthState.Unauthenticated ->
                return Result.Error(UserError.DeletionFailure.NotAuthenticated)
        }

        return userRepository.deleteUserAccount(currentUserId)
    }
}
