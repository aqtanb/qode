package com.qodein.core.ui.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs

/**
 * Utility object for consistent string formatting across the app
 */
object StringFormatter {

    /**
     * Formats currency amounts in Kazakhstani Tenge
     *
     * @param amount Amount in tenge
     * @param showSymbol Whether to show the ₸ symbol
     * @param abbreviated Whether to use abbreviated format (K, M)
     * @return Formatted currency string
     */
    fun formatCurrency(
        amount: Int,
        showSymbol: Boolean = true,
        abbreviated: Boolean = false
    ): String {
        val formattedAmount = when {
            abbreviated && amount >= 1_000_000 -> {
                val millions = amount / 1_000_000.0
                if (millions % 1 == 0.0) {
                    "${millions.toInt()}M"
                } else {
                    String.format("%.1fM", millions)
                }
            }
            abbreviated && amount >= 1_000 -> {
                val thousands = amount / 1_000.0
                if (thousands % 1 == 0.0) {
                    "${thousands.toInt()}K"
                } else {
                    String.format("%.1fK", thousands)
                }
            }
            else -> {
                // Add spaces as thousand separators
                amount.toString().reversed()
                    .chunked(3)
                    .joinToString(" ")
                    .reversed()
            }
        }

        return if (showSymbol) "$formattedAmount ₸" else formattedAmount
    }

    /**
     * Formats discount information
     *
     * @param discountAmount Discount amount in tenge (nullable)
     * @param discountPercentage Discount percentage (nullable)
     * @param minimumOrderAmount Minimum order amount in tenge (nullable)
     * @return Formatted discount string
     */
    fun formatDiscount(
        discountAmount: Int? = null,
        discountPercentage: Int? = null,
        minimumOrderAmount: Int? = null
    ): String {
        val discountPart = when {
            discountPercentage != null -> "$discountPercentage% off"
            discountAmount != null -> "${formatCurrency(discountAmount)} off"
            else -> "Discount available"
        }

        val minimumPart = minimumOrderAmount?.let {
            " on orders above ${formatCurrency(it)}"
        } ?: ""

        return discountPart + minimumPart
    }

    /**
     * Formats time elapsed since a given date/time
     *
     * @param dateTime The past date/time
     * @param showSeconds Whether to show seconds for very recent times
     * @return Human-readable time elapsed string
     */
    fun formatTimeAgo(
        dateTime: LocalDateTime,
        showSeconds: Boolean = false
    ): String {
        val now = LocalDateTime.now()
        val seconds = ChronoUnit.SECONDS.between(dateTime, now)
        val minutes = ChronoUnit.MINUTES.between(dateTime, now)
        val hours = ChronoUnit.HOURS.between(dateTime, now)
        val days = ChronoUnit.DAYS.between(dateTime, now)
        val weeks = days / 7
        val months = days / 30
        val years = days / 365

        return when {
            years > 0 -> "${years}y ago"
            months > 0 -> "${months}mo ago"
            weeks > 0 -> "${weeks}w ago"
            days > 0 -> "${days}d ago"
            hours > 0 -> "${hours}h ago"
            minutes > 0 -> "${minutes}m ago"
            showSeconds && seconds > 5 -> "${seconds}s ago"
            else -> "just now"
        }
    }

    /**
     * Formats a date for display
     *
     * @param date The date to format
     * @param includeYear Whether to include the year
     * @param abbreviated Whether to use abbreviated month names
     * @return Formatted date string
     */
    fun formatDate(
        date: LocalDate,
        includeYear: Boolean = true,
        abbreviated: Boolean = true
    ): String {
        val pattern = when {
            includeYear && abbreviated -> "MMM dd, yyyy"
            includeYear && !abbreviated -> "MMMM dd, yyyy"
            !includeYear && abbreviated -> "MMM dd"
            else -> "MMMM dd"
        }

        return date.format(DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH))
    }

    /**
     * Formats expiry date with context (e.g., "expires in 3 days")
     *
     * @param expiryDate The expiry date
     * @return Contextual expiry string
     */
    fun formatExpiry(expiryDate: LocalDate?): String? {
        if (expiryDate == null) return null

        val today = LocalDate.now()
        val daysUntilExpiry = ChronoUnit.DAYS.between(today, expiryDate)

        return when {
            daysUntilExpiry < 0 -> "Expired ${abs(daysUntilExpiry)} days ago"
            daysUntilExpiry == 0L -> "Expires today"
            daysUntilExpiry == 1L -> "Expires tomorrow"
            daysUntilExpiry <= 7 -> "Expires in $daysUntilExpiry days"
            daysUntilExpiry <= 30 -> "Expires ${formatDate(expiryDate, includeYear = false)}"
            else -> "Expires ${formatDate(expiryDate, includeYear = true, abbreviated = true)}"
        }
    }

    /**
     * Formats user statistics for display
     *
     * @param count The count to format
     * @param singular Singular form of the unit
     * @param plural Plural form of the unit (defaults to singular + "s")
     * @return Formatted count string
     */
    fun formatCount(
        count: Int,
        singular: String,
        plural: String = "${singular}s"
    ): String {
        val formattedCount = when {
            count >= 1_000_000 -> "${count / 1_000_000}M"
            count >= 1_000 -> "${count / 1_000}K"
            else -> count.toString()
        }

        val unit = if (count == 1) singular else plural
        return "$formattedCount $unit"
    }

    /**
     * Formats follower count for display
     *
     * @param count Number of followers
     * @return Formatted follower string
     */
    fun formatFollowers(count: Int): String = formatCount(count, "follower")

    /**
     * Formats upvote count for display
     *
     * @param count Number of upvotes
     * @return Formatted upvote string
     */
    fun formatUpvotes(count: Int): String = formatCount(count, "upvote")

    /**
     * Formats promo code count for display
     *
     * @param count Number of promo codes
     * @return Formatted promo code string
     */
    fun formatPromoCodes(count: Int): String = formatCount(count, "code")

    /**
     * Formats percentage with proper handling of decimals
     *
     * @param percentage The percentage value
     * @param showSymbol Whether to include the % symbol
     * @param decimals Number of decimal places to show
     * @return Formatted percentage string
     */
    fun formatPercentage(
        percentage: Double,
        showSymbol: Boolean = true,
        decimals: Int = 0
    ): String {
        val formatted = if (decimals == 0) {
            percentage.toInt().toString()
        } else {
            String.format("%.${decimals}f", percentage)
        }

        return if (showSymbol) "$formatted%" else formatted
    }

    /**
     * Truncates text to a specified length with ellipsis
     *
     * @param text The text to truncate
     * @param maxLength Maximum length before truncation
     * @param suffix Suffix to add when truncated (default: "...")
     * @return Truncated text
     */
    fun truncateText(
        text: String,
        maxLength: Int,
        suffix: String = "..."
    ): String {
        return if (text.length <= maxLength) {
            text
        } else {
            text.take(maxLength - suffix.length) + suffix
        }
    }

    /**
     * Formats phone numbers for Kazakhstan
     *
     * @param phoneNumber Raw phone number
     * @return Formatted phone number string
     */
    fun formatPhoneNumber(phoneNumber: String): String {
        // Remove all non-digit characters
        val digits = phoneNumber.filter { it.isDigit() }

        return when {
            digits.length == 11 && digits.startsWith("7") -> {
                // Format: +7 (777) 123-45-67
                "+${digits[0]} (${digits.substring(1, 4)}) ${digits.substring(4, 7)}-${digits.substring(7, 9)}-${digits.substring(9, 11)}"
            }
            digits.length == 10 && (digits.startsWith("77") || digits.startsWith("70")) -> {
                // Add country code and format
                "+7 (${digits.substring(0, 3)}) ${digits.substring(3, 6)}-${digits.substring(6, 8)}-${digits.substring(8, 10)}"
            }
            else -> phoneNumber // Return original if format not recognized
        }
    }

    /**
     * Formats validation error messages for forms
     *
     * @param fieldName Name of the field
     * @param errorType Type of validation error
     * @param customMessage Custom error message (optional)
     * @return Formatted error message
     */
    fun formatValidationError(
        fieldName: String,
        errorType: ValidationErrorType,
        customMessage: String? = null
    ): String {
        return customMessage ?: when (errorType) {
            ValidationErrorType.Required -> "$fieldName is required"
            ValidationErrorType.TooShort -> "$fieldName is too short"
            ValidationErrorType.TooLong -> "$fieldName is too long"
            ValidationErrorType.InvalidFormat -> "$fieldName format is invalid"
            ValidationErrorType.InvalidEmail -> "Please enter a valid email address"
            ValidationErrorType.InvalidPhone -> "Please enter a valid phone number"
            ValidationErrorType.PasswordTooWeak -> "Password must be at least 8 characters with letters and numbers"
            ValidationErrorType.PasswordMismatch -> "Passwords do not match"
            ValidationErrorType.InvalidUrl -> "Please enter a valid URL"
            ValidationErrorType.InvalidDate -> "Please enter a valid date"
            ValidationErrorType.DateTooEarly -> "Date cannot be in the past"
            ValidationErrorType.DateTooLate -> "Date is too far in the future"
        }
    }

    /**
     * Formats search query highlighting
     *
     * @param text Original text
     * @param query Search query to highlight
     * @return Text with highlighted portions (basic string replacement)
     */
    fun highlightSearchQuery(
        text: String,
        query: String
    ): String {
        if (query.isBlank()) return text

        // Simple case-insensitive highlighting
        // In a real implementation, you might want to use AnnotatedString
        return text.replace(query, "**$query**", ignoreCase = true)
    }

    /**
     * Formats file sizes for display
     *
     * @param bytes Size in bytes
     * @return Human-readable file size
     */
    fun formatFileSize(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = bytes.toDouble()
        var unitIndex = 0

        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }

        return if (size % 1 == 0.0) {
            "${size.toInt()} ${units[unitIndex]}"
        } else {
            String.format("%.1f %s", size, units[unitIndex])
        }
    }

    /**
     * Formats app version for display
     *
     * @param major Major version number
     * @param minor Minor version number
     * @param patch Patch version number
     * @param buildNumber Build number (optional)
     * @return Formatted version string
     */
    fun formatVersion(
        major: Int,
        minor: Int,
        patch: Int,
        buildNumber: Int? = null
    ): String {
        val baseVersion = "$major.$minor.$patch"
        return buildNumber?.let { "$baseVersion ($it)" } ?: baseVersion
    }
}

/**
 * Enum for validation error types
 */
enum class ValidationErrorType {
    Required,
    TooShort,
    TooLong,
    InvalidFormat,
    InvalidEmail,
    InvalidPhone,
    PasswordTooWeak,
    PasswordMismatch,
    InvalidUrl,
    InvalidDate,
    DateTooEarly,
    DateTooLate
}

/**
 * Extension functions for common formatting operations
 */

/**
 * Extension function for Int to format as currency
 */
fun Int.toCurrency(
    showSymbol: Boolean = true,
    abbreviated: Boolean = false
): String {
    return StringFormatter.formatCurrency(this, showSymbol, abbreviated)
}

/**
 * Extension function for LocalDateTime to format time ago
 */
fun LocalDateTime.toTimeAgo(showSeconds: Boolean = false): String {
    return StringFormatter.formatTimeAgo(this, showSeconds)
}

/**
 * Extension function for LocalDate to format date
 */
fun LocalDate.toFormattedString(
    includeYear: Boolean = true,
    abbreviated: Boolean = true
): String {
    return StringFormatter.formatDate(this, includeYear, abbreviated)
}

/**
 * Extension function for LocalDate to format expiry
 */
fun LocalDate.toExpiryString(): String? {
    return StringFormatter.formatExpiry(this)
}

/**
 * Extension function for String to truncate text
 */
fun String.truncate(
    maxLength: Int,
    suffix: String = "..."
): String {
    return StringFormatter.truncateText(this, maxLength, suffix)
}

/**
 * Extension function for String to format phone number
 */
fun String.toFormattedPhone(): String {
    return StringFormatter.formatPhoneNumber(this)
}

/**
 * Extension function for Long to format file size
 */
fun Long.toFileSize(): String {
    return StringFormatter.formatFileSize(this)
}
