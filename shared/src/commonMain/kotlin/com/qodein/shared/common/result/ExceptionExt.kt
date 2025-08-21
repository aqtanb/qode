package com.qodein.shared.common.result

/**
 * Exception classification extensions for shared module.
 *
 * IMPORTANT: This file contains NO user-facing strings.
 * Only error type classification, retry logic, and display flags.
 * UI layer handles string mapping and localization.
 */

/**
 * Classifies a technical exception into a UI-mappable error type.
 * UI layer maps ErrorType to localized string resources.
 */
fun Throwable.toErrorType(): ErrorType =
    when {
        // Network and connectivity errors (message-based detection)
        message?.contains("timeout", ignoreCase = true) == true -> ErrorType.NETWORK_TIMEOUT
        message?.contains("no internet", ignoreCase = true) == true -> ErrorType.NETWORK_NO_CONNECTION
        message?.contains("host", ignoreCase = true) == true -> ErrorType.NETWORK_HOST_UNREACHABLE
        message?.contains("connection error", ignoreCase = true) == true -> ErrorType.NETWORK_GENERAL
        message?.contains("network", ignoreCase = true) == true -> ErrorType.NETWORK_GENERAL

        // Security and permission errors (message-based detection)
        message?.contains("permission denied", ignoreCase = true) == true -> ErrorType.AUTH_PERMISSION_DENIED
        message?.contains("authentication failed", ignoreCase = true) == true -> ErrorType.AUTH_INVALID_CREDENTIALS
        message?.contains("unauthorized", ignoreCase = true) == true -> ErrorType.AUTH_UNAUTHORIZED
        message?.contains("cancelled", ignoreCase = true) == true -> ErrorType.AUTH_USER_CANCELLED
        message?.contains("user account not found", ignoreCase = true) == true -> ErrorType.AUTH_USER_NOT_FOUND

        // Promo code specific errors (message-based detection)
        message?.contains("promo code", ignoreCase = true) == true -> when {
            message?.contains("not found", ignoreCase = true) == true -> ErrorType.PROMO_CODE_NOT_FOUND
            message?.contains("expired", ignoreCase = true) == true -> ErrorType.PROMO_CODE_EXPIRED
            message?.contains("inactive", ignoreCase = true) == true ||
                message?.contains("no longer active", ignoreCase = true) == true -> ErrorType.PROMO_CODE_INACTIVE
            message?.contains("invalid", ignoreCase = true) == true -> ErrorType.PROMO_CODE_INVALID
            message?.contains("already exists", ignoreCase = true) == true -> ErrorType.PROMO_CODE_ALREADY_EXISTS
            message?.contains("already used", ignoreCase = true) == true -> ErrorType.PROMO_CODE_ALREADY_USED
            message?.contains("minimum order", ignoreCase = true) == true -> ErrorType.PROMO_CODE_MINIMUM_ORDER_NOT_MET
            else -> ErrorType.PROMO_CODE_INVALID
        }

        // User specific errors (message-based detection)
        message?.contains("user", ignoreCase = true) == true && message?.contains("not found", ignoreCase = true) == true ->
            ErrorType.USER_NOT_FOUND
        message?.contains("user", ignoreCase = true) == true && message?.contains("banned", ignoreCase = true) == true ->
            ErrorType.USER_BANNED
        message?.contains("user", ignoreCase = true) == true && message?.contains("suspended", ignoreCase = true) == true ->
            ErrorType.USER_SUSPENDED

        // Service specific errors (message-based detection)
        message?.contains("service", ignoreCase = true) == true && message?.contains("not found", ignoreCase = true) == true ->
            ErrorType.SERVICE_NOT_FOUND
        message?.contains("service unavailable", ignoreCase = true) == true -> ErrorType.SERVICE_UNAVAILABLE_GENERAL
        message?.contains("service", ignoreCase = true) == true && message?.contains("unavailable", ignoreCase = true) == true ->
            ErrorType.SERVICE_UNAVAILABLE

        // Generic validation errors (message-based detection)
        message?.contains("required", ignoreCase = true) == true -> ErrorType.VALIDATION_REQUIRED_FIELD
        message?.contains("invalid format", ignoreCase = true) == true -> ErrorType.VALIDATION_INVALID_FORMAT
        message?.contains("too short", ignoreCase = true) == true -> ErrorType.VALIDATION_TOO_SHORT
        message?.contains("too long", ignoreCase = true) == true -> ErrorType.VALIDATION_TOO_LONG

        // Configuration and initialization errors (message-based detection)
        message?.contains("configuration", ignoreCase = true) == true -> ErrorType.SERVICE_CONFIGURATION_ERROR
        message?.contains("initialization", ignoreCase = true) == true -> ErrorType.SERVICE_INITIALIZATION_ERROR

        // Exception type-based classification (fallback for cases without specific message patterns)
        this is IllegalArgumentException -> ErrorType.VALIDATION_INVALID_FORMAT
        this is IllegalStateException -> ErrorType.SERVICE_UNAVAILABLE_GENERAL

        // Fallback for unknown exception types
        else -> ErrorType.UNKNOWN_ERROR
    }

/**
 * Determines if an exception represents a retryable error.
 *
 * Returns true for transient errors (network, service availability)
 * and false for permanent errors (validation, permissions).
 */
fun Throwable.isRetryable(): Boolean =
    when (this.toErrorType()) {
        // Network errors are usually retryable
        ErrorType.NETWORK_TIMEOUT,
        ErrorType.NETWORK_NO_CONNECTION,
        ErrorType.NETWORK_HOST_UNREACHABLE,
        ErrorType.NETWORK_GENERAL -> true

        // Permission errors are not retryable (need different user action)
        ErrorType.AUTH_PERMISSION_DENIED,
        ErrorType.AUTH_INVALID_CREDENTIALS,
        ErrorType.AUTH_UNAUTHORIZED,
        ErrorType.AUTH_USER_NOT_FOUND -> false

        // User cancellation is retryable (user can try sign-in again)
        ErrorType.AUTH_USER_CANCELLED -> true

        // Business logic errors - mostly not retryable
        ErrorType.PROMO_CODE_NOT_FOUND,
        ErrorType.PROMO_CODE_EXPIRED,
        ErrorType.PROMO_CODE_INACTIVE,
        ErrorType.PROMO_CODE_INVALID,
        ErrorType.PROMO_CODE_ALREADY_EXISTS,
        ErrorType.PROMO_CODE_ALREADY_USED,
        ErrorType.PROMO_CODE_MINIMUM_ORDER_NOT_MET,
        ErrorType.USER_NOT_FOUND,
        ErrorType.USER_BANNED,
        ErrorType.USER_SUSPENDED,
        ErrorType.SERVICE_NOT_FOUND -> false

        // Service availability - retryable
        ErrorType.SERVICE_UNAVAILABLE,
        ErrorType.SERVICE_UNAVAILABLE_GENERAL,
        ErrorType.SERVICE_CONFIGURATION_ERROR,
        ErrorType.SERVICE_INITIALIZATION_ERROR -> true

        // Validation errors are not retryable (need input changes)
        ErrorType.VALIDATION_REQUIRED_FIELD,
        ErrorType.VALIDATION_INVALID_FORMAT,
        ErrorType.VALIDATION_TOO_SHORT,
        ErrorType.VALIDATION_TOO_LONG -> false

        // Unknown errors default to retryable (safer UX)
        ErrorType.UNKNOWN_ERROR -> true
    }

/**
 * Determines if an exception should trigger a snackbar message.
 *
 * Returns false for validation errors that should be shown inline,
 * true for system errors that should show as snackbars.
 */
fun Throwable.shouldShowSnackbar(): Boolean =
    when (this.toErrorType()) {
        // Network errors should show snackbars
        ErrorType.NETWORK_TIMEOUT,
        ErrorType.NETWORK_NO_CONNECTION,
        ErrorType.NETWORK_HOST_UNREACHABLE,
        ErrorType.NETWORK_GENERAL -> true

        // Permission errors should show snackbars
        ErrorType.AUTH_PERMISSION_DENIED,
        ErrorType.AUTH_INVALID_CREDENTIALS,
        ErrorType.AUTH_UNAUTHORIZED,
        ErrorType.AUTH_USER_NOT_FOUND,
        ErrorType.AUTH_USER_CANCELLED -> true

        // Business logic errors should show snackbars
        ErrorType.PROMO_CODE_NOT_FOUND,
        ErrorType.PROMO_CODE_EXPIRED,
        ErrorType.PROMO_CODE_INACTIVE,
        ErrorType.PROMO_CODE_INVALID,
        ErrorType.PROMO_CODE_ALREADY_EXISTS,
        ErrorType.PROMO_CODE_ALREADY_USED,
        ErrorType.PROMO_CODE_MINIMUM_ORDER_NOT_MET,
        ErrorType.USER_NOT_FOUND,
        ErrorType.USER_BANNED,
        ErrorType.USER_SUSPENDED,
        ErrorType.SERVICE_NOT_FOUND,
        ErrorType.SERVICE_UNAVAILABLE -> true

        // Service errors should show snackbars
        ErrorType.SERVICE_UNAVAILABLE_GENERAL,
        ErrorType.SERVICE_CONFIGURATION_ERROR,
        ErrorType.SERVICE_INITIALIZATION_ERROR -> true

        // Form validation errors should show inline, not as snackbars
        ErrorType.VALIDATION_REQUIRED_FIELD,
        ErrorType.VALIDATION_INVALID_FORMAT,
        ErrorType.VALIDATION_TOO_SHORT,
        ErrorType.VALIDATION_TOO_LONG -> false

        // Unknown errors default to snackbar
        ErrorType.UNKNOWN_ERROR -> true
    }

/**
 * Gets a technical error code for logging/debugging purposes.
 * This helps developers identify specific error scenarios without
 * exposing technical details to users.
 */
fun Throwable.getErrorCode(): String {
    val exceptionCode = when (this) {
        is IllegalArgumentException -> "VAL"
        is IllegalStateException -> "SVC"
        else -> when {
            // Message-based detection for platform-specific exceptions
            message?.contains("network", ignoreCase = true) == true ||
                message?.contains("connection", ignoreCase = true) == true ||
                message?.contains("timeout", ignoreCase = true) == true -> "NET"
            message?.contains("permission", ignoreCase = true) == true ||
                message?.contains("authentication", ignoreCase = true) == true ||
                message?.contains("unauthorized", ignoreCase = true) == true -> "SEC"
            else -> "UNK"
        }
    }

    val messageHash = message?.hashCode()?.let {
        kotlin.math.abs(it % 10000).toString().padStart(4, '0')
    } ?: "0000"

    return "$exceptionCode-$messageHash"
}

/**
 * Suggests the most appropriate user action for this exception.
 */
fun Throwable.suggestedAction(): ErrorAction =
    when (this.toErrorType()) {
        ErrorType.NETWORK_TIMEOUT,
        ErrorType.NETWORK_NO_CONNECTION,
        ErrorType.NETWORK_HOST_UNREACHABLE,
        ErrorType.NETWORK_GENERAL -> ErrorAction.CHECK_NETWORK

        ErrorType.AUTH_INVALID_CREDENTIALS,
        ErrorType.AUTH_USER_NOT_FOUND -> ErrorAction.SIGN_IN

        ErrorType.AUTH_PERMISSION_DENIED,
        ErrorType.AUTH_UNAUTHORIZED -> ErrorAction.CONTACT_SUPPORT

        ErrorType.AUTH_USER_CANCELLED -> ErrorAction.RETRY // User can try again

        ErrorType.VALIDATION_REQUIRED_FIELD,
        ErrorType.VALIDATION_INVALID_FORMAT,
        ErrorType.VALIDATION_TOO_SHORT,
        ErrorType.VALIDATION_TOO_LONG -> ErrorAction.DISMISS_ONLY

        ErrorType.SERVICE_UNAVAILABLE,
        ErrorType.SERVICE_UNAVAILABLE_GENERAL,
        ErrorType.SERVICE_CONFIGURATION_ERROR,
        ErrorType.SERVICE_INITIALIZATION_ERROR -> ErrorAction.RETRY

        // Business logic errors - depends on retry-ability
        else -> if (this.isRetryable()) ErrorAction.RETRY else ErrorAction.CONTACT_SUPPORT
    }

/**
 * Suggests the most appropriate user action for this error type.
 */
fun ErrorType.suggestedAction(): ErrorAction =
    when (this) {
        ErrorType.NETWORK_TIMEOUT,
        ErrorType.NETWORK_NO_CONNECTION,
        ErrorType.NETWORK_HOST_UNREACHABLE,
        ErrorType.NETWORK_GENERAL -> ErrorAction.CHECK_NETWORK

        ErrorType.AUTH_INVALID_CREDENTIALS,
        ErrorType.AUTH_USER_NOT_FOUND -> ErrorAction.SIGN_IN

        ErrorType.AUTH_PERMISSION_DENIED,
        ErrorType.AUTH_UNAUTHORIZED -> ErrorAction.CONTACT_SUPPORT

        ErrorType.AUTH_USER_CANCELLED -> ErrorAction.RETRY // User can try sign-in again

        ErrorType.VALIDATION_REQUIRED_FIELD,
        ErrorType.VALIDATION_INVALID_FORMAT,
        ErrorType.VALIDATION_TOO_SHORT,
        ErrorType.VALIDATION_TOO_LONG -> ErrorAction.DISMISS_ONLY

        ErrorType.SERVICE_UNAVAILABLE,
        ErrorType.SERVICE_UNAVAILABLE_GENERAL,
        ErrorType.SERVICE_CONFIGURATION_ERROR,
        ErrorType.SERVICE_INITIALIZATION_ERROR -> ErrorAction.RETRY

        ErrorType.PROMO_CODE_NOT_FOUND,
        ErrorType.PROMO_CODE_EXPIRED,
        ErrorType.PROMO_CODE_INACTIVE,
        ErrorType.PROMO_CODE_INVALID,
        ErrorType.PROMO_CODE_ALREADY_EXISTS,
        ErrorType.PROMO_CODE_ALREADY_USED,
        ErrorType.PROMO_CODE_MINIMUM_ORDER_NOT_MET -> ErrorAction.DISMISS_ONLY

        ErrorType.USER_NOT_FOUND,
        ErrorType.USER_BANNED,
        ErrorType.USER_SUSPENDED,
        ErrorType.SERVICE_NOT_FOUND -> ErrorAction.CONTACT_SUPPORT

        ErrorType.UNKNOWN_ERROR -> ErrorAction.RETRY
    }
