package com.qodein.core.data.mapper

import com.google.firebase.Timestamp
import com.qodein.core.data.dto.TagDto
import com.qodein.shared.model.Tag
import kotlin.time.Clock
import kotlin.time.toKotlinInstant

object TagMapper {

    fun toDomain(dto: TagDto): Tag =
        Tag(
            value = dto.documentId,
            postCount = dto.postCount,
            createdAt = dto.createdAt?.toInstant()?.toKotlinInstant() ?: Clock.System.now(),
        )

    fun toDto(tag: Tag): TagDto =
        TagDto(
            documentId = tag.value,
            postCount = tag.postCount,
            createdAt = Timestamp(tag.createdAt.epochSeconds, 0),
        )

    fun toDomainList(dtos: List<TagDto>): List<Tag> = dtos.map { toDomain(it) }

    fun toDtoList(tags: List<Tag>): List<TagDto> = tags.map { toDto(it) }
}
