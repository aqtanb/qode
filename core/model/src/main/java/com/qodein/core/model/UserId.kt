package com.qodein.core.model

@JvmInline
value class UserId(val value: String) {
    init {
        require(value.isNotBlank()) { "UserId cannot be blank" }
        require(value.length <= 128) { "UserId too long (max 128 characters)" }
        require(value.matches(USER_ID_REGEX)) { "UserId contains invalid characters" }
    }

    companion object {
        private val USER_ID_REGEX = "^[a-zA-Z0-9_-]+$".toRegex()

        fun createSafe(value: String): Result<UserId> =
            runCatching {
                UserId(value.trim())
            }
    }
}
