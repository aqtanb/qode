package com.qodein.shared.common.error

/**
 * Domain errors for Service operations.
 * User-focused hierarchical errors that abstract away implementation details.
 */
sealed interface ServiceError : OperationError {

    /**
     * Failures when user tries to search/discover services.
     */
    sealed interface SearchFailure : ServiceError {
        data object NoResults : SearchFailure
        data object QueryTooShort : SearchFailure
        data object TooManyResults : SearchFailure
        data object InvalidQuery : SearchFailure
    }

    /**
     * Failures when user tries to get service details/data.
     */
    sealed interface RetrievalFailure : ServiceError {
        data object NotFound : RetrievalFailure
        data object DataCorrupted : RetrievalFailure
        data object CacheExpired : RetrievalFailure
    }
}
