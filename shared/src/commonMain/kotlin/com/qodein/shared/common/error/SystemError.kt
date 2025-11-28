package com.qodein.shared.common.error

/**
 * Shared system-level errors for infrastructure failures.
 * Use for network, auth, and permission issues that can occur anywhere.
 * Use domain errors (PostError, StorageError) for business logic failures.
 */
sealed interface SystemError : OperationError {
    /** Network connectivity failure (IOException, timeout) */
    data object Offline : SystemError

    /** Unexpected errors - use as fallback */
    data object Unknown : SystemError
}
