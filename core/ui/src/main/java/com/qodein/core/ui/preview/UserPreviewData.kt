package com.qodein.core.ui.preview

import com.qodein.shared.common.requireSuccess
import com.qodein.shared.model.User
import com.qodein.shared.model.UserConsent
import com.qodein.shared.model.UserId
import com.qodein.shared.model.UserProfile
import com.qodein.shared.model.UserStats

object UserPreviewData {

    private fun buildUser(
        id: String,
        displayName: String?,
        photoUrl: String?,
        submittedPromocodes: Int,
        submittedPosts: Int
    ): User =
        User.fromDto(
            id = UserId(id),
            profile = UserProfile.create(
                displayName = displayName,
                photoUrl = photoUrl,
            ).requireSuccess(),
            stats = UserStats(
                userId = UserId(id),
                submittedPromocodesCount = submittedPromocodes,
                submittedPostsCount = submittedPosts,
            ),
            consent = UserConsent(),
        )

    val newUser = buildUser(
        id = "user123",
        displayName = "John",
        photoUrl = "https://picsum.photos/200/200?random=1",
        submittedPromocodes = 0,
        submittedPosts = 0,
    )

    val activeContributor = buildUser(
        id = "user456",
        displayName = "Sarah Smith",
        photoUrl = "https://picsum.photos/200/200?random=2",
        submittedPromocodes = 47,
        submittedPosts = 23,
    )

    val powerUser = buildUser(
        id = "user789",
        displayName = "Alex Chen",
        photoUrl = null,
        submittedPromocodes = 156,
        submittedPosts = 89,
    )

    val userWithoutAvatar = buildUser(
        id = "user999",
        displayName = "Maria Garcia",
        photoUrl = null,
        submittedPromocodes = 12,
        submittedPosts = 5,
    )

    val userWithLongName = buildUser(
        id = "user888",
        displayName = "Alexandra Rodriguez-Martinez",
        photoUrl = "https://picsum.photos/200/200?random=3",
        submittedPromocodes = 8,
        submittedPosts = 15,
    )
}
