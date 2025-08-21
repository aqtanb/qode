package com.qodein.shared.common.result

/**
 * Error type classification for UI layer string mapping.
 * Shared module contains only error classification logic - NO user-facing strings.
 * UI layer maps these types to localized strings via string resources.
 */
enum class ErrorType {
    // Network errors
    NETWORK_TIMEOUT,
    NETWORK_NO_CONNECTION,
    NETWORK_HOST_UNREACHABLE,
    NETWORK_GENERAL,

    // Authentication/Security errors
    AUTH_PERMISSION_DENIED,
    AUTH_USER_CANCELLED,
    AUTH_INVALID_CREDENTIALS,
    AUTH_USER_NOT_FOUND,
    AUTH_UNAUTHORIZED,

    // Business logic errors - Promo codes
    PROMO_CODE_NOT_FOUND,
    PROMO_CODE_EXPIRED,
    PROMO_CODE_INACTIVE,
    PROMO_CODE_INVALID,
    PROMO_CODE_ALREADY_EXISTS,
    PROMO_CODE_ALREADY_USED,
    PROMO_CODE_MINIMUM_ORDER_NOT_MET,

    // Business logic errors - Users
    USER_NOT_FOUND,
    USER_BANNED,
    USER_SUSPENDED,

    // Business logic errors - Services
    SERVICE_NOT_FOUND,
    SERVICE_UNAVAILABLE,

    // Validation errors
    VALIDATION_REQUIRED_FIELD,
    VALIDATION_INVALID_FORMAT,
    VALIDATION_TOO_SHORT,
    VALIDATION_TOO_LONG,

    // System errors
    SERVICE_UNAVAILABLE_GENERAL,
    SERVICE_CONFIGURATION_ERROR,
    SERVICE_INITIALIZATION_ERROR,

    // Fallback
    UNKNOWN_ERROR
}

/**
 * Action suggestions for error recovery.
 */
enum class ErrorAction {
    RETRY, // Show retry button
    SIGN_IN, // Navigate to sign in
    CHECK_NETWORK, // Show network troubleshooting
    CONTACT_SUPPORT, // Show support contact
    DISMISS_ONLY // Only allow dismissing
}
