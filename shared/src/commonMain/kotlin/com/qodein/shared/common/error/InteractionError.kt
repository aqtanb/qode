package com.qodein.shared.common.error

/**
 * Domain errors for User Interaction operations (votes, bookmarks).
 * User-focused hierarchical errors that abstract away implementation details.
 */
sealed interface InteractionError : OperationError {

    /**
     * Failures when user tries to vote on content.
     */
    sealed interface VotingFailure : InteractionError {
        data object NotAuthorized : VotingFailure
        data object AlreadyVoted : VotingFailure
        data object ContentNotFound : VotingFailure
        data object SaveFailed : VotingFailure
    }

    /**
     * Failures when user tries to bookmark content.
     */
    sealed interface BookmarkFailure : InteractionError {
        data object NotAuthorized : BookmarkFailure
        data object ContentNotFound : BookmarkFailure
        data object SaveFailed : BookmarkFailure
        data object RemoveFailed : BookmarkFailure
    }

    /**
     * System-level failures that prevent any interaction operation.
     */
    sealed interface SystemFailure : InteractionError {
        data object Offline : SystemFailure
        data object ServiceDown : SystemFailure
        data object Unknown : SystemFailure
    }
}
