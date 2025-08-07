package com.qodein.core.model

@JvmInline
value class Email(val value: String) {
    init {
        val normalized = value.trim().lowercase()
        require(normalized.isNotBlank()) { "Email cannot be blank" }
        require(normalized.length <= 254) { "Email too long (max 254 characters)" }
        require(normalized.matches(EMAIL_REGEX)) { "Invalid email format" }
    }

    val domain: String get() = value.substringAfter("@")
    val localPart: String get() = value.substringBefore("@")
    val normalized: String get() = value.trim().lowercase()

    companion object {
        private val EMAIL_REGEX = """^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$""".toRegex()

        fun createSafe(value: String): Result<Email> =
            runCatching {
                Email(value.trim().lowercase())
            }

        fun isValid(value: String): Boolean = value.trim().lowercase().matches(EMAIL_REGEX)
    }
}
