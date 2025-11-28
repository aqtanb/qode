package com.qodein.shared.common.error

/**
 * Firestore-specific errors mapped from FirebaseFirestoreException.Code
 * Based on gRPC status codes (https://grpc.io/docs/guides/status-codes/)
 */
sealed interface FirestoreError : OperationError {
    /**
     * Client specified invalid argument (e.g., invalid field value, malformed query).
     * Fix: Validate input data before sending to Firestore.
     */
    data object InvalidArgument : FirestoreError

    /**
     * Requested document/collection not found.
     * Common when: Document was deleted, wrong path, or never existed.
     */
    data object NotFound : FirestoreError

    /**
     * Attempted to create document that already exists.
     * Common when: Using set() without merge, or creating duplicate IDs.
     */
    data object AlreadyExists : FirestoreError

    /**
     * Insufficient permissions to perform operation.
     * Fix: Check Firestore Security Rules - user lacks read/write permission.
     */
    data object PermissionDenied : FirestoreError

    /**
     * Request lacks valid authentication credentials.
     * Common when: User not signed in, or token expired.
     */
    data object Unauthenticated : FirestoreError

    /**
     * Operation rejected due to system state (e.g., missing composite index).
     * Fix: Create missing Firestore index via console link in error message.
     */
    data object FailedPrecondition : FirestoreError

    /**
     * Operation attempted past valid range (e.g., query limit exceeded).
     * Rare - usually invalid query parameters.
     */
    data object OutOfRange : FirestoreError

    /**
     * Operation not implemented or not supported by Firestore.
     * Rare - may occur with experimental/deprecated features.
     */
    data object Unimplemented : FirestoreError

    /**
     * Service temporarily unavailable (network issue or server maintenance).
     * Action: Retry with exponential backoff. User may have no internet.
     */
    data object Unavailable : FirestoreError

    /**
     * Operation timed out (query took too long or slow network).
     * Fix: Optimize query, add indexes, or increase timeout.
     */
    data object DeadlineExceeded : FirestoreError

    /**
     * Resource quota exceeded (read/write limits, storage, etc.).
     * Fix: Upgrade Firebase plan or optimize query patterns.
     */
    data object ResourceExhausted : FirestoreError

    /**
     * Internal server error - something broken on Firebase side.
     * Action: Retry or report to Firebase support if persistent.
     */
    data object Internal : FirestoreError

    /**
     * Unrecoverable data loss or corruption.
     * Critical: Contact Firebase support immediately.
     */
    data object DataLoss : FirestoreError

    /**
     * Operation cancelled by caller (client-side cancellation).
     * Common when: User navigates away, coroutine cancelled, or timeout.
     */
    data object Cancelled : FirestoreError

    /**
     * Transaction aborted due to concurrent modification.
     * Action: Retry transaction - another client modified same data.
     */
    data object Aborted : FirestoreError
}
