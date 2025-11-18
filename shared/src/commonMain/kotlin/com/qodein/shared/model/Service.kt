package com.qodein.shared.model

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.ServiceError
import kotlinx.serialization.Serializable

/**
 * Service ID - format: servicename_category, lowercase, sanitized
 * Validation is done in Service.create() for rich error handling.
 */
@Serializable
@JvmInline
value class ServiceId(val value: String) {
    override fun toString(): String = value
}

@ConsistentCopyVisibility
@Serializable
data class Service private constructor(val id: ServiceId, val name: String, val logoUrl: String? = null, val promoCodeCount: Int = 0) {
    companion object {
        const val NAME_MIN_LENGTH = 1
        const val NAME_MAX_LENGTH = 100

        fun create(
            name: String,
            logoUrl: String? = null
        ): Result<Service, ServiceError.CreationFailure> {
            val cleanName = name.trim()
            val cleanLogoUrl = logoUrl?.trim()

            if (cleanName.isBlank()) {
                return Result.Error(ServiceError.CreationFailure.EmptyName)
            }
            if (cleanName.length < NAME_MIN_LENGTH) {
                return Result.Error(ServiceError.CreationFailure.NameTooShort)
            }
            if (cleanName.length > NAME_MAX_LENGTH) {
                return Result.Error(ServiceError.CreationFailure.NameTooLong)
            }

            val serviceId = generateServiceId(cleanName)
            if (serviceId.isBlank() || serviceId.length > 200) {
                return Result.Error(ServiceError.CreationFailure.InvalidServiceId)
            }

            return Result.Success(
                Service(
                    id = ServiceId(serviceId),
                    name = cleanName,
                    logoUrl = cleanLogoUrl,
                    promoCodeCount = 0,
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
            logoUrl: String? = null,
            promoCodeCount: Int = 0
        ): Service =
            Service(
                id = id,
                name = name,
                logoUrl = logoUrl,
                promoCodeCount = promoCodeCount,
            )

        private fun generateServiceId(name: String): String =
            name.lowercase().trim().replace(Regex("\\s+"), "_").replace(Regex("[^a-z0-9_]"), "")
    }
}
