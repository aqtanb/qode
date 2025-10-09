package com.qodein.feature.post.submission

import com.qodein.shared.common.error.OperationError

/**
 * Events for Post Submission screen.
 * One-time side effects like navigation and error display.
 */
sealed interface PostSubmissionEvent {
    data object NavigateBack : PostSubmissionEvent
    data object PostSubmitted : PostSubmissionEvent
    data class ShowError(val error: OperationError) : PostSubmissionEvent
    data object OpenImagePicker : PostSubmissionEvent
}
