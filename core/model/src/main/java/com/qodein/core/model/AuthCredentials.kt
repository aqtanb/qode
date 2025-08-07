package com.qodein.core.model

data class AuthCredentials(
    val uid: String,
    val email: String,
    val displayName: String?,
    val photoUrl: String?,
    val idToken: String,
    val provider: AuthProvider = AuthProvider.GOOGLE,
    val createdAt: Long = System.currentTimeMillis()
) {
    init {
        require(uid.isNotBlank()) { "UID cannot be blank" }
        require(Email.isValid(email)) { "Invalid email format" }
        require(idToken.isNotBlank()) { "ID token cannot be blank" }
    }

    val userId: UserId get() = UserId(uid)
    val userEmail: Email get() = Email(email)

    companion object {
        fun createSafe(
            uid: String,
            email: String,
            displayName: String?,
            photoUrl: String?,
            idToken: String
        ): Result<AuthCredentials> =
            runCatching {
                AuthCredentials(
                    uid = uid.trim(),
                    email = email.trim(),
                    displayName = displayName?.trim()?.takeIf { it.isNotBlank() },
                    photoUrl = photoUrl?.trim()?.takeIf { it.isNotBlank() },
                    idToken = idToken.trim(),
                )
            }
    }
}

enum class AuthProvider {
    GOOGLE // For now only Google
}
