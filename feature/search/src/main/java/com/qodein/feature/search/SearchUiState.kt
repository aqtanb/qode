package com.qodein.feature.search

import com.qodein.shared.common.result.ErrorType
import com.qodein.shared.model.Post
import com.qodein.shared.model.Tag

/**
 * UI state for the search screen following MVI pattern
 */
sealed interface SearchUiState {
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
    ) : SearchUiState

    data class Content(
        override val searchQuery: String = "",
        override val selectedTags: List<Tag> = emptyList(),
        override val suggestedTags: List<Tag> = emptyList(),
        override val isSearchFocused: Boolean = false,
        val posts: List<Post> = emptyList(),
        val isRefreshing: Boolean = false,
        val isLoadingMore: Boolean = false,
        val hasMorePosts: Boolean = true
    ) : SearchUiState {
        val isEmpty: Boolean get() = posts.isEmpty()
        val hasContent: Boolean get() = posts.isNotEmpty()
    }

    data class Error(
        override val searchQuery: String = "",
        override val selectedTags: List<Tag> = emptyList(),
        override val suggestedTags: List<Tag> = emptyList(),
        override val isSearchFocused: Boolean = false,
        val errorType: ErrorType,
        val isRetryable: Boolean = false,
        val shouldShowSnackbar: Boolean = false,
        val errorCode: String? = null
    ) : SearchUiState

    companion object {
        fun initial(): SearchUiState = Loading()
    }
}
