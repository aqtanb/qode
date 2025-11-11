package com.qodein.core.data.util

import co.touchlab.kermit.Logger
import com.algolia.client.exception.AlgoliaApiException
import com.algolia.client.exception.AlgoliaIterableException
import com.algolia.client.exception.AlgoliaRetryException
import com.algolia.client.exception.AlgoliaRuntimeException
import com.algolia.client.exception.AlgoliaWaitException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.qodein.shared.common.error.AlgoliaError
import com.qodein.shared.common.error.FirestoreError

/**
 * Centralized error mapping utility for external service exceptions.
 * Maps platform-specific exceptions to domain error types.
 */
object ErrorMapper {

    /**
     * Maps all Algolia runtime exceptions to domain AlgoliaError types.
     * Handles API errors, retry failures, wait errors, and iterable errors.
     *
     * @param e The Algolia runtime exception
     * @param tag Logger tag for context
     * @return Mapped AlgoliaError
     */
    fun mapAlgoliaException(
        e: AlgoliaRuntimeException,
        tag: String
    ): AlgoliaError =
        when (e) {
            is AlgoliaApiException -> {
                Logger.e(tag, e) { "Algolia API error [HTTP ${e.httpErrorCode}]: ${e.message}" }
                when (e.httpErrorCode) {
                    400 -> AlgoliaError.InvalidQuery
                    401, 403 -> AlgoliaError.InvalidCredentials
                    404 -> AlgoliaError.IndexNotFound
                    429 -> AlgoliaError.RateLimitExceeded
                    500, 502, 503 -> AlgoliaError.ServerError
                    504 -> AlgoliaError.Timeout
                    in 500..599 -> AlgoliaError.ServerError
                    else -> {
                        Logger.w(tag) { "Unmapped Algolia HTTP code: ${e.httpErrorCode}" }
                        AlgoliaError.NetworkError
                    }
                }
            }
            is AlgoliaRetryException -> {
                Logger.e(tag, e) { "Algolia retry strategy failed after ${e.exceptions.size} attempts" }
                AlgoliaError.ServiceUnavailable
            }
            is AlgoliaWaitException -> {
                Logger.e(tag, e) { "Algolia wait strategy error: ${e.message}" }
                AlgoliaError.Timeout
            }
            is AlgoliaIterableException -> {
                Logger.e(tag, e) { "Algolia iterable helper error: ${e.message}" }
                AlgoliaError.NetworkError
            }
            else -> {
                Logger.e(tag, e) { "Unknown Algolia runtime exception: ${e::class.simpleName}" }
                AlgoliaError.NetworkError
            }
        }

    /**
     * Maps Firebase Firestore exceptions to domain FirestoreError types based on error codes.
     *
     * @param e The Firestore exception
     * @param tag Logger tag for context
     * @return Mapped FirestoreError
     */
    fun mapFirestoreException(
        e: FirebaseFirestoreException,
        tag: String
    ): FirestoreError {
        Logger.e(tag, e) { "Firestore error [${e.code.name}]: ${e.message}" }

        return when (e.code) {
            FirebaseFirestoreException.Code.INVALID_ARGUMENT -> {
                Logger.d(tag) { "Invalid argument - validate input data before sending to Firestore" }
                FirestoreError.InvalidArgument
            }
            FirebaseFirestoreException.Code.NOT_FOUND -> {
                Logger.d(tag) { "Document/collection not found" }
                FirestoreError.NotFound
            }
            FirebaseFirestoreException.Code.ALREADY_EXISTS -> {
                Logger.d(tag) { "Document already exists" }
                FirestoreError.AlreadyExists
            }
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                Logger.w(tag) { "Permission denied - check Firestore security rules" }
                FirestoreError.PermissionDenied
            }
            FirebaseFirestoreException.Code.UNAUTHENTICATED -> {
                Logger.w(tag) { "User not authenticated - token may be expired" }
                FirestoreError.Unauthenticated
            }
            FirebaseFirestoreException.Code.FAILED_PRECONDITION -> {
                Logger.e(tag) { "Failed precondition - likely missing Firestore index. Check console for index creation link" }
                FirestoreError.FailedPrecondition
            }
            FirebaseFirestoreException.Code.OUT_OF_RANGE -> {
                Logger.e(tag) { "Query out of range" }
                FirestoreError.OutOfRange
            }
            FirebaseFirestoreException.Code.UNIMPLEMENTED -> {
                Logger.e(tag) { "Operation not implemented" }
                FirestoreError.Unimplemented
            }
            FirebaseFirestoreException.Code.UNAVAILABLE -> {
                Logger.w(tag) { "Firestore unavailable - retry recommended" }
                FirestoreError.Unavailable
            }
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> {
                Logger.e(tag) { "Query timeout - consider optimizing query or adding indexes" }
                FirestoreError.DeadlineExceeded
            }
            FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED -> {
                Logger.e(tag) { "Firestore quota exceeded - upgrade plan or optimize queries" }
                FirestoreError.ResourceExhausted
            }
            FirebaseFirestoreException.Code.INTERNAL -> {
                Logger.e(tag) { "Firestore internal error - retry or report to Firebase support" }
                FirestoreError.Internal
            }
            FirebaseFirestoreException.Code.DATA_LOSS -> {
                Logger.e(tag) { "CRITICAL: Firestore data loss detected - contact Firebase support" }
                FirestoreError.DataLoss
            }
            FirebaseFirestoreException.Code.CANCELLED -> {
                Logger.w(tag) { "Operation cancelled" }
                FirestoreError.Cancelled
            }
            FirebaseFirestoreException.Code.ABORTED -> {
                Logger.w(tag) { "Transaction aborted - concurrent modification, retry recommended" }
                FirestoreError.Aborted
            }
            else -> {
                Logger.w(tag) { "Unmapped Firestore error code: ${e.code}" }
                FirestoreError.Internal
            }
        }
    }
}
