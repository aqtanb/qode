package com.qodein.core.data.worker

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

private const val INITIAL_BACKOFF_SECONDS = 30L

internal fun OneTimeWorkRequest.Builder.setUploadPolicy(): OneTimeWorkRequest.Builder =
    setConstraints(
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build(),
    ).setBackoffCriteria(BackoffPolicy.EXPONENTIAL, INITIAL_BACKOFF_SECONDS, TimeUnit.SECONDS)

/**
 * Builds a unique work name from the submission's content, so submitting the same thing twice
 * while the first upload is still pending keeps a single upload.
 */
internal fun uploadWorkName(
    kind: String,
    vararg parts: Any?
): String {
    val digest = MessageDigest.getInstance("SHA-256")
        .digest(parts.joinToString(separator = "\u0000").toByteArray())
        .joinToString("") { "%02x".format(it) }
    return "upload-$kind-$digest"
}
