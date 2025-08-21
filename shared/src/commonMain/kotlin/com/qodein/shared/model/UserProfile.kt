package com.qodein.shared.model

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.yearsUntil

data class UserProfile(
    val username: String,
    val firstName: String,
    val lastName: String?,
    val bio: String?,
    val photoUrl: String?,
    val birthday: LocalDate?,
    val gender: Gender?,
    val isGenerated: Boolean = false, // Was profile auto-generated?
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val updatedAt: Long = Clock.System.now().toEpochMilliseconds()
) {
    init {
        validateUsername(username)
        validateFirstName(firstName)
        lastName?.let { validateLastName(it) }
        bio?.let { validateBio(it) }
        photoUrl?.let { validatePhotoUrl(it) }
        birthday?.let { validateBirthday(it) }
        require(createdAt > 0) { "CreatedAt must be positive" }
        require(updatedAt >= createdAt) { "UpdatedAt cannot be before createdAt" }
    }

    val displayName: String get() = listOfNotNull(firstName, lastName).joinToString(" ")
    val fullName: String get() = displayName
    val initials: String get() = listOfNotNull(
        firstName.firstOrNull()?.toString(),
        lastName?.firstOrNull()?.toString(),
    ).joinToString("")

    val isComplete: Boolean get() = !isGenerated &&
        firstName.isNotBlank() &&
        username.isNotBlank() &&
        (bio?.isNotBlank() == true || birthday != null)

    val age: Int? get() = birthday?.let {
        it.yearsUntil(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date)
    }

    companion object {
        const val MIN_USERNAME_LENGTH = 3
        const val MAX_USERNAME_LENGTH = 30
        const val MIN_FIRST_NAME_LENGTH = 1
        const val MAX_FIRST_NAME_LENGTH = 50
        const val MAX_LAST_NAME_LENGTH = 50
        const val MAX_BIO_LENGTH = 500
        const val MIN_AGE = 13
        const val MAX_AGE = 120

        private val USERNAME_REGEX = "^[a-zA-Z0-9_]+$".toRegex()
        private val NAME_REGEX = "^[a-zA-ZÀ-ÿА-я\\s'-]+$".toRegex()
        private val URL_REGEX = "^https?://.*".toRegex()

        fun createSafe(
            username: String,
            firstName: String,
            lastName: String? = null,
            bio: String? = null,
            photoUrl: String? = null,
            birthday: LocalDate? = null,
            gender: Gender? = null,
            isGenerated: Boolean = false
        ): Result<UserProfile> =
            runCatching {
                UserProfile(
                    username = username.trim(),
                    firstName = firstName.trim(),
                    lastName = lastName?.trim()?.takeIf { it.isNotBlank() },
                    bio = bio?.trim()?.takeIf { it.isNotBlank() },
                    photoUrl = photoUrl?.trim()?.takeIf { it.isNotBlank() },
                    birthday = birthday,
                    gender = gender,
                    isGenerated = isGenerated,
                )
            }

        private fun validateUsername(username: String) {
            require(username.isNotBlank()) { "Username cannot be blank" }
            require(username.length >= MIN_USERNAME_LENGTH) {
                "Username too short (min $MIN_USERNAME_LENGTH characters)"
            }
            require(username.length <= MAX_USERNAME_LENGTH) {
                "Username too long (max $MAX_USERNAME_LENGTH characters)"
            }
            require(username.matches(USERNAME_REGEX)) {
                "Username can only contain letters, numbers, and underscores"
            }
            require(!username.startsWith("_")) { "Username cannot start with underscore" }
            require(!username.endsWith("_")) { "Username cannot end with underscore" }
        }

        private fun validateFirstName(firstName: String) {
            require(firstName.isNotBlank()) { "First name cannot be blank" }
            require(firstName.length >= MIN_FIRST_NAME_LENGTH) {
                "First name too short"
            }
            require(firstName.length <= MAX_FIRST_NAME_LENGTH) {
                "First name too long (max $MAX_FIRST_NAME_LENGTH characters)"
            }
            require(firstName.matches(NAME_REGEX)) {
                "First name contains invalid characters"
            }
        }

        private fun validateLastName(lastName: String) {
            require(lastName.length <= MAX_LAST_NAME_LENGTH) {
                "Last name too long (max $MAX_LAST_NAME_LENGTH characters)"
            }
            require(lastName.matches(NAME_REGEX)) {
                "Last name contains invalid characters"
            }
        }

        private fun validateBio(bio: String) {
            require(bio.length <= MAX_BIO_LENGTH) {
                "Bio too long (max $MAX_BIO_LENGTH characters)"
            }
        }

        private fun validatePhotoUrl(photoUrl: String) {
            require(photoUrl.matches(URL_REGEX)) {
                "Photo URL must be a valid HTTP/HTTPS URL"
            }
            require(photoUrl.length <= 2048) { "Photo URL too long" }
        }

        private fun validateBirthday(birthday: LocalDate) {
            val age = birthday.yearsUntil(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date)
            require(age >= MIN_AGE) { "User must be at least $MIN_AGE years old" }
            require(age <= MAX_AGE) { "Invalid birth date" }
        }
    }
}
