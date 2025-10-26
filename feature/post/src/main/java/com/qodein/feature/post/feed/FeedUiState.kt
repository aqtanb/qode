package com.qodein.feature.post.feed

import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.Post

/**
 * UI state for the search screen following MVI pattern
 */
sealed interface FeedUiState {

    data object Loading : FeedUiState

    data class Success(val posts: List<Post> = emptyList()) : FeedUiState

    data class Error(val error: OperationError) : FeedUiState
}
