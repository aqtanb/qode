package com.qodein.shared.model

sealed class UserResolutionResult {
    data class ExistingUser(val user: User) : UserResolutionResult()
    data class NewUserNeedsConsent(val authUser: GoogleAuthResult) : UserResolutionResult()
}
