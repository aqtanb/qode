package com.qodein.core.ui.preview

import com.qodein.shared.model.Email
import com.qodein.shared.model.User
import com.qodein.shared.model.UserId
import com.qodein.shared.model.UserProfile
import com.qodein.shared.model.UserStats

object UserPreviewData {

    val newUser = User.fromDto(
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
    )

    val activeContributor = User.fromDto(
        id = UserId("user456"),
        email = Email("sarah.smith@example.com"),
        profile = UserProfile(
            firstName = "Sarah",
            lastName = "Smith",
            bio = "Deal hunter | Sharing the best promo codes and savings tips",
            photoUrl = "https://picsum.photos/200/200?random=2",
        ),
        stats = UserStats(
            userId = UserId("user456"),
            submittedPromocodesCount = 47,
            submittedPostsCount = 23,
        ),
    )

    val powerUser = User.fromDto(
        id = UserId("user789"),
        email = Email("alex.chen@example.com"),
        profile = UserProfile(
            firstName = "Alex",
            lastName = "Chen",
            bio = "Top contributor | Helping everyone save money 💰",
            photoUrl = null,
        ),
        stats = UserStats(
            userId = UserId("user789"),
            submittedPromocodesCount = 156,
            submittedPostsCount = 89,
        ),
    )

    val userWithoutAvatar = User.fromDto(
        id = UserId("user999"),
        email = Email("maria.garcia@example.com"),
        profile = UserProfile(
            firstName = "Maria",
            lastName = "Garcia",
            bio = null,
            photoUrl = null,
        ),
        stats = UserStats(
            userId = UserId("user999"),
            submittedPromocodesCount = 12,
            submittedPostsCount = 5,
        ),
    )

    val userWithLongName = User.fromDto(
        id = UserId("user888"),
        email = Email("alexandra.rodriguez@example.com"),
        profile = UserProfile(
            firstName = "Alexandra",
            lastName = "Rodriguez-Martinez",
            bio = "Passionate about finding the best deals and sharing them with the community!",
            photoUrl = "https://picsum.photos/200/200?random=3",
        ),
        stats = UserStats(
            userId = UserId("user888"),
            submittedPromocodesCount = 8,
            submittedPostsCount = 15,
        ),
    )
}
