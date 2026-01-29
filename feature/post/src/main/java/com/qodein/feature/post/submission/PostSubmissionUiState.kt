package com.qodein.feature.post.submission

import com.qodein.shared.model.Post

data class PostSubmissionUiState(
    val title: String = "",
    val content: String = "",
    val tagInput: String = "",
    val tags: List<String> = emptyList(),
    val imageUris: List<String> = emptyList(),
    val isSubmitting: Boolean = false,
    val compressingImages: Boolean = false
) {
    val isTitleValid: Boolean get() = title.isNotBlank() && title.length <= Post.TITLE_MAX_LENGTH
    val isContentValid: Boolean get() = content.length <= Post.CONTENT_MAX_LENGTH
    val imageCountIsValid: Boolean get() = imageUris.size <= Post.MAX_IMAGES

    val canSubmit: Boolean get() =
        isTitleValid &&
            isContentValid &&
            imageCountIsValid &&
            !isSubmitting &&
            !compressingImages
}
