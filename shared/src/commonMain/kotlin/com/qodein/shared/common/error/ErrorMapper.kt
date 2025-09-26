package com.qodein.shared.common.error

import com.qodein.shared.common.result.QodeException
import kotlinx.datetime.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Utility object for mapping platform exceptions to domain-specific errors.
 *
 * This mapper provides intelligent exception-to-domain-error conversion that:
 * - Preserves exception context and details
 * - Enriches errors with additional metadata
 * - Provides fallback mapping for unknown exceptions
 * - Maintains backward compatibility with existing error handling
 */
object ErrorMapper {

    /**
     * Maps a platform exception to an appropriate domain error.
     *
     * @param exception The platform exception to map
     * @param operationContext Additional context about the operation that failed
     * @return A typed domain error with rich context
     */
    fun mapToDomainError(
        exception: Throwable,
        operationContext: OperationContext = OperationContext()
    ): DomainError {
        return when (exception) {
            // Direct mapping for custom Qode exceptions
            is QodeException -> mapQodeException(exception, operationContext)

            // Network-related exceptions
            is java.io.IOException -> mapIOException(exception, operationContext)
            is java.net.SocketTimeoutException -> mapTimeoutException(exception, operationContext)
            is java.net.UnknownHostException -> mapUnknownHostException(exception, operationContext)
            is java.net.ConnectException -> mapConnectException(exception, operationContext)

            // Security and authentication exceptions
            is SecurityException -> mapSecurityException(exception, operationContext)

            // Validation exceptions
            is IllegalArgumentException -> mapValidationException(exception, operationContext)
            is IllegalStateException -> mapStateException(exception, operationContext)

            // Platform-specific exceptions (will be enhanced with actual platform implementations)
            else -> mapGenericException(exception, operationContext)
        }
    }

    /**
     * Maps exceptions specifically to network errors.
     * Used when we know the context is network-related.
     */
    fun mapToNetworkError(
        exception: Throwable,
        url: String? = null,
        retryCount: Int = 0
    ): NetworkError {
        return when (exception) {
            is java.net.SocketTimeoutException -> NetworkError.Timeout(
                timeoutMs = extractTimeoutMs(exception),
                url = url ?: extractUrlFromException(exception),
                retryCount = retryCount
            )

            is java.net.UnknownHostException -> NetworkError.HostUnreachable(
                host = exception.message ?: "unknown",
                reason = "Host not found"
            )

            is java.net.ConnectException -> NetworkError.NoConnection(
                lastConnectedAt = null, // Could be enhanced with actual last connection time
                connectionType = null // Could be enhanced with network detection
            )

            is java.io.IOException -> {
                when {
                    exception.message?.contains("timeout", ignoreCase = true) == true ->
                        NetworkError.Timeout(
                            timeoutMs = 30000, // Default timeout
                            url = url ?: "unknown",
                            retryCount = retryCount
                        )

                    exception.message?.contains("connection", ignoreCase = true) == true ->
                        NetworkError.NoConnection(
                            lastConnectedAt = null,
                            connectionType = null
                        )

                    else -> NetworkError.ServerError(
                        httpCode = 0,
                        serverMessage = exception.message ?: "Unknown server error",
                        endpoint = url ?: "unknown",
                        requestId = null
                    )
                }
            }

            else -> NetworkError.ServerError(
                httpCode = 0,
                serverMessage = exception.message ?: "Unknown error",
                endpoint = url ?: "unknown",
                requestId = null
            )
        }
    }

    /**
     * Maps exceptions specifically to authentication errors.
     */
    fun mapToAuthError(exception: Throwable, provider: AuthProvider = AuthProvider.EMAIL): AuthError {
        return when (exception) {
            is SecurityException -> {
                when {
                    exception.message?.contains("authentication", ignoreCase = true) == true ->
                        AuthError.InvalidCredentials(
                            attemptCount = 1,
                            lockoutTimeRemaining = null,
                            provider = provider
                        )

                    exception.message?.contains("permission", ignoreCase = true) == true ->
                        AuthError.Unauthorized(
                            requiredPermissions = listOf("unknown"),
                            currentPermissions = emptyList(),
                            resource = "unknown"
                        )

                    exception.message?.contains("user not found", ignoreCase = true) == true ->
                        AuthError.UserNotFound(
                            identifier = "unknown",
                            searchType = UserSearchType.EMAIL
                        )

                    else -> AuthError.Unauthorized(
                        requiredPermissions = listOf("unknown"),
                        currentPermissions = emptyList(),
                        resource = extractResourceFromException(exception)
                    )
                }
            }

            else -> AuthError.InvalidCredentials(
                attemptCount = 1,
                lockoutTimeRemaining = null,
                provider = provider
            )
        }
    }

    /**
     * Maps exceptions specifically to business errors.
     */
    fun mapToBusinessError(
        exception: Throwable,
        businessContext: BusinessContext = BusinessContext()
    ): BusinessError {
        return when {
            exception.message?.contains("promo", ignoreCase = true) == true ||
            exception.message?.contains("coupon", ignoreCase = true) == true -> {
                when {
                    exception.message?.contains("expired", ignoreCase = true) == true ->
                        BusinessError.PromoCodeExpired(
                            promoCode = businessContext.promoCode ?: "unknown",
                            expiredAt = Clock.System.now().minus(1.minutes), // Assume recent expiry
                            service = businessContext.service
                        )

                    exception.message?.contains("not found", ignoreCase = true) == true ->
                        BusinessError.PromoCodeNotFound(
                            promoCode = businessContext.promoCode ?: "unknown",
                            searchCriteria = businessContext.searchCriteria
                        )

                    else -> BusinessError.PromoCodeNotFound(
                        promoCode = businessContext.promoCode ?: "unknown",
                        searchCriteria = businessContext.searchCriteria
                    )
                }
            }

            exception.message?.contains("service", ignoreCase = true) == true &&
            exception.message?.contains("unavailable", ignoreCase = true) == true -> {
                BusinessError.ServiceUnavailable(
                    service = businessContext.service ?: Service("unknown", "Unknown Service"),
                    reason = UnavailabilityReason.UNKNOWN,
                    estimatedRecovery = null
                )
            }

            exception.message?.contains("not found", ignoreCase = true) == true -> {
                BusinessError.ResourceNotFound(
                    resourceType = businessContext.resourceType ?: "unknown",
                    resourceId = businessContext.resourceId ?: "unknown",
                    searchCriteria = businessContext.searchCriteria
                )
            }

            else -> BusinessError.ResourceNotFound(
                resourceType = "unknown",
                resourceId = exception.message?.take(50) ?: "unknown",
                searchCriteria = emptyMap()
            )
        }
    }

    /**
     * Maps exceptions specifically to validation errors.
     */
    fun mapToValidationError(exception: Throwable, fieldName: String = "unknown"): ValidationError {
        return when (exception) {
            is IllegalArgumentException -> {
                when {
                    exception.message?.contains("required", ignoreCase = true) == true ||
                    exception.message?.contains("missing", ignoreCase = true) == true ||
                    exception.message?.contains("empty", ignoreCase = true) == true ->
                        ValidationError.RequiredField(
                            fieldName = fieldName,
                            fieldType = FieldType.TEXT
                        )

                    exception.message?.contains("format", ignoreCase = true) == true ||
                    exception.message?.contains("invalid", ignoreCase = true) == true ->
                        ValidationError.InvalidFormat(
                            fieldName = fieldName,
                            expectedFormat = "unknown",
                            actualValue = ""
                        )

                    exception.message?.contains("short", ignoreCase = true) == true ->
                        ValidationError.TooShort(
                            fieldName = fieldName,
                            minLength = 0,
                            actualLength = 0
                        )

                    exception.message?.contains("long", ignoreCase = true) == true ->
                        ValidationError.TooLong(
                            fieldName = fieldName,
                            maxLength = 100,
                            actualLength = 0
                        )

                    else -> ValidationError.InvalidFormat(
                        fieldName = fieldName,
                        expectedFormat = "valid format",
                        actualValue = exception.message?.take(20) ?: ""
                    )
                }
            }

            else -> ValidationError.InvalidFormat(
                fieldName = fieldName,
                expectedFormat = "valid format",
                actualValue = exception.message?.take(20) ?: ""
            )
        }
    }

    // Private mapping methods

    private fun mapQodeException(exception: QodeException, context: OperationContext): DomainError {
        // Map existing QodeException to new domain errors based on errorType
        return when (exception.errorType) {
            com.qodein.shared.common.result.ErrorType.NETWORK_TIMEOUT -> NetworkError.Timeout(
                timeoutMs = 30000,
                url = context.url ?: "unknown",
                retryCount = context.retryCount
            )

            com.qodein.shared.common.result.ErrorType.NETWORK_NO_CONNECTION -> NetworkError.NoConnection(
                lastConnectedAt = null,
                connectionType = null
            )

            com.qodein.shared.common.result.ErrorType.NETWORK_HOST_UNREACHABLE -> NetworkError.HostUnreachable(
                host = context.url ?: "unknown",
                reason = exception.message
            )

            com.qodein.shared.common.result.ErrorType.AUTH_INVALID_CREDENTIALS -> AuthError.InvalidCredentials(
                attemptCount = 1,
                lockoutTimeRemaining = null,
                provider = AuthProvider.EMAIL
            )

            com.qodein.shared.common.result.ErrorType.AUTH_USER_CANCELLED -> AuthError.UserCancelled

            com.qodein.shared.common.result.ErrorType.PROMO_CODE_EXPIRED -> BusinessError.PromoCodeExpired(
                promoCode = context.businessContext?.promoCode ?: "unknown",
                expiredAt = Clock.System.now(),
                service = context.businessContext?.service
            )

            com.qodein.shared.common.result.ErrorType.PROMO_CODE_NOT_FOUND -> BusinessError.PromoCodeNotFound(
                promoCode = context.businessContext?.promoCode ?: "unknown",
                searchCriteria = context.businessContext?.searchCriteria ?: emptyMap()
            )

            com.qodein.shared.common.result.ErrorType.VALIDATION_REQUIRED_FIELD -> ValidationError.RequiredField(
                fieldName = context.fieldName ?: "unknown",
                fieldType = FieldType.TEXT
            )

            else -> mapGenericException(exception, context)
        }
    }

    private fun mapIOException(exception: java.io.IOException, context: OperationContext): NetworkError {
        return when {
            exception.message?.contains("timeout", ignoreCase = true) == true ->
                NetworkError.Timeout(
                    timeoutMs = extractTimeoutMs(exception),
                    url = context.url ?: "unknown",
                    retryCount = context.retryCount
                )

            exception.message?.contains("connection", ignoreCase = true) == true ->
                NetworkError.NoConnection(
                    lastConnectedAt = null,
                    connectionType = null
                )

            else -> NetworkError.ServerError(
                httpCode = 0,
                serverMessage = exception.message ?: "IO Error",
                endpoint = context.url ?: "unknown",
                requestId = context.requestId
            )
        }
    }

    private fun mapTimeoutException(exception: java.net.SocketTimeoutException, context: OperationContext): NetworkError.Timeout {
        return NetworkError.Timeout(
            timeoutMs = extractTimeoutMs(exception),
            url = context.url ?: "unknown",
            retryCount = context.retryCount
        )
    }

    private fun mapUnknownHostException(exception: java.net.UnknownHostException, context: OperationContext): NetworkError.HostUnreachable {
        return NetworkError.HostUnreachable(
            host = exception.message ?: context.url ?: "unknown",
            reason = "Host not found"
        )
    }

    private fun mapConnectException(exception: java.net.ConnectException, context: OperationContext): NetworkError.NoConnection {
        return NetworkError.NoConnection(
            lastConnectedAt = null,
            connectionType = null
        )
    }

    private fun mapSecurityException(exception: SecurityException, context: OperationContext): AuthError {
        return when {
            exception.message?.contains("authentication", ignoreCase = true) == true ->
                AuthError.InvalidCredentials(
                    attemptCount = 1,
                    lockoutTimeRemaining = null,
                    provider = AuthProvider.EMAIL
                )

            exception.message?.contains("permission", ignoreCase = true) == true ->
                AuthError.Unauthorized(
                    requiredPermissions = listOf("unknown"),
                    currentPermissions = emptyList(),
                    resource = context.resource ?: "unknown"
                )

            else -> AuthError.Unauthorized(
                requiredPermissions = listOf("unknown"),
                currentPermissions = emptyList(),
                resource = context.resource ?: "unknown"
            )
        }
    }

    private fun mapValidationException(exception: IllegalArgumentException, context: OperationContext): ValidationError {
        return ValidationError.InvalidFormat(
            fieldName = context.fieldName ?: "unknown",
            expectedFormat = "valid format",
            actualValue = exception.message?.take(20) ?: ""
        )
    }

    private fun mapStateException(exception: IllegalStateException, context: OperationContext): BusinessError {
        return BusinessError.ServiceUnavailable(
            service = context.businessContext?.service ?: Service("unknown", "Unknown Service"),
            reason = UnavailabilityReason.CONFIGURATION_ERROR,
            estimatedRecovery = null
        )
    }

    private fun mapGenericException(exception: Throwable, context: OperationContext): DomainError {
        // Fallback mapping based on message analysis
        val message = exception.message?.lowercase() ?: ""

        return when {
            // Network-related keywords
            message.contains("network") || message.contains("connection") || message.contains("timeout") ->
                NetworkError.ServerError(
                    httpCode = 0,
                    serverMessage = exception.message ?: "Unknown network error",
                    endpoint = context.url ?: "unknown",
                    requestId = context.requestId
                )

            // Auth-related keywords
            message.contains("auth") || message.contains("permission") || message.contains("unauthorized") ->
                AuthError.Unauthorized(
                    requiredPermissions = listOf("unknown"),
                    currentPermissions = emptyList(),
                    resource = context.resource ?: "unknown"
                )

            // Business-related keywords
            message.contains("not found") || message.contains("unavailable") ->
                BusinessError.ResourceNotFound(
                    resourceType = context.businessContext?.resourceType ?: "unknown",
                    resourceId = context.businessContext?.resourceId ?: "unknown"
                )

            // Default to network error for unknown exceptions
            else -> NetworkError.ServerError(
                httpCode = 0,
                serverMessage = exception.message ?: "Unknown error",
                endpoint = context.url ?: "unknown",
                requestId = context.requestId
            )
        }
    }

    // Helper methods for extracting information from exceptions

    private fun extractTimeoutMs(exception: Throwable): Long {
        // Try to extract timeout value from exception message
        val message = exception.message ?: ""
        val timeoutRegex = Regex("""(\d+)\s*ms""")
        val match = timeoutRegex.find(message)
        return match?.groupValues?.get(1)?.toLongOrNull() ?: 30000L // Default 30 seconds
    }

    private fun extractUrlFromException(exception: Throwable): String {
        val message = exception.message ?: ""
        // Try to extract URL from exception message
        val urlRegex = Regex("""https?://[^\s]+""")
        return urlRegex.find(message)?.value ?: "unknown"
    }

    private fun extractResourceFromException(exception: Throwable): String {
        return exception.message?.substringAfter("resource:")?.trim()?.take(50) ?: "unknown"
    }
}

/**
 * Context information for error mapping operations.
 * Provides additional metadata to enrich domain errors.
 */
data class OperationContext(
    val url: String? = null,
    val requestId: String? = null,
    val retryCount: Int = 0,
    val resource: String? = null,
    val fieldName: String? = null,
    val userId: String? = null,
    val businessContext: BusinessContext? = null
)

/**
 * Business-specific context for error mapping.
 */
data class BusinessContext(
    val promoCode: String? = null,
    val service: Service? = null,
    val resourceType: String? = null,
    val resourceId: String? = null,
    val searchCriteria: Map<String, Any> = emptyMap()
)

/**
 * Creates an OperationContext for network operations.
 */
fun networkContext(
    url: String,
    requestId: String? = null,
    retryCount: Int = 0
) = OperationContext(
    url = url,
    requestId = requestId,
    retryCount = retryCount
)

/**
 * Creates an OperationContext for authentication operations.
 */
fun authContext(
    resource: String? = null,
    userId: String? = null
) = OperationContext(
    resource = resource,
    userId = userId
)

/**
 * Creates an OperationContext for validation operations.
 */
fun validationContext(
    fieldName: String
) = OperationContext(
    fieldName = fieldName
)

/**
 * Creates an OperationContext for business operations.
 */
fun businessContext(
    promoCode: String? = null,
    service: Service? = null,
    resourceType: String? = null,
    resourceId: String? = null,
    searchCriteria: Map<String, Any> = emptyMap()
) = OperationContext(
    businessContext = BusinessContext(
        promoCode = promoCode,
        service = service,
        resourceType = resourceType,
        resourceId = resourceId,
        searchCriteria = searchCriteria
    )
)