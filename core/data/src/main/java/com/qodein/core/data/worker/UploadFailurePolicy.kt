package com.qodein.core.data.worker

import com.qodein.shared.common.error.DatabaseError
import com.qodein.shared.common.error.InteractionError
import com.qodein.shared.common.error.LegalDocumentError
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.PostError
import com.qodein.shared.common.error.PromocodeError
import com.qodein.shared.common.error.ServiceError
import com.qodein.shared.common.error.StorageError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.common.error.UserError

/**
 * Decides whether a failed upload is worth another attempt.
 * The `when` has no `else`, so a new error type must be classified here before the app compiles.
 */
internal object UploadFailurePolicy {

    const val MAX_ATTEMPTS = 5

    fun isTransient(error: OperationError): Boolean =
        when (error) {
            SystemError.Offline -> true
            SystemError.Unknown -> false

            DatabaseError.Unavailable,
            DatabaseError.DeadlineExceeded,
            DatabaseError.Internal,
            DatabaseError.ResourceExhausted,
            DatabaseError.Aborted -> true

            DatabaseError.InvalidArgument,
            DatabaseError.NotFound,
            DatabaseError.AlreadyExists,
            DatabaseError.PermissionDenied,
            DatabaseError.Unauthenticated,
            DatabaseError.FailedPrecondition,
            DatabaseError.OutOfRange,
            DatabaseError.Unimplemented,
            DatabaseError.DataLoss,
            DatabaseError.Cancelled -> false

            UserError.AuthenticationFailure.ServiceUnavailable -> true
            LegalDocumentError.Unavailable -> true

            is StorageError,
            is PostError,
            is PromocodeError,
            is ServiceError,
            is UserError,
            is InteractionError,
            is LegalDocumentError -> false
        }

    /** True when this attempt should be retried; false when it is the final failure. */
    fun shouldRetry(
        error: OperationError,
        runAttemptCount: Int
    ): Boolean = isTransient(error) && runAttemptCount + 1 < MAX_ATTEMPTS
}
