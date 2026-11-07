package com.qodein.core.data.worker

import com.qodein.shared.common.error.DatabaseError
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.PostError
import com.qodein.shared.common.error.PromocodeError
import com.qodein.shared.common.error.StorageError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.common.error.UserError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UploadFailurePolicyTest {

    private val transient: List<OperationError> = listOf(
        SystemError.Offline,
        DatabaseError.Unavailable,
        DatabaseError.DeadlineExceeded,
        DatabaseError.Internal,
        DatabaseError.ResourceExhausted,
        DatabaseError.Aborted,
    )

    private val permanent: List<OperationError> = listOf(
        SystemError.Unknown,
        DatabaseError.InvalidArgument,
        DatabaseError.PermissionDenied,
        DatabaseError.Unauthenticated,
        DatabaseError.AlreadyExists,
        PostError.CreationFailure.EmptyTitle,
        PromocodeError.CreationFailure.InvalidDateRange,
        PromocodeError.SubmissionFailure.DuplicateCode,
        StorageError.UploadFailure.FileTooLarge,
        UserError.ProfileFailure.NotFound,
    )

    @Test
    fun networkTimeoutAndServerErrorsAreTransient() {
        transient.forEach { assertTrue("$it should be transient", UploadFailurePolicy.isTransient(it)) }
    }

    @Test
    fun validationAndClientErrorsArePermanent() {
        permanent.forEach { assertFalse("$it should be permanent", UploadFailurePolicy.isTransient(it)) }
    }

    @Test
    fun transientErrorRetriesUntilTheLastAttempt() {
        val retries = (0 until UploadFailurePolicy.MAX_ATTEMPTS).map { attempt ->
            UploadFailurePolicy.shouldRetry(SystemError.Offline, runAttemptCount = attempt)
        }

        assertEquals(listOf(true, true, true, true, false), retries)
    }

    @Test
    fun permanentErrorNeverRetries() {
        assertFalse(UploadFailurePolicy.shouldRetry(DatabaseError.PermissionDenied, runAttemptCount = 0))
    }
}
