package com.qodein.core.domain.usecase.auth

import com.qodein.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignOutUseCase @Inject constructor(private val authRepository: AuthRepository) {

    operator fun invoke(): Flow<Result<Unit>> =
        authRepository.signOut()
            .map { Result.success(it) }
            .catch { emit(Result.failure(it)) }
}
