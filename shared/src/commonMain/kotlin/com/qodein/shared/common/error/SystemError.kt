package com.qodein.shared.common.error

/**
 * Shared system-level errors for infrastructure failures.
 * Use for network, auth, and permission issues that can occur anywhere.
 * Use domain errors (PostError, StorageError) for business logic failures.
 */
sealed interface SystemError : OperationError {
    /** Network connectivity failure (IOException, timeout) */
    data object Offline : SystemError

    /** User not authenticated (HTTP 401, session expired) */
    data object Unauthorized : SystemError

    /** User lacks permissions (HTTP 403, SecurityException) */
    data object PermissionDenied : SystemError

    /** Backend services unavailable (HTTP 503) */
    data object ServiceDown : SystemError

    /** Unexpected errors - use as fallback */
    data object Unknown : SystemError
}
