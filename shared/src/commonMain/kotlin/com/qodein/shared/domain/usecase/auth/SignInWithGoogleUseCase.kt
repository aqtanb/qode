package com.qodein.shared.domain.usecase.auth

import com.qodein.shared.domain.repository.AuthRepository
import com.qodein.shared.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class SignInWithGoogleUseCase constructor(private val authRepository: AuthRepository) {

    operator fun invoke(): Flow<Result<User>> =
        authRepository.signInWithGoogle()
            .map { Result.success(it) }
            .catch { emit(Result.failure(it)) }
}
