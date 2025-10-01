package com.qodein.feature.post.submission

import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.Tag

data class PostSubmissionUiState(
    // Input fields
    val title: String = "",
    val content: String = "",
    val tags: List<Tag> = emptyList(),
    val imageUris: List<String> = emptyList(),

    // UI state
    val isTagSelectorVisible: Boolean = false,
    val tagSearchQuery: String = "",
    val availableTags: List<Tag> = emptyList(), // Popular tags or search results

    // Submission state
    val isSubmitting: Boolean = false,
    val submissionError: OperationError? = null,

    // Validation
    val validationErrors: ValidationErrors = ValidationErrors(),

    // Auth

    val isAuthSheetVisible: Boolean = false,
    val authError: OperationError? = null
) {
    val titleCharCount: Int get() = title.length
    val contentCharCount: Int get() = content.length

    val isTitleValid: Boolean get() = title.isNotBlank() && title.length <= 200
    val isContentValid: Boolean get() = content.isNotBlank() && content.length <= 2000
    val areTagsValid: Boolean get() = tags.isNotEmpty() && tags.size <= 10
    val areImagesValid: Boolean get() = imageUris.size <= 5

    val canSubmit: Boolean get() =
        isTitleValid &&
            isContentValid &&
            areTagsValid &&
            areImagesValid &&
            !isSubmitting

    data class ValidationErrors(
        val titleError: String? = null,
        val contentError: String? = null,
        val tagsError: String? = null,
        val imagesError: String? = null
    ) {
        val hasErrors: Boolean get() = titleError != null || contentError != null || tagsError != null || imagesError != null
    }
}
