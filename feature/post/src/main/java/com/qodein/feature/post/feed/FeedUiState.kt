package com.qodein.feature.post.feed

import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.PaginationCursor
import com.qodein.shared.model.Post
import com.qodein.shared.model.SortBy
import com.qodein.shared.model.User

data class FeedUiState(val currentUser: User? = null, val postsState: PostsUiState = PostsUiState.Loading)

sealed interface PostsUiState {
    object Loading : PostsUiState
    data class Success(
        val posts: List<Post>,
        val hasMore: Boolean,
        val nextCursor: PaginationCursor<SortBy>?,
        val isLoadingMore: Boolean = false
    ) : PostsUiState
    data class Error(val error: OperationError) : PostsUiState
}
