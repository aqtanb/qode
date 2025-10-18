package com.qodein.feature.post.submission

import com.qodein.shared.common.error.SystemError
import com.qodein.shared.model.Tag
import com.qodein.shared.model.Tag.Companion.MAX_TAGS_SELECTED
import com.qodein.shared.model.User

/**
 * Top-level UI state for Post Submission screen.
 * Follows clean MVI pattern with Loading/Success/Error states.
 */
sealed interface PostSubmissionUiState {
    /**
     * Initial loading state while fetching user auth state and initial data.
     */
    data object Loading : PostSubmissionUiState

    /**
     * Success state with all form data and computed properties.
     */
    data class Success(
        // Input fields
        val title: String = "",
        val content: String = "",
        val tags: List<Tag> = emptyList(),
        val imageUris: List<String> = emptyList(),

        val tagSearchQuery: String = "",
        val availableTags: List<Tag> = emptyList(),

        // Auth state
        val authentication: PostAuthenticationState = PostAuthenticationState.Loading,

        // Submission state
        val submission: PostSubmissionState = PostSubmissionState.Idle,

        // Validation
        val validationErrors: ValidationErrors = ValidationErrors()
    ) : PostSubmissionUiState {
        val titleCharCount: Int get() = title.length
        val contentCharCount: Int get() = content.length

        val isTitleValid: Boolean get() = title.isNotBlank() && title.length <= 200
        val isContentValid: Boolean get() = content.isNotBlank() && content.length <= 2000
        val areTagsValid: Boolean get() = tags.isNotEmpty() && tags.size <= MAX_TAGS_SELECTED
        val areImagesValid: Boolean get() = imageUris.size <= 5

        val canSubmit: Boolean get() =
            isTitleValid &&
                isContentValid &&
                areTagsValid &&
                areImagesValid &&
                submission is PostSubmissionState.Idle &&
                authentication is PostAuthenticationState.Authenticated

        companion object {
            fun initial() = Success()
        }
    }

    /**
     * Error state when initial loading fails.
     */
    data class Error(val errorType: SystemError) : PostSubmissionUiState
}

/**
 * Authentication state for post submission.
 * Errors are handled via events, not stored in state.
 */
sealed interface PostAuthenticationState {
    data object Loading : PostAuthenticationState
    data object Unauthenticated : PostAuthenticationState
    data class Authenticated(val user: User) : PostAuthenticationState
}

/**
 * Submission state tracking.
 */
sealed interface PostSubmissionState {
    data object Idle : PostSubmissionState
    data object Submitting : PostSubmissionState
}

/**
 * Validation errors for form fields.
 */
data class ValidationErrors(
    val titleError: String? = null,
    val contentError: String? = null,
    val tagsError: String? = null,
    val imagesError: String? = null
) {
    val hasErrors: Boolean get() = titleError != null || contentError != null || tagsError != null || imagesError != null
}
