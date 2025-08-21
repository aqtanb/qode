package com.qodein.shared.model

enum class Gender(val displayName: String) {
    MALE("Male"),
    FEMALE("Female"),
    OTHER("Other");

    companion object {
        fun fromString(value: String?): Gender? =
            when (value?.lowercase()) {
                "male", "m" -> MALE
                "female", "f" -> FEMALE
                "other", "o" -> OTHER
                else -> null
            }
    }
}
