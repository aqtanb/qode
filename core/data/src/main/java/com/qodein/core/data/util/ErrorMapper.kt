package com.qodein.core.data.util

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
     * @return Mapped AlgoliaError
     */
    fun mapAlgoliaException(e: AlgoliaRuntimeException): AlgoliaError =
        when (e) {
            is AlgoliaApiException -> {
                when (e.httpErrorCode) {
                    400 -> AlgoliaError.InvalidQuery
                    401, 403 -> AlgoliaError.InvalidCredentials
                    404 -> AlgoliaError.IndexNotFound
                    429 -> AlgoliaError.RateLimitExceeded
                    500, 502, 503 -> AlgoliaError.ServerError
                    504 -> AlgoliaError.Timeout
                    in 500..599 -> AlgoliaError.ServerError
                    else -> AlgoliaError.NetworkError
                }
            }
            is AlgoliaRetryException -> AlgoliaError.ServiceUnavailable
            is AlgoliaWaitException -> AlgoliaError.Timeout
            is AlgoliaIterableException -> AlgoliaError.NetworkError
            else -> AlgoliaError.NetworkError
        }

    /**
     * Maps Firebase Firestore exceptions to domain FirestoreError types based on error codes.
     *
     * @param e The Firestore exception
     * @return Mapped FirestoreError
     */
    fun mapFirestoreException(e: FirebaseFirestoreException): FirestoreError =
        when (e.code) {
            FirebaseFirestoreException.Code.INVALID_ARGUMENT -> FirestoreError.InvalidArgument
            FirebaseFirestoreException.Code.NOT_FOUND -> FirestoreError.NotFound
            FirebaseFirestoreException.Code.ALREADY_EXISTS -> FirestoreError.AlreadyExists
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> FirestoreError.PermissionDenied
            FirebaseFirestoreException.Code.UNAUTHENTICATED -> FirestoreError.Unauthenticated
            FirebaseFirestoreException.Code.FAILED_PRECONDITION -> FirestoreError.FailedPrecondition
            FirebaseFirestoreException.Code.OUT_OF_RANGE -> FirestoreError.OutOfRange
            FirebaseFirestoreException.Code.UNIMPLEMENTED -> FirestoreError.Unimplemented
            FirebaseFirestoreException.Code.UNAVAILABLE -> FirestoreError.Unavailable
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> FirestoreError.DeadlineExceeded
            FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED -> FirestoreError.ResourceExhausted
            FirebaseFirestoreException.Code.INTERNAL -> FirestoreError.Internal
            FirebaseFirestoreException.Code.DATA_LOSS -> FirestoreError.DataLoss
            FirebaseFirestoreException.Code.CANCELLED -> FirestoreError.Cancelled
            FirebaseFirestoreException.Code.ABORTED -> FirestoreError.Aborted
            else -> FirestoreError.Internal
        }
}
