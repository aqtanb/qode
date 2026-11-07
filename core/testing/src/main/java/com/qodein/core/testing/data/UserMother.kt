package com.qodein.core.testing.data

import com.qodein.shared.model.User
import com.qodein.shared.model.UserConsent
import com.qodein.shared.model.UserId
import com.qodein.shared.model.UserProfile
import com.qodein.shared.model.UserStats

object UserMother {
    fun alice(): User = user(id = "alice-id", displayName = "Alice", photoUrl = "https://example.com/alice.png")

    fun user(
        id: String = "user-id",
        displayName: String? = "User",
        photoUrl: String? = null
    ): User =
        User.fromDto(
            id = UserId(id),
            profile = UserProfile.fromDto(displayName = displayName, photoUrl = photoUrl),
            stats = UserStats(userId = UserId(id)),
            consent = UserConsent(),
        )
}
