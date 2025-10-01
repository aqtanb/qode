package com.qodein.feature.post.submission

sealed interface PostSubmissionEvent {
    data object NavigateBack : PostSubmissionEvent
    data class ShowSnackbar(val message: String) : PostSubmissionEvent
    data object OpenImagePicker : PostSubmissionEvent
}
