package com.qodein.core.testing.data

import com.qodein.shared.model.Email
import com.qodein.shared.model.User
import com.qodein.shared.model.UserId
import com.qodein.shared.model.UserProfile
import com.qodein.shared.model.UserStats

/**
 * Shared test user data for consistent testing across modules
 */
object TestUsers {
    val sampleUser = User(
        id = UserId("user123"),
        email = Email("john.doe@example.com"),
        profile = UserProfile(
            firstName = "John",
            lastName = "Doe",
            bio = "New to the community, excited to share and discover great deals!",
            photoUrl = "https://picsum.photos/200/200?random=1",
        ),
        stats = UserStats(
            userId = UserId("user123"),
            submittedPromocodesCount = 0,
            submittedPostsCount = 0,
        ),
        country = "KZ",
    )
}
