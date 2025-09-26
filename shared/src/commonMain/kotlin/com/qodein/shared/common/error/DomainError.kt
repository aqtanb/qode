package com.qodein.shared.common.error

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration

/**
 * Base interface for all domain-specific errors in the Qode application.
 *
 * This interface provides a foundation for type-safe error handling with rich context.
 * Each domain error contains:
 * - Unique error identifier for tracking and analytics
 * - Timestamp for debugging and monitoring
 * - Rich context map for detailed error information
 */
sealed interface DomainError {
    /**
     * Unique identifier for this error type.
     * Used for analytics, logging, and error tracking.
     */
    val errorId: String

    /**
     * Timestamp when this error occurred.
     * Defaults to current system time when error is created.
     */
    val timestamp: Instant

    /**
     * Rich context information about this error.
     * Contains domain-specific data that can help with:
     * - Error analytics and monitoring
     * - User-facing error messages
     * - Error recovery strategies
     * - Debugging and support
     */
    val context: Map<String, Any>
}

/**
 * Network-related domain errors.
 * These errors occur during network operations and API calls.
 */
sealed interface NetworkError : DomainError {
    /**
     * Request timed out after waiting for specified duration.
     */
    data class Timeout(
        val timeoutMs: Long,
        val url: String,
        val retryCount: Int = 0,
        override val errorId: String = "NET_TIMEOUT",
        override val timestamp: Instant = Clock.System.now(),
        override val context: Map<String, Any> = mapOf(
            "timeout_ms" to timeoutMs,
            "url" to url,
            "retry_count" to retryCount
        )
    ) : NetworkError

    /**
     * No network connection available.
     */
    data class NoConnection(
        val lastConnectedAt: Instant?,
        val connectionType: ConnectionType?,
        override val errorId: String = "NET_NO_CONNECTION",
        override val timestamp: Instant = Clock.System.now(),
        override val context: Map<String, Any> = mapOf(
            "last_connected_at" to (lastConnectedAt?.toString() ?: "never"),
            "connection_type" to (connectionType?.name ?: "unknown")
        )
    ) : NetworkError

    /**
     * Server returned an error response.
     */
    data class ServerError(
        val httpCode: Int,
        val serverMessage: String,
        val endpoint: String,
        val requestId: String?,
        override val errorId: String = "NET_SERVER_ERROR",
        override val timestamp: Instant = Clock.System.now(),
        override val context: Map<String, Any> = mapOf(
            "http_code" to httpCode,
            "server_message" to serverMessage,
            "endpoint" to endpoint,
            "request_id" to (requestId ?: "unknown")
        )
    ) : NetworkError

    /**
     * Host is unreachable.
     */
    data class HostUnreachable(
        val host: String,
        val reason: String?,
        override val errorId: String = "NET_HOST_UNREACHABLE",
        override val timestamp: Instant = Clock.System.now(),
        override val context: Map<String, Any> = mapOf(
            "host" to host,
            "reason" to (reason ?: "unknown")
        )
    ) : NetworkError
}

/**
 * Authentication and authorization domain errors.
 * These errors occur during user authentication and permission checks.
 */
sealed interface AuthError : DomainError {
    /**
     * User cancelled the authentication process.
     */
    data object UserCancelled : AuthError {
        override val errorId: String = "AUTH_USER_CANCELLED"
        override val timestamp: Instant = Clock.System.now()
        override val context: Map<String, Any> = emptyMap()
    }

    /**
     * Invalid credentials provided during authentication.
     */
    data class InvalidCredentials(
        val attemptCount: Int,
        val lockoutTimeRemaining: Duration?,
        val provider: AuthProvider,
        override val errorId: String = "AUTH_INVALID_CREDENTIALS",
        override val timestamp: Instant = Clock.System.now(),
        override val context: Map<String, Any> = mapOf(
            "attempt_count" to attemptCount,
            "lockout_remaining_ms" to (lockoutTimeRemaining?.inWholeMilliseconds ?: 0),
            "provider" to provider.name
        )
    ) : AuthError

    /**
     * User lacks required permissions for the requested operation.
     */
    data class Unauthorized(
        val requiredPermissions: List<String>,
        val currentPermissions: List<String>,
        val resource: String,
        override val errorId: String = "AUTH_UNAUTHORIZED",
        override val timestamp: Instant = Clock.System.now(),
        override val context: Map<String, Any> = mapOf(
            "required_permissions" to requiredPermissions,
            "current_permissions" to currentPermissions,
            "resource" to resource
        )
    ) : AuthError

    /**
     * User account not found.
     */
    data class UserNotFound(
        val identifier: String,
        val searchType: UserSearchType,
        override val errorId: String = "AUTH_USER_NOT_FOUND",
        override val timestamp: Instant = Clock.System.now(),
        override val context: Map<String, Any> = mapOf(
            "identifier" to identifier,
            "search_type" to searchType.name
        )
    ) : AuthError

    /**
     * Authentication token is expired or invalid.
     */
    data class TokenExpired(
        val tokenType: TokenType,
        val expiredAt: Instant,
        override val errorId: String = "AUTH_TOKEN_EXPIRED",
        override val timestamp: Instant = Clock.System.now(),
        override val context: Map<String, Any> = mapOf(
            "token_type" to tokenType.name,
            "expired_at" to expiredAt.toString()
        )
    ) : AuthError
}

/**
 * Business logic domain errors.
 * These errors occur when business rules are violated or business operations fail.
 */
sealed interface BusinessError : DomainError {
    /**
     * Promo code has expired and cannot be used.
     */
    data class PromoCodeExpired(
        val promoCode: String,
        val expiredAt: Instant,
        val service: Service?,
        val userNotificationSent: Boolean = false,
        override val errorId: String = "BIZ_PROMO_EXPIRED",
        override val timestamp: Instant = Clock.System.now(),
        override val context: Map<String, Any> = mapOf(
            "promo_code" to promoCode,
            "expired_at" to expiredAt.toString(),
            "service_id" to (service?.id ?: "unknown"),
            "user_notified" to userNotificationSent
        )
    ) : BusinessError

    /**
     * Promo code was not found.
     */
    data class PromoCodeNotFound(
        val promoCode: String,
        val searchCriteria: Map<String, Any>,
        override val errorId: String = "BIZ_PROMO_NOT_FOUND",
        override val timestamp: Instant = Clock.System.now(),
        override val context: Map<String, Any> = mapOf(
            "promo_code" to promoCode,
            "search_criteria" to searchCriteria.toString()
        )
    ) : BusinessError

    /**
     * Service is temporarily unavailable.
     */
    data class ServiceUnavailable(
        val service: Service,
        val reason: UnavailabilityReason,
        val estimatedRecovery: Duration?,
        val alternativeServices: List<Service> = emptyList(),
        override val errorId: String = "BIZ_SERVICE_UNAVAILABLE",
        override val timestamp: Instant = Clock.System.now(),
        override val context: Map<String, Any> = mapOf(
            "service_id" to service.id,
            "service_name" to service.name,
            "reason" to reason.name,
            "estimated_recovery_ms" to (estimatedRecovery?.inWholeMilliseconds ?: -1),
            "alternatives_count" to alternativeServices.size
        )
    ) : BusinessError

    /**
     * Resource was not found.
     */
    data class ResourceNotFound(
        val resourceType: String,
        val resourceId: String,
        val searchCriteria: Map<String, Any> = emptyMap(),
        override val errorId: String = "BIZ_RESOURCE_NOT_FOUND",
        override val timestamp: Instant = Clock.System.now(),
        override val context: Map<String, Any> = mapOf(
            "resource_type" to resourceType,
            "resource_id" to resourceId,
            "search_criteria" to searchCriteria.toString()
        )
    ) : BusinessError
}

/**
 * Data validation domain errors.
 * These errors occur when user input or data validation fails.
 */
sealed interface ValidationError : DomainError {
    /**
     * Required field is missing or empty.
     */
    data class RequiredField(
        val fieldName: String,
        val fieldType: FieldType,
        override val errorId: String = "VAL_REQUIRED_FIELD",
        override val timestamp: Instant = Clock.System.now(),
        override val context: Map<String, Any> = mapOf(
            "field_name" to fieldName,
            "field_type" to fieldType.name
        )
    ) : ValidationError

    /**
     * Field value does not match expected format.
     */
    data class InvalidFormat(
        val fieldName: String,
        val expectedFormat: String,
        val actualValue: String,
        val formatExamples: List<String> = emptyList(),
        override val errorId: String = "VAL_INVALID_FORMAT",
        override val timestamp: Instant = Clock.System.now(),
        override val context: Map<String, Any> = mapOf(
            "field_name" to fieldName,
            "expected_format" to expectedFormat,
            "actual_length" to actualValue.length,
            "examples_count" to formatExamples.size,
            "actual_value_preview" to actualValue.take(20)
        )
    ) : ValidationError

    /**
     * Field value is too short.
     */
    data class TooShort(
        val fieldName: String,
        val minLength: Int,
        val actualLength: Int,
        override val errorId: String = "VAL_TOO_SHORT",
        override val timestamp: Instant = Clock.System.now(),
        override val context: Map<String, Any> = mapOf(
            "field_name" to fieldName,
            "min_length" to minLength,
            "actual_length" to actualLength
        )
    ) : ValidationError

    /**
     * Field value is too long.
     */
    data class TooLong(
        val fieldName: String,
        val maxLength: Int,
        val actualLength: Int,
        override val errorId: String = "VAL_TOO_LONG",
        override val timestamp: Instant = Clock.System.now(),
        override val context: Map<String, Any> = mapOf(
            "field_name" to fieldName,
            "max_length" to maxLength,
            "actual_length" to actualLength
        )
    ) : ValidationError
}

// Supporting enums and data classes

/**
 * Types of network connections.
 */
enum class ConnectionType {
    WIFI, CELLULAR, ETHERNET, UNKNOWN
}

/**
 * Authentication providers.
 */
enum class AuthProvider {
    GOOGLE, 
}

/**
 * User search types for authentication.
 */
enum class UserSearchType {
    EMAIL, PHONE, USER_ID, USERNAME
}

/**
 * Token types for authentication.
 */
enum class TokenType {
    ACCESS, REFRESH, ID, CUSTOM
}

/**
 * Reasons why a service might be unavailable.
 */
enum class UnavailabilityReason {
    MAINTENANCE, OVERLOAD, CONFIGURATION_ERROR, DEPENDENCY_FAILURE, UNKNOWN
}

/**
 * Field types for validation errors.
 */
enum class FieldType {
    TEXT, EMAIL, PHONE, PASSWORD, URL, NUMBER, DATE, TIME, BOOLEAN
}

/**
 * Simplified service model for error context.
 * This is a minimal representation to avoid circular dependencies.
 */
data class Service(
    val id: String,
    val name: String,
    val category: String? = null
)