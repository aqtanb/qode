package com.qodein.shared.domain.usecase.user

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.UserRepository
import com.qodein.shared.model.User

class GetUserByIdUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(userId: String): Result<User, OperationError> = userRepository.getUserById(userId)
}
