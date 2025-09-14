package com.qodein.shared.common.result

/**
 * Base exception class for all Qode business logic errors.
 * Carries ErrorType directly instead of relying on string parsing.
 */
abstract class QodeException(val errorType: ErrorType, message: String? = null, cause: Throwable? = null) : Exception(message, cause)

/**
 * Authentication and permission related exceptions
 */
class AuthException(errorType: ErrorType, message: String? = null, cause: Throwable? = null) : QodeException(errorType, message, cause) {

    companion object {
        fun userNotFound(cause: Throwable? = null) = AuthException(ErrorType.AUTH_USER_NOT_FOUND, cause = cause)

        fun permissionDenied(cause: Throwable? = null) = AuthException(ErrorType.AUTH_PERMISSION_DENIED, cause = cause)

        fun invalidCredentials(cause: Throwable? = null) = AuthException(ErrorType.AUTH_INVALID_CREDENTIALS, cause = cause)

        fun userCancelled(cause: Throwable? = null) = AuthException(ErrorType.AUTH_USER_CANCELLED, cause = cause)

        fun unauthorized(cause: Throwable? = null) = AuthException(ErrorType.AUTH_UNAUTHORIZED, cause = cause)
    }
}

/**
 * Network and connectivity related exceptions
 */
class NetworkException(errorType: ErrorType, message: String? = null, cause: Throwable? = null) : QodeException(errorType, message, cause) {

    companion object {
        fun timeout(cause: Throwable? = null) = NetworkException(ErrorType.NETWORK_TIMEOUT, cause = cause)

        fun noConnection(cause: Throwable? = null) = NetworkException(ErrorType.NETWORK_NO_CONNECTION, cause = cause)

        fun hostUnreachable(cause: Throwable? = null) = NetworkException(ErrorType.NETWORK_HOST_UNREACHABLE, cause = cause)

        fun general(cause: Throwable? = null) = NetworkException(ErrorType.NETWORK_GENERAL, cause = cause)
    }
}

/**
 * Business logic related exceptions
 */
class BusinessException(errorType: ErrorType, message: String? = null, cause: Throwable? = null) :
    QodeException(errorType, message, cause) {

    companion object {
        fun promoCodeNotFound(cause: Throwable? = null) = BusinessException(ErrorType.PROMO_CODE_NOT_FOUND, cause = cause)

        fun promoCodeExpired(cause: Throwable? = null) = BusinessException(ErrorType.PROMO_CODE_EXPIRED, cause = cause)

        fun serviceUnavailable(cause: Throwable? = null) = BusinessException(ErrorType.SERVICE_UNAVAILABLE_GENERAL, cause = cause)

        fun userNotFound(cause: Throwable? = null) = BusinessException(ErrorType.USER_NOT_FOUND, cause = cause)
    }
}

/**
 * Service and system related exceptions
 */
class ServiceException(errorType: ErrorType, message: String? = null, cause: Throwable? = null) : QodeException(errorType, message, cause) {

    companion object {
        fun unavailable(cause: Throwable? = null) = ServiceException(ErrorType.SERVICE_UNAVAILABLE_GENERAL, cause = cause)

        fun configurationError(cause: Throwable? = null) = ServiceException(ErrorType.SERVICE_CONFIGURATION_ERROR, cause = cause)
    }
}
