package com.qodein.feature.comment

/**
 * One-time events for the comment screen following MVI pattern
 */
sealed class CommentEvent {
    data object NavigateBack : CommentEvent()
    data class NavigateToProfile(val username: String) : CommentEvent()
    data class ShowError(val message: String) : CommentEvent()
    data class ShowSuccess(val message: String) : CommentEvent()
    data object CommentSubmitted : CommentEvent()
    data object ScrollToBottom : CommentEvent()
}
