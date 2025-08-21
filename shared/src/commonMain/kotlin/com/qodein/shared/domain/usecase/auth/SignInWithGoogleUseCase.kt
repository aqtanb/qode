package com.qodein.shared.domain.usecase.auth

import com.qodein.shared.common.result.Result
import com.qodein.shared.common.result.asResult
import com.qodein.shared.domain.repository.AuthRepository
import com.qodein.shared.model.User
import kotlinx.coroutines.flow.Flow

class SignInWithGoogleUseCase constructor(private val authRepository: AuthRepository) {

    operator fun invoke(): Flow<Result<User>> = authRepository.signInWithGoogle().asResult()
}
