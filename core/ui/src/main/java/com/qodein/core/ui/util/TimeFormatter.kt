package com.qodein.core.ui.util

import android.text.format.DateUtils
import androidx.compose.runtime.Composable
import kotlin.time.Instant

object TimeFormatter {
    fun formatRelativeTime(timestamp: Instant): String {
        val now = System.currentTimeMillis()
        val time = timestamp.toEpochMilliseconds()

        return DateUtils.getRelativeTimeSpanString(
            time,
            now,
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE or DateUtils.FORMAT_ABBREV_ALL or DateUtils.FORMAT_NUMERIC_DATE or DateUtils.FORMAT_NO_YEAR,
        ).toString()
    }
}

/**
 * Composable function to format relative time with automatic context.
 *
 * @param timestamp The time to format
 * @return Localized relative time string
 */
@Composable
fun rememberFormattedRelativeTime(timestamp: Instant): String = TimeFormatter.formatRelativeTime(timestamp)
