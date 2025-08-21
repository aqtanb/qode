package com.qodein.shared.domain.usecase.auth

import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.asResult
import com.qodein.shared.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class SignOutUseCase constructor(private val authRepository: AuthRepository) {

    operator fun invoke(): Flow<Result<Unit>> = authRepository.signOut().asResult()
}
