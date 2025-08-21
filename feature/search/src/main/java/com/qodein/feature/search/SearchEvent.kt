package com.qodein.feature.search

import com.qodein.shared.model.PostId

/**
 * One-time events for the search screen following MVI pattern
 */
sealed class SearchEvent {
    data class NavigateToPost(val postId: PostId) : SearchEvent()
    data class NavigateToComments(val postId: PostId) : SearchEvent()
    data class NavigateToProfile(val username: String) : SearchEvent()
    data class ShowShareDialog(val postId: PostId) : SearchEvent()
    data class ShowError(val message: String) : SearchEvent()
    data class ShowSuccess(val message: String) : SearchEvent()
}
