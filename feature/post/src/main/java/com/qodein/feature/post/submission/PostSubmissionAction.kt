package com.qodein.feature.post.submission

import com.qodein.shared.model.Tag

sealed interface PostSubmissionAction {
    // Input actions
    data class UpdateTitle(val title: String) : PostSubmissionAction
    data class UpdateContent(val content: String) : PostSubmissionAction

    // Tag actions
    data class AddTag(val tag: Tag) : PostSubmissionAction
    data class RemoveTag(val tag: Tag) : PostSubmissionAction

    // Image actions
    data class RemoveImage(val index: Int) : PostSubmissionAction
    data class UpdateImageUris(val uris: List<String>) : PostSubmissionAction

    // Submission actions
    data object Submit : PostSubmissionAction

    // Navigation actions
    data object NavigateBack : PostSubmissionAction

    // Error handling
    data object ClearValidationErrors : PostSubmissionAction
    data object RetryPostSubmission : PostSubmissionAction
}
