package com.qodein.shared.common.logging

import co.touchlab.kermit.Logger
import com.qodein.shared.common.result.getErrorCode
import com.qodein.shared.common.result.isRetryable
import com.qodein.shared.common.result.toErrorType

/**
 * Extension functions for Kermit Logger following NIA patterns.
 * Provides convenient logging methods that integrate with the error handling system.
 *
 * These functions work with Kermit directly and bridge to Timber on Android
 * via KermitTimberWriter for unified log output.
 */

/**
 * Log an error with ErrorType classification.
 * Useful for handled errors that need to be tracked but not crashed.
 * Following NIA pattern: log handled errors at ViewModel level with classification.
 */
fun Logger.logHandledError(
    throwable: Throwable,
    tag: String = this.tag
) {
    val errorType = throwable.toErrorType()
    val errorCode = throwable.getErrorCode()
    e(throwable) { "Handled error [$errorCode][${errorType.name}]: ${throwable.message}" }
}

/**
 * Log an error with additional context for error boundaries.
 * Use this at repository boundaries where exceptions are caught and converted to Result.Error.
 * Following NIA pattern: log at data layer boundaries for technical context.
 */
fun Logger.logErrorBoundary(
    operation: String,
    throwable: Throwable,
    tag: String = this.tag
) {
    val errorType = throwable.toErrorType()
    val errorCode = throwable.getErrorCode()
    val isRetryable = throwable.isRetryable()
    e(throwable) { "Error in $operation [$errorCode][${errorType.name}] retryable=$isRetryable: ${throwable.message}" }
}

/**
 * Log user actions for analytics foundation.
 * These logs can later be enhanced to trigger Firebase Analytics events.
 */
fun Logger.logUserAction(
    action: String,
    details: Map<String, String> = emptyMap(),
    tag: String = this.tag
) {
    val detailsStr = if (details.isNotEmpty()) {
        val formattedDetails = details.entries.joinToString(", ") { "${it.key}=${it.value}" }
        " ($formattedDetails)"
    } else {
        ""
    }
    i { "User action: $action$detailsStr" }
}

/**
 * Log performance metrics for optimization insights.
 */
fun Logger.logPerformance(
    operation: String,
    durationMs: Long,
    tag: String = this.tag
) {
    d { "Performance: $operation completed in ${durationMs}ms" }
}

/**
 * Log state changes in ViewModels for debugging.
 * Only logs in debug builds to avoid performance impact.
 */
fun Logger.logStateChange(
    from: String,
    to: String,
    tag: String = this.tag
) {
    d { "State change: $from -> $to" }
}
