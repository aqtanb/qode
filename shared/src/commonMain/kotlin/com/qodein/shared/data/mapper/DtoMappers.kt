package com.qodein.shared.data.mapper

import com.qodein.shared.data.dto.AppUpdateConfigDto
import com.qodein.shared.data.dto.BannerDto
import com.qodein.shared.data.dto.PostDto
import com.qodein.shared.data.dto.PostInsertDto
import com.qodein.shared.data.dto.PromocodeDto
import com.qodein.shared.data.dto.PromocodeInsertDto
import com.qodein.shared.data.dto.ReportInsertDto
import com.qodein.shared.data.dto.ServiceDto
import com.qodein.shared.data.dto.ServiceInsertDto
import com.qodein.shared.data.dto.UserDto
import com.qodein.shared.data.dto.UserInsertDto
import com.qodein.shared.data.dto.UserInteractionDto
import com.qodein.shared.model.AppUpdateConfig
import com.qodein.shared.model.Banner
import com.qodein.shared.model.BannerId
import com.qodein.shared.model.ContentReport
import com.qodein.shared.model.ContentType
import com.qodein.shared.model.Discount
import com.qodein.shared.model.Post
import com.qodein.shared.model.PostId
import com.qodein.shared.model.Promocode
import com.qodein.shared.model.PromocodeId
import com.qodein.shared.model.Service
import com.qodein.shared.model.ServiceId
import com.qodein.shared.model.User
import com.qodein.shared.model.UserConsent
import com.qodein.shared.model.UserId
import com.qodein.shared.model.UserInteraction
import com.qodein.shared.model.UserProfile
import com.qodein.shared.model.UserStats
import com.qodein.shared.model.VoteState
import kotlin.time.Instant

fun PromocodeDto.toDomain(): Promocode =
    Promocode.fromDto(
        id = PromocodeId(id),
        code = code,
        discount = when (discountType) {
            PromocodeDto.TYPE_FIXED_AMOUNT -> Discount.FixedAmount(discountValue ?: 0.0)
            PromocodeDto.TYPE_FREE_ITEM -> Discount.FreeItem(freeItemDescription.orEmpty())
            else -> Discount.Percentage(discountValue ?: 0.0)
        },
        minimumOrderAmount = minimumOrderAmount,
        startDate = Instant.parse(startDate),
        endDate = Instant.parse(endDate),
        authorId = UserId(authorId),
        serviceName = serviceName,
        description = description,
        upvotes = upvotes,
        downvotes = downvotes,
        isVerified = verified,
        serviceId = ServiceId(serviceId),
        serviceLogoUrl = serviceLogoUrl,
        serviceSiteUrl = serviceSiteUrl,
        authorUsername = authorName,
        authorAvatarUrl = authorAvatarUrl,
        imageUrls = imageUrls,
        createdAt = Instant.parse(createdAt),
        voteScore = voteScore,
    )

fun Promocode.toInsertDto(): PromocodeInsertDto {
    val discount = discount
    return PromocodeInsertDto(
        id = id.value,
        code = code,
        serviceId = requireNotNull(serviceId) { "Promocode needs a service" }.value,
        authorId = authorId.value,
        startDate = startDate.toString(),
        endDate = endDate.toString(),
        discountType = when (discount) {
            is Discount.Percentage -> PromocodeDto.TYPE_PERCENTAGE
            is Discount.FixedAmount -> PromocodeDto.TYPE_FIXED_AMOUNT
            is Discount.FreeItem -> PromocodeDto.TYPE_FREE_ITEM
        },
        discountValue = when (discount) {
            is Discount.Percentage -> discount.value
            is Discount.FixedAmount -> discount.value
            is Discount.FreeItem -> null
        },
        freeItemDescription = (discount as? Discount.FreeItem)?.description,
        minimumOrderAmount = minimumOrderAmount,
        description = description,
        imageUrls = imageUrls,
    )
}

fun PostDto.toDomain(): Post =
    Post.fromDto(
        id = PostId(id),
        authorId = UserId(authorId),
        authorName = authorName.orEmpty(),
        authorAvatarUrl = authorAvatarUrl,
        title = title,
        content = content,
        imageUrls = imageUrls,
        tags = tags,
        upvotes = upvotes,
        downvotes = downvotes,
        voteScore = voteScore,
        createdAt = Instant.parse(createdAt),
        updatedAt = Instant.parse(updatedAt),
    )

fun Post.toInsertDto(): PostInsertDto =
    PostInsertDto(
        id = id.value,
        authorId = authorId.value,
        title = title,
        content = content,
        imageUrls = imageUrls,
        tags = tags,
    )

fun ServiceDto.toDomain(): Service =
    Service.fromDto(
        id = ServiceId(id),
        name = name,
        siteUrl = siteUrl,
        logoUrl = logoUrl,
        promoCodeCount = promocodeCount,
    )

fun Service.toInsertDto(): ServiceInsertDto =
    ServiceInsertDto(
        id = id.value,
        name = name,
        siteUrl = siteUrl,
        logoUrl = logoUrl,
    )

fun UserDto.toDomain(): User {
    val userId = UserId(id)
    return User.fromDto(
        id = userId,
        profile = UserProfile.fromDto(displayName = displayName, photoUrl = photoUrl),
        stats = UserStats(
            userId = userId,
            submittedPromocodesCount = submittedPromocodesCount,
            submittedPostsCount = submittedPostsCount,
        ),
        consent = UserConsent(
            legalPoliciesAcceptedAt = legalPoliciesAcceptedAt?.let { Instant.parse(it).toEpochMilliseconds() },
        ),
    )
}

fun User.toInsertDto(): UserInsertDto =
    UserInsertDto(
        id = id.value,
        displayName = profile.displayName,
        photoUrl = profile.photoUrl,
        legalPoliciesAcceptedAt = consent.legalPoliciesAcceptedAt?.let { Instant.fromEpochMilliseconds(it).toString() },
    )

fun UserInteractionDto.toDomain(): UserInteraction? =
    runCatching {
        UserInteraction(
            itemId = itemId,
            itemType = ContentType.valueOf(itemType),
            userId = UserId(userId),
            voteState = VoteState.valueOf(voteState),
            isBookmarked = bookmarked,
        )
    }.getOrNull()

fun BannerDto.toDomain(): Banner =
    Banner(
        id = BannerId(id),
        imageUrl = imageUrl,
        ctaTitle = ctaTitle,
        ctaDescription = ctaDescription,
        ctaUrl = ctaUrl,
        priority = priority,
    )

fun AppUpdateConfigDto.toDomain(): AppUpdateConfig =
    AppUpdateConfig(
        minimumVersionCode = minimumVersionCode,
        flexibleUpdateStaleDays = flexibleUpdateStaleDays,
    )

fun ContentReport.toInsertDto(): ReportInsertDto =
    ReportInsertDto(
        reportedItemId = reportedItemId,
        reportedItemType = reportedItemType.name,
        reporterId = reporterId,
        reason = reason.name,
        additionalDetails = additionalDetails,
    )
