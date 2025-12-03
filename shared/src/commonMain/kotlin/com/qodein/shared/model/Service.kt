package com.qodein.shared.model

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.ServiceError
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Service ID - generated identifier (UUID hex string).
 * Validation is done in Service.create() for rich error handling.
 */
@Serializable
@JvmInline
value class ServiceId(val value: String) {
    init {
        require(value.isNotBlank()) { "Service ID cannot be blank" }
    }

    override fun toString(): String = value
}

/**
 * Represents a reference to a service, either by ID or by name.
 * Used when creating promocodes to allow flexible service resolution.
 */
sealed interface ServiceRef {
    /**
     * Reference to an existing service by its ID.
     */
    data class ById(val id: ServiceId) : ServiceRef

    /**
     * Reference to a service by name. If service doesn't exist, it will be created.
     */
    data class ByName(val name: String, val siteUrl: String) : ServiceRef
}

@ConsistentCopyVisibility
@Serializable
data class Service private constructor(
    val id: ServiceId,
    val name: String,
    val siteUrl: String,
    val logoUrl: String?,
    val promocodeCount: Int
) {
    companion object {
        const val NAME_MIN_LENGTH = 1
        const val NAME_MAX_LENGTH = 40
        const val CLEARBIT_LOGO_BASE = "https://logo.clearbit.com/"

        fun create(
            name: String,
            siteUrl: String,
            logoUrl: String? = null
        ): Result<Service, ServiceError.CreationFailure> {
            val cleanName = name.trim()
            val cleanSiteUrl = siteUrl.trim()

            if (cleanName.isBlank()) {
                return Result.Error(ServiceError.CreationFailure.EmptyName)
            }
            if (cleanName.length < NAME_MIN_LENGTH) {
                return Result.Error(ServiceError.CreationFailure.NameTooShort)
            }
            if (cleanName.length > NAME_MAX_LENGTH) {
                return Result.Error(ServiceError.CreationFailure.NameTooLong)
            }
            if (cleanSiteUrl.isBlank()) {
                return Result.Error(ServiceError.CreationFailure.EmptySiteUrl)
            }

            val normalizedSiteUrl = normalizeDomain(cleanSiteUrl)
            val resolvedLogoUrl = logoUrl ?: deriveClearbitLogoUrl(normalizedSiteUrl)
            val serviceId = generateRandomId()

            return Result.Success(
                Service(
                    id = ServiceId(serviceId),
                    name = cleanName,
                    siteUrl = normalizedSiteUrl,
                    logoUrl = resolvedLogoUrl,
                    promocodeCount = 0,
                ),
            )
        }

        /**
         * Reconstruct a Service from storage/DTO (for mappers/repositories only).
         * Assumes data is already validated. No sanitization performed.
         */
        fun fromDto(
            id: ServiceId,
            name: String,
            siteUrl: String = "",
            logoUrl: String? = null,
            promoCodeCount: Int = 0
        ): Service =
            Service(
                id = id,
                name = name,
                siteUrl = normalizeDomain(siteUrl),
                logoUrl = logoUrl,
                promocodeCount = promoCodeCount,
            )

        @OptIn(ExperimentalUuidApi::class)
        private fun generateRandomId(): String = Uuid.random().toHexString()

        private fun normalizeDomain(rawUrl: String): String {
            val trimmed = rawUrl.trim()
            if (trimmed.isBlank()) return ""
            val withoutScheme = trimmed
                .removePrefix("https://")
                .removePrefix("http://")
                .removePrefix("www.")
            return withoutScheme
                .substringBefore('/')
                .substringBefore('?')
                .substringBefore('#')
                .trim()
        }

        private fun deriveClearbitLogoUrl(siteUrl: String): String? {
            if (siteUrl.isBlank()) return null
            return "$CLEARBIT_LOGO_BASE$siteUrl"
        }
    }
}
