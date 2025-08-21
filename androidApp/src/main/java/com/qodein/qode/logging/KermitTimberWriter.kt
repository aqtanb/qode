package com.qodein.qode.logging

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import timber.log.Timber

/**
 * Kermit LogWriter that bridges shared module logging to Timber.
 *
 * This allows logs from the shared module (using Kermit) to appear
 * in Timber's output, enabling:
 * - Unified log output through Timber trees
 * - Shared module logs in Crashlytics (via Timber trees)
 * - Familiar Android developer experience
 * - Easy Firebase Analytics integration later
 */
class KermitTimberWriter : LogWriter() {

    override fun log(
        severity: Severity,
        message: String,
        tag: String,
        throwable: Throwable?
    ) {
        when (severity) {
            Severity.Verbose -> Timber.tag(tag).v(throwable, message)
            Severity.Debug -> Timber.tag(tag).d(throwable, message)
            Severity.Info -> Timber.tag(tag).i(throwable, message)
            Severity.Warn -> Timber.tag(tag).w(throwable, message)
            Severity.Error -> Timber.tag(tag).e(throwable, message)
            Severity.Assert -> Timber.tag(tag).wtf(throwable, message)
        }
    }
}
