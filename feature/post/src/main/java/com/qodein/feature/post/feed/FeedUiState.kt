package com.qodein.feature.post.feed

import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.Post
import com.qodein.shared.model.Tag

/**
 * UI state for the search screen following MVI pattern
 */
sealed interface FeedUiState {
    val searchQuery: String
    val selectedTags: List<Tag>
    val suggestedTags: List<Tag>
    val isSearchFocused: Boolean

    val hasFilters: Boolean get() = searchQuery.isNotBlank() || selectedTags.isNotEmpty()

    data class Loading(
        override val searchQuery: String = "",
        override val selectedTags: List<Tag> = emptyList(),
        override val suggestedTags: List<Tag> = emptyList(),
        override val isSearchFocused: Boolean = false
    ) : FeedUiState

    data class Content(
        override val searchQuery: String = "",
        override val selectedTags: List<Tag> = emptyList(),
        override val suggestedTags: List<Tag> = emptyList(),
        override val isSearchFocused: Boolean = false,
        val posts: List<Post> = emptyList(),
        val isRefreshing: Boolean = false,
        val isLoadingMore: Boolean = false,
        val hasMorePosts: Boolean = true
    ) : FeedUiState {
        val isEmpty: Boolean get() = posts.isEmpty()
        val hasContent: Boolean get() = posts.isNotEmpty()
    }

    data class Error(
        override val searchQuery: String = "",
        override val selectedTags: List<Tag> = emptyList(),
        override val suggestedTags: List<Tag> = emptyList(),
        override val isSearchFocused: Boolean = false,
        val errorType: OperationError,
        val isRetryable: Boolean = false,
        val shouldShowSnackbar: Boolean = false,
        val errorCode: String? = null
    ) : FeedUiState

    companion object {
        fun initial(): FeedUiState = Loading()
    }
}
