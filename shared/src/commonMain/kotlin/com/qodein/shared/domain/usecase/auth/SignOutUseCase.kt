package com.qodein.shared.domain.usecase.auth

import com.qodein.shared.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class SignOutUseCase constructor(private val authRepository: AuthRepository) {

    operator fun invoke(): Flow<Result<Unit>> =
        authRepository.signOut()
            .map { Result.success(it) }
            .catch { emit(Result.failure(it)) }
}
