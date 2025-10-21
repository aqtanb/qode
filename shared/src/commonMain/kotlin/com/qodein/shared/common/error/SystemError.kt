package com.qodein.shared.common.error

/**
 * Shared system-level errors that can occur across all domains.
 * These abstract away infrastructure failures (network, database, etc.).
 */
sealed interface SystemError : OperationError {
    data object Offline : SystemError
    data object ServiceDown : SystemError
    data object PermissionDenied : SystemError
    data object Unknown : SystemError
}
