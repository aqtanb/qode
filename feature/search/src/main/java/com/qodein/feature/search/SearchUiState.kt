package com.qodein.feature.search

import com.qodein.shared.model.Post
import com.qodein.shared.model.Tag

/**
 * UI state for the search screen following MVI pattern
 */
data class SearchUiState(
    val searchQuery: String = "",
    val selectedTags: List<Tag> = emptyList(),
    val posts: List<Post> = emptyList(),
    val suggestedTags: List<Tag> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMorePosts: Boolean = true,
    val errorMessage: String? = null,
    val isSearchFocused: Boolean = false
) {
    val isEmpty: Boolean get() = posts.isEmpty() && !isLoading && errorMessage == null
    val hasContent: Boolean get() = posts.isNotEmpty()
    val isInitialLoad: Boolean get() = isLoading && posts.isEmpty()
    val hasFilters: Boolean get() = searchQuery.isNotBlank() || selectedTags.isNotEmpty()

    companion object {
        fun initial() = SearchUiState()
    }
}
