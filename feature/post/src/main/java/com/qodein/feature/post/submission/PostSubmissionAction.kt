package com.qodein.feature.post.submission

import com.qodein.shared.model.Tag
import com.qodein.shared.model.User

sealed interface PostSubmissionAction {
    // Input actions
    data class UpdateTitle(val title: String) : PostSubmissionAction
    data class UpdateContent(val content: String) : PostSubmissionAction

    // Tag actions
    data class AddTag(val tag: Tag) : PostSubmissionAction
    data class RemoveTag(val tag: Tag) : PostSubmissionAction
    data object ShowTagSelector : PostSubmissionAction
    data object HideTagSelector : PostSubmissionAction
    data class SearchTags(val query: String) : PostSubmissionAction

    // Image actions
    data class RemoveImage(val index: Int) : PostSubmissionAction
    data class UpdateImageUris(val uris: List<String>) : PostSubmissionAction

    // Submission actions
    data object Submit : PostSubmissionAction
    data class SubmitWithUser(val user: User) : PostSubmissionAction

    // Navigation actions
    data object NavigateBack : PostSubmissionAction

    // Auth actions
    data object SignInWithGoogle : PostSubmissionAction
    data object DismissAuthSheet : PostSubmissionAction

    // Error handling
    data object ClearValidationErrors : PostSubmissionAction
    data object RetryPostSubmission : PostSubmissionAction
}
