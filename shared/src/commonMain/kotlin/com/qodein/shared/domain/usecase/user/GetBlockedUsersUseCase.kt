package com.qodein.shared.domain.usecase.user

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.AuthState
import com.qodein.shared.domain.repository.UserRepository
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import com.qodein.shared.model.BlocksSortBy
import com.qodein.shared.model.PaginatedResult
import com.qodein.shared.model.PaginationRequest
import com.qodein.shared.model.User
import kotlinx.coroutines.flow.first

class GetBlockedUsersUseCase(private val userRepository: UserRepository, private val getAuthStateUseCase: GetAuthStateUseCase) {
    suspend operator fun invoke(cursor: Any? = null): Result<PaginatedResult<User, BlocksSortBy>, OperationError> {
        val authState = getAuthStateUseCase().first()

        return when (authState) {
            is AuthState.Authenticated ->
                userRepository.getBlockedUsers(
                    currentUserId = authState.userId.value,
                    cursor = cursor,
                    limit = PaginationRequest.DEFAULT_PAGE_SIZE,
                )
            AuthState.Unauthenticated ->
                Result.Success(PaginatedResult(emptyList(), null))
        }
    }
}
