package com.qodein.core.data.mapper

import com.google.firebase.Timestamp
import com.qodein.core.data.model.BannerDto
import com.qodein.shared.model.Banner
import com.qodein.shared.model.BannerId

/**
 * Mapper for converting between Banner domain models and DTOs.
 * Follows the existing mapper pattern in the project.
 */
object BannerMapper {

    /**
     * Converts a Banner domain model to BannerDto for Firestore storage.
     *
     * @param banner The domain model to convert
     * @return BannerDto ready for Firestore serialization
     */
    fun toDto(banner: Banner): BannerDto =
        BannerDto(
            id = banner.id.value,
            imageUrl = banner.imageUrl,
            targetCountries = banner.targetCountries,
            brandName = banner.brandName,
            ctaTitle = banner.ctaTitle,
            ctaDescription = banner.ctaDescription,
            ctaUrl = banner.ctaUrl,
            isActive = banner.isActive,
            priority = banner.priority,
            createdAt = Timestamp(banner.createdAt / 1000, 0), // Convert millis to seconds
            updatedAt = Timestamp(banner.updatedAt / 1000, 0), // Convert millis to seconds
            expiresAt = banner.expiresAt?.let { Timestamp(it / 1000, 0) }, // Convert millis to seconds
        )

    /**
     * Converts a BannerDto from Firestore to Banner domain model.
     * Handles null safety and provides sensible defaults.
     *
     * @param dto The DTO from Firestore
     * @return Banner domain model
     * @throws IllegalArgumentException if required fields are missing/invalid
     */
    fun toDomain(dto: BannerDto): Banner {
        // Validate required fields
        require(dto.id.isNotBlank()) { "Banner ID cannot be blank" }
        require(dto.brandName.isNotBlank()) { "Banner brand name cannot be blank" }
        require(dto.ctaTitle.isNotEmpty()) { "Banner CTA title map cannot be empty" }
        require(dto.ctaDescription.isNotEmpty()) { "Banner CTA description map cannot be empty" }

        return Banner(
            id = BannerId(dto.id),
            imageUrl = dto.imageUrl,
            targetCountries = dto.targetCountries,
            brandName = dto.brandName,
            ctaTitle = dto.ctaTitle,
            ctaDescription = dto.ctaDescription,
            ctaUrl = dto.ctaUrl,
            isActive = dto.isActive,
            priority = dto.priority,
            createdAt = dto.createdAt?.toDate()?.time ?: System.currentTimeMillis(),
            updatedAt = dto.updatedAt?.toDate()?.time ?: System.currentTimeMillis(),
            expiresAt = dto.expiresAt?.toDate()?.time,
        )
    }

    /**
     * Converts a list of BannerDtos to Banner domain models.
     * Filters out any DTOs that fail conversion to ensure robustness.
     *
     * @param dtos List of DTOs from Firestore
     * @return List of valid Banner domain models
     */
    fun toDomainList(dtos: List<BannerDto>): List<Banner> =
        dtos.mapNotNull { dto ->
            try {
                toDomain(dto)
            } catch (e: Exception) {
                // Log the error but don't fail the entire operation
                // In a real app, you'd use proper logging here
                null
            }
        }

    /**
     * Converts a list of Banner domain models to BannerDtos.
     *
     * @param banners List of domain models
     * @return List of DTOs ready for Firestore
     */
    fun toDtoList(banners: List<Banner>): List<BannerDto> = banners.map { banner -> toDto(banner) }
}
