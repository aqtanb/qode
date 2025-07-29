package com.qodein.core.domain.repository

import com.qodein.core.model.PhoneNumber

interface AuthRepository {
    suspend fun sendVerificationCode(phoneNumber: PhoneNumber): Result<Unit>
    suspend fun signInWithGoogle(): Result<Unit>
}
