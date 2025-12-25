package com.qodein.core.ui.preview

import com.qodein.shared.common.requireSuccess
import com.qodein.shared.model.Email
import com.qodein.shared.model.User
import com.qodein.shared.model.UserConsent
import com.qodein.shared.model.UserId
import com.qodein.shared.model.UserProfile
import com.qodein.shared.model.UserStats

object UserPreviewData {

    private fun buildUser(
        id: String,
        email: String,
        displayName: String?,
        photoUrl: String?,
        submittedPromocodes: Int,
        submittedPosts: Int
    ): User =
        User.fromDto(
            id = UserId(id),
            email = Email(email),
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
        email = "john.doe@example.com",
        displayName = "John",
        photoUrl = "https://picsum.photos/200/200?random=1",
        submittedPromocodes = 0,
        submittedPosts = 0,
    )

    val activeContributor = buildUser(
        id = "user456",
        email = "sarah.smith@example.com",
        displayName = "Sarah Smith",
        photoUrl = "https://picsum.photos/200/200?random=2",
        submittedPromocodes = 47,
        submittedPosts = 23,
    )

    val powerUser = buildUser(
        id = "user789",
        email = "alex.chen@example.com",
        displayName = "Alex Chen",
        photoUrl = null,
        submittedPromocodes = 156,
        submittedPosts = 89,
    )

    val userWithoutAvatar = buildUser(
        id = "user999",
        email = "maria.garcia@example.com",
        displayName = "Maria Garcia",
        photoUrl = null,
        submittedPromocodes = 12,
        submittedPosts = 5,
    )

    val userWithLongName = buildUser(
        id = "user888",
        email = "alexandra.rodriguez@example.com",
        displayName = "Alexandra Rodriguez-Martinez",
        photoUrl = "https://picsum.photos/200/200?random=3",
        submittedPromocodes = 8,
        submittedPosts = 15,
    )
}
