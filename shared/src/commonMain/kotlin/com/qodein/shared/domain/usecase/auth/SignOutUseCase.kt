package com.qodein.shared.domain.usecase.auth

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class SignOutUseCase constructor(private val authRepository: AuthRepository) {

    operator fun invoke(): Flow<Result<Unit, OperationError>> = authRepository.signOut()
}
