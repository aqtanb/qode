package com.qodein.feature.post.submission

sealed interface PostSubmissionAction {
    data class UpdateTitle(val title: String) : PostSubmissionAction
    data class UpdateContent(val content: String) : PostSubmissionAction

    data class UpdateTag(val tagInput: String) : PostSubmissionAction
    data class AddTag(val tag: String) : PostSubmissionAction
    data class RemoveTag(val tag: String) : PostSubmissionAction

    data class RemoveImage(val index: Int) : PostSubmissionAction
    data class UpdateImageUris(val uris: List<String>) : PostSubmissionAction

    data object Submit : PostSubmissionAction

    data object NavigateBack : PostSubmissionAction

    data object RetryPostSubmission : PostSubmissionAction
}
