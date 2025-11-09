package com.qodein.core.data.mapper

import com.google.firebase.Timestamp
import com.qodein.core.data.dto.UserDto
import com.qodein.core.data.dto.UserProfileDto
import com.qodein.core.data.dto.UserStatsDto
import com.qodein.shared.model.Email
import com.qodein.shared.model.User
import com.qodein.shared.model.UserId
import com.qodein.shared.model.UserProfile
import com.qodein.shared.model.UserStats

object UserMapper {

    fun toDomain(
        dto: UserDto,
        userId: UserId
    ): User =
        User(
            id = userId,
            email = Email(dto.email),
            profile = profileToDomain(dto.profile),
            stats = statsToDomain(dto.stats, userId),
            country = dto.country,
        )

    fun toDto(user: User): UserDto =
        UserDto(
            email = user.email.value,
            profile = profileToDto(user.profile),
            stats = statsToDto(user.stats),
            country = user.country,
        )

    private fun profileToDomain(dto: UserProfileDto): UserProfile =
        UserProfile(
            firstName = dto.firstName,
            lastName = dto.lastName,
            bio = dto.bio,
            photoUrl = dto.photoUrl,
            createdAt = dto.createdAt?.toDate()?.time ?: 0L,
            updatedAt = dto.updatedAt?.toDate()?.time ?: 0L,
        )

    private fun profileToDto(profile: UserProfile): UserProfileDto =
        UserProfileDto(
            firstName = profile.firstName,
            lastName = profile.lastName,
            bio = profile.bio,
            photoUrl = profile.photoUrl,
            createdAt = Timestamp(profile.createdAt / 1000, 0),
            updatedAt = Timestamp(profile.updatedAt / 1000, 0),
        )

    private fun statsToDomain(
        dto: UserStatsDto,
        userId: UserId
    ): UserStats =
        UserStats(
            userId = userId,
            submittedPromocodesCount = dto.submittedPromocodesCount,
            submittedPostsCount = dto.submittedPostsCount,
            createdAt = dto.createdAt?.toDate()?.time ?: 0L,
        )

    private fun statsToDto(stats: UserStats): UserStatsDto =
        UserStatsDto(
            submittedPromocodesCount = stats.submittedPromocodesCount,
            submittedPostsCount = stats.submittedPostsCount,
            createdAt = Timestamp(stats.createdAt / 1000, 0),
        )
}
