package com.qodein.core.model

/**
 * Represents a guest user who can browse but not interact
 */
object GuestUser {
    val id: UserId = UserId("guest")
    val displayName: String = "Guest"
    val level: UserLevel = UserLevel.NEWCOMER

    fun isGuest(): Boolean = true
    fun canInteract(): Boolean = false
    fun canSubmit(): Boolean = false
    fun canVote(): Boolean = false
    fun canComment(): Boolean = false
    fun canFollow(): Boolean = false
}

/**
 * Union type for authenticated and guest users
 */
sealed interface UserSession {
    data class Authenticated(val user: User) : UserSession {
        fun isGuest(): Boolean = false
        fun canInteract(): Boolean = true
    }

    data object Guest : UserSession {
        fun isGuest(): Boolean = true
        fun canInteract(): Boolean = false
    }

    data object Unauthenticated : UserSession {
        fun isGuest(): Boolean = false
        fun canInteract(): Boolean = false
    }
}
