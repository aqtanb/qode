package com.qodein.shared.domain.usecase.auth

import com.qodein.shared.domain.repository.AuthRepository
import com.qodein.shared.model.User
import kotlinx.coroutines.flow.Flow

class GetAuthStateUseCase(private val authRepository: AuthRepository) {

    operator fun invoke(): Flow<User?> = authRepository.getAuthStateFlow()
}
