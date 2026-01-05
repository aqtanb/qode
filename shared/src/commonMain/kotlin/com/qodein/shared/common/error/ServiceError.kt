package com.qodein.shared.common.error

/**
 * Domain errors for Service operations.
 * User-focused hierarchical errors that abstract away implementation details.
 */
sealed interface ServiceError : OperationError {

    /**
     * Failures when user tries to create a service (client-side validation).
     */
    sealed interface CreationFailure : ServiceError {
        data object EmptyName : CreationFailure
        data object NameTooShort : CreationFailure
        data object NameTooLong : CreationFailure
        data object InvalidServiceId : CreationFailure
        data object EmptySiteUrl : CreationFailure
        data object LogoNotFound : CreationFailure
        data object InvalidDomainFormat : CreationFailure
    }

    /**
     * Failures when submitting a service to the backend (server-side rejection).
     */
    sealed interface SubmissionFailure : ServiceError {
        data object DuplicateService : SubmissionFailure
        data object NotAuthorized : SubmissionFailure
        data object InvalidData : SubmissionFailure
    }

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
