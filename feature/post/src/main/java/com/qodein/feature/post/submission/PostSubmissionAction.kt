package com.qodein.feature.post.submission

sealed interface PostSubmissionAction {
    data class UpdateTitle(val title: String) : PostSubmissionAction
    data class UpdateContent(val content: String) : PostSubmissionAction

    data class UpdateTag(val tagInput: String) : PostSubmissionAction
    data class AddTag(val tag: String) : PostSubmissionAction
    data class RemoveTag(val tag: String) : PostSubmissionAction

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
