package com.qodein.shared.common.error

/**
 * Database errors mapped from Supabase responses.
 * Based on gRPC status codes (https://grpc.io/docs/guides/status-codes/)
 */
sealed interface DatabaseError : OperationError {
    /**
     * Client specified invalid argument (e.g., invalid field value, malformed query).
     * Fix: Validate input data before sending it.
     */
    data object InvalidArgument : DatabaseError

    /**
     * Requested document/collection not found.
     * Common when: Document was deleted, wrong path, or never existed.
     */
    data object NotFound : DatabaseError

    /**
     * Attempted to create document that already exists.
     * Common when: Using set() without merge, or creating duplicate IDs.
     */
    data object AlreadyExists : DatabaseError

    /**
     * Insufficient permissions to perform operation.
     * Fix: Check Row Level Security policies - user lacks read/write permission.
     */
    data object PermissionDenied : DatabaseError

    /**
     * Request lacks valid authentication credentials.
     * Common when: User not signed in, or token expired.
     */
    data object Unauthenticated : DatabaseError

    /**
     * Operation rejected due to system state (e.g., missing composite index).
     * Fix: Check the operation's preconditions.
     */
    data object FailedPrecondition : DatabaseError

    /**
     * Operation attempted past valid range (e.g., query limit exceeded).
     * Rare - usually invalid query parameters.
     */
    data object OutOfRange : DatabaseError

    /**
     * Operation not implemented or not supported by the database.
     * Rare - may occur with experimental/deprecated features.
     */
    data object Unimplemented : DatabaseError

    /**
     * Service temporarily unavailable (network issue or server maintenance).
     * Action: Retry with exponential backoff. User may have no internet.
     */
    data object Unavailable : DatabaseError

    /**
     * Operation timed out (query took too long or slow network).
     * Fix: Optimize query, add indexes, or increase timeout.
     */
    data object DeadlineExceeded : DatabaseError

    /**
     * Resource quota exceeded (read/write limits, storage, etc.).
     * Fix: Upgrade Firebase plan or optimize query patterns.
     */
    data object ResourceExhausted : DatabaseError

    /**
     * Internal server error - something broken on Firebase side.
     * Action: Retry or report to Firebase support if persistent.
     */
    data object Internal : DatabaseError

    /**
     * Unrecoverable data loss or corruption.
     * Critical: Contact Firebase support immediately.
     */
    data object DataLoss : DatabaseError

    /**
     * Operation cancelled by caller (client-side cancellation).
     * Common when: User navigates away, coroutine cancelled, or timeout.
     */
    data object Cancelled : DatabaseError

    /**
     * Transaction aborted due to concurrent modification.
     * Action: Retry transaction - another client modified same data.
     */
    data object Aborted : DatabaseError
}
