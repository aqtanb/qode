package com.qodein.feature.post.detail

import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.Post
import com.qodein.shared.model.UserId
import com.qodein.shared.model.VoteState

data class PostDetailUiState(
    val postState: DataState<Post> = DataState.Loading,
    val userId: UserId? = null,
    val userVoteState: VoteState = VoteState.NONE,
    val isBookmarked: Boolean = false
)

sealed interface DataState<out T> {
    data class Success<T>(val data: T) : DataState<T>
    data class Error(val error: OperationError) : DataState<Nothing>
    data object Loading : DataState<Nothing>
}
