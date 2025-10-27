package com.qodein.feature.post.detail

import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.Post

data class PostDetailUiState(val postState: DataState<Post> = DataState.Loading)

sealed interface DataState<out T> {
    data class Success<T>(val data: T) : DataState<T>
    data class Error(val error: OperationError) : DataState<Nothing>
    data object Loading : DataState<Nothing>
}
