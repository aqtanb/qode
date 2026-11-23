package com.qodein.shared.domain.usecase.user

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.UserError
import com.qodein.shared.domain.AuthState
import com.qodein.shared.domain.repository.UserRepository
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import com.qodein.shared.model.UserProfile
import kotlinx.coroutines.flow.first

/** Renames the signed-in user. A blank name clears it; feeds then show the "Qode user" fallback. */
class UpdateDisplayNameUseCase(private val userRepository: UserRepository, private val getAuthStateUseCase: GetAuthStateUseCase) {
    suspend operator fun invoke(displayName: String): Result<String?, OperationError> {
        val userId = when (val authState = getAuthStateUseCase().first()) {
            is AuthState.Authenticated -> authState.userId.value
            AuthState.Unauthenticated -> return Result.Error(UserError.AuthenticationFailure.NoCredentialsAvailable)
        }
        val cleanName = when (val profile = UserProfile.create(displayName = displayName)) {
            is Result.Success -> profile.data.displayName
            is Result.Error -> return Result.Error(profile.error)
        }
        return when (val result = userRepository.updateDisplayName(userId, cleanName)) {
            is Result.Success -> Result.Success(cleanName)
            is Result.Error -> result
        }
    }
}
