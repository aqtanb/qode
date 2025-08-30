package com.qodein.core.data.mapper

import com.google.firebase.Timestamp
import com.qodein.core.data.model.UserActivityDto
import com.qodein.shared.model.ActivityType
import com.qodein.shared.model.UserActivity
import com.qodein.shared.model.UserId
import kotlin.time.Clock
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

object UserActivityMapper {

    fun toDomain(dto: UserActivityDto): UserActivity {
        require(dto.id.isNotBlank()) { "UserActivity ID cannot be blank" }
        require(dto.userId.isNotBlank()) { "UserActivity user ID cannot be blank" }
        require(dto.type.isNotBlank()) { "UserActivity type cannot be blank" }
        require(dto.targetId.isNotBlank()) { "UserActivity target ID cannot be blank" }
        require(dto.targetType.isNotBlank()) { "UserActivity target type cannot be blank" }

        val activityType = try {
            ActivityType.valueOf(dto.type.uppercase())
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Unknown activity type: ${dto.type}")
        }

        return UserActivity(
            id = dto.id,
            userId = UserId(dto.userId),
            type = activityType,
            targetId = dto.targetId,
            targetType = dto.targetType,
            targetTitle = dto.targetTitle,
            metadata = dto.metadata,
            createdAt = dto.createdAt?.toInstant()?.toKotlinInstant() ?: Clock.System.now(),
        )
    }

    fun toDto(domain: UserActivity): UserActivityDto =
        UserActivityDto(
            id = domain.id,
            userId = domain.userId.value,
            type = domain.type.name,
            targetId = domain.targetId,
            targetType = domain.targetType,
            targetTitle = domain.targetTitle,
            metadata = domain.metadata,
            createdAt = Timestamp(domain.createdAt.toJavaInstant()),
        )

    fun toDomainList(dtos: List<UserActivityDto>): List<UserActivity> =
        dtos.mapNotNull { dto ->
            try {
                toDomain(dto)
            } catch (e: Exception) {
                null
            }
        }

    fun toDtoList(activities: List<UserActivity>): List<UserActivityDto> = activities.map { toDto(it) }
}
