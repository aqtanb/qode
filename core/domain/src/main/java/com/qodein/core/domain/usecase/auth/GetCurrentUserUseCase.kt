package com.qodein.core.domain.usecase.auth

import com.qodein.core.domain.repository.AuthRepository
import com.qodein.core.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetCurrentUserUseCase @Inject constructor(private val authRepository: AuthRepository) {

    operator fun invoke(): Flow<Result<User?>> =
        authRepository.getAuthStateFlow()
            .map { Result.success(it) }
            .catch { emit(Result.failure(it)) }

    fun getCurrentUserOnce(): Flow<Result<User?>> =
        authRepository.getCurrentUser()
            .map { Result.success(it) }
            .catch { emit(Result.failure(it)) }

    fun isSignedIn(): Boolean = authRepository.isSignedIn()
}
