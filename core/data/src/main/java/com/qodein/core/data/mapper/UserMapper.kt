package com.qodein.core.data.mapper

import com.qodein.core.data.dto.UserDto
import com.qodein.core.data.dto.UserProfileDto
import com.qodein.core.data.dto.UserStatsDto
import com.qodein.shared.model.Email
import com.qodein.shared.model.User
import com.qodein.shared.model.UserId
import com.qodein.shared.model.UserProfile
import com.qodein.shared.model.UserStats

object UserMapper {
    fun toDomain(dto: UserDto): User =
        User.fromDto(
            id = UserId(dto.documentId),
            email = Email(dto.email),
            profile = UserProfile.fromDto(
                displayName = dto.profile.displayName,
                photoUrl = dto.profile.photoUrl,
            ),
            stats = UserStats(
                userId = UserId(dto.documentId),
                submittedPromocodesCount = dto.stats.submittedPromocodesCount,
                submittedPostsCount = dto.stats.submittedPostsCount,
            ),
        )

    fun toDto(user: User): UserDto =
        UserDto(
            documentId = user.id.value,
            email = user.email.value,
            profile = UserProfileDto(
                displayName = user.profile.displayName,
                photoUrl = user.profile.photoUrl,
            ),
            stats = UserStatsDto(
                submittedPromocodesCount = user.stats.submittedPromocodesCount,
                submittedPostsCount = user.stats.submittedPostsCount,
            ),
        )
}
