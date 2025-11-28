package com.qodein.shared.domain.usecase.auth

import com.qodein.shared.domain.repository.AuthRepository

class SignOutUseCase(private val authRepository: AuthRepository) {
    operator fun invoke() = authRepository.signOut()
}
