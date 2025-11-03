package com.qodein.shared.domain.usecase.user

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.UserRepository
import com.qodein.shared.model.User
import kotlinx.coroutines.flow.Flow

class GetUserByIdUseCase(private val userRepository: UserRepository) {

    operator fun invoke(userId: String): Flow<Result<User, OperationError>> = userRepository.getUserById(userId)
}
