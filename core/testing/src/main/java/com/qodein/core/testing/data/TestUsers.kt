package com.qodein.core.testing.data

import com.qodein.shared.model.Email
import com.qodein.shared.model.User
import com.qodein.shared.model.UserId
import com.qodein.shared.model.UserPreferences
import com.qodein.shared.model.UserProfile
import com.qodein.shared.model.UserStats

/**
 * Shared test user data for consistent testing across modules
 */
object TestUsers {

    /**
     * Standard test user - use this for most tests
     */
    val sampleUser = User(
        id = UserId("test_user_id"),
        email = Email("john.doe@example.com"),
        profile = UserProfile(
            username = "johndoe",
            firstName = "John",
            lastName = "Doe",
            bio = "Test user for unit testing",
            photoUrl = "https://example.com/profile.jpg",
            birthday = null,
            gender = null,
            isGenerated = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        ),
        stats = UserStats.initial(UserId("test_user_id")),
        preferences = UserPreferences.default(UserId("test_user_id")),
    )

    /**
     * Power user with high stats - use for stats-related tests
     */
    val powerUser = User(
        id = UserId("power_user_id"),
        email = Email("power.user@example.com"),
        profile = UserProfile(
            username = "poweruser",
            firstName = "Power",
            lastName = "User",
            bio = "High-activity test user with significant stats",
            photoUrl = "https://example.com/power-user.jpg",
            birthday = null,
            gender = null,
            isGenerated = false,
            createdAt = System.currentTimeMillis() - 86400000, // 1 day ago
            updatedAt = System.currentTimeMillis(),
        ),
        stats = UserStats(
            userId = UserId("power_user_id"),
            submittedCodes = 50,
            upvotes = 125,
            downvotes = 15,
            createdAt = System.currentTimeMillis() - 86400000,
        ),
        preferences = UserPreferences.default(UserId("power_user_id")),
    )

    /**
     * New user with minimal data - use for edge case testing
     */
    val newUser = User(
        id = UserId("new_user_id"),
        email = Email("new.user@example.com"),
        profile = UserProfile(
            username = "newuser",
            firstName = "New",
            lastName = "User",
            bio = null, // No bio set yet
            photoUrl = null, // No profile photo
            birthday = null,
            gender = null,
            isGenerated = true, // Generated profile
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        ),
        stats = UserStats.initial(UserId("new_user_id")),
        preferences = UserPreferences.default(UserId("new_user_id")),
    )

    /**
     * Create a user with custom ID - useful for parameterized tests
     */
    fun createUser(
        id: String = "test_user_${System.currentTimeMillis()}",
        email: String = "test@example.com",
        username: String = "testuser",
        firstName: String = "Test",
        lastName: String = "User"
    ) = User(
        id = UserId(id),
        email = Email(email),
        profile = UserProfile(
            username = username,
            firstName = firstName,
            lastName = lastName,
            bio = "Dynamically created test user",
            photoUrl = "https://example.com/test-user.jpg",
            birthday = null,
            gender = null,
            isGenerated = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        ),
        stats = UserStats.initial(UserId(id)),
        preferences = UserPreferences.default(UserId(id)),
    )
}
