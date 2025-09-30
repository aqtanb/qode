package com.qodein.shared.common.error

/**
 * Domain errors for Post operations.
 * User-focused hierarchical errors that abstract away implementation details.
 */
sealed interface PostError : OperationError {

    /**
     * Failures when user tries to create/submit a post (client-side validation).
     */
    sealed interface CreationFailure : PostError {
        data object EmptyTitle : CreationFailure
        data object TitleTooLong : CreationFailure
        data object EmptyContent : CreationFailure
        data object ContentTooLong : CreationFailure
        data object EmptyAuthorName : CreationFailure
        data object TooManyTags : CreationFailure
        data object TooManyImages : CreationFailure
        data object InvalidTagData : CreationFailure
    }

    /**
     * Failures when submitting a post to the backend (server-side rejection).
     */
    sealed interface SubmissionFailure : PostError {
        data object NotAuthorized : SubmissionFailure
        data object InvalidData : SubmissionFailure
    }

    /**
     * Failures when user tries to get/view posts.
     */
    sealed interface RetrievalFailure : PostError {
        data object NotFound : RetrievalFailure
        data object NoResults : RetrievalFailure
        data object AccessDenied : RetrievalFailure
    }
}
