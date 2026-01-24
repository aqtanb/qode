package com.qodein.feature.post.detail

import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.Post
import com.qodein.shared.model.UserId
import com.qodein.shared.model.VoteState

data class PostDetailUiState(
    val postState: PostUiState = PostUiState.Loading,
    val currentUserId: UserId? = null,
    val userVoteState: VoteState = VoteState.NONE,
    val isBookmarked: Boolean = false,
    val isSigningIn: Boolean = false
)

sealed interface PostUiState {
    data class Success(val post: Post) : PostUiState
    data class Error(val error: OperationError) : PostUiState
    data object Loading : PostUiState
}
