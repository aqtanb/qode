package com.qodein.shared.common.error

/**
 * Algolia-specific errors mapped from Algolia client exceptions
 * Based on Algolia API error codes (https://www.algolia.com/doc/guides/sending-events/troubleshooting/)
 */
sealed interface AlgoliaError : OperationError {
    /**
     * Invalid API key or Application ID.
     * Fix: Check Algolia credentials in configuration.
     */
    data object InvalidCredentials : AlgoliaError

    /**
     * API key doesn't have required permissions for this operation.
     * Fix: Update API key permissions in Algolia dashboard.
     */
    data object InsufficientPermissions : AlgoliaError

    /**
     * Index name doesn't exist in Algolia application.
     * Fix: Create index or check index name spelling.
     */
    data object IndexNotFound : AlgoliaError

    /**
     * Search query syntax is invalid.
     * Common when: Special characters not escaped, invalid filters.
     */
    data object InvalidQuery : AlgoliaError

    /**
     * Request exceeds size limits (query too long, too many hits requested).
     * Fix: Reduce query size or pagination limit.
     */
    data object RequestTooLarge : AlgoliaError

    /**
     * Rate limit exceeded for API operations.
     * Action: Implement exponential backoff, or upgrade Algolia plan.
     */
    data object RateLimitExceeded : AlgoliaError

    /**
     * Network error connecting to Algolia servers.
     * Action: Check internet connection, retry with backoff.
     */
    data object NetworkError : AlgoliaError

    /**
     * Request timeout - Algolia didn't respond in time.
     * Fix: Check network connection, reduce query complexity.
     */
    data object Timeout : AlgoliaError

    /**
     * Algolia server error (5xx HTTP codes).
     * Action: Retry with exponential backoff, report if persistent.
     */
    data object ServerError : AlgoliaError

    /**
     * Algolia service temporarily unavailable (maintenance, outage).
     * Action: Check Algolia status page, retry later.
     */
    data object ServiceUnavailable : AlgoliaError
}
