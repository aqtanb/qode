package com.qodein.feature.post.feed

import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.Post
import com.qodein.shared.model.User

sealed interface FeedUiState {

    data object Loading : FeedUiState

    data class Success(val posts: PostsUiState = PostsUiState.Loading, val currentUser: User? = null) : FeedUiState

    data class Error(val error: OperationError) : FeedUiState
}

sealed interface PostsUiState {
    data object Loading : PostsUiState
    data class Success(val posts: List<Post>) : PostsUiState
    data class Error(val error: OperationError) : PostsUiState
}
