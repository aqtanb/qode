package com.qodein.feature.profile

import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.PaginationCursor
import com.qodein.shared.model.Post
import com.qodein.shared.model.Promocode
import com.qodein.shared.model.SortBy
import com.qodein.shared.model.User

enum class ProfileTab {
    PROMOCODES,
    POSTS
}

sealed interface ProfileUiState {
    data class Success(
        val user: User,
        val selectedTab: ProfileTab = ProfileTab.PROMOCODES,
        val promocodesState: PaginatedDataState<Promocode> = PaginatedDataState.Loading,
        val postsState: PaginatedDataState<Post> = PaginatedDataState.Loading
    ) : ProfileUiState

    data object Loading : ProfileUiState

    data class Error(val errorType: OperationError) : ProfileUiState
}

sealed interface PaginatedDataState<out T> {
    data object Loading : PaginatedDataState<Nothing>

    data class Success<T>(
        val items: List<T>,
        val hasMore: Boolean,
        val nextCursor: PaginationCursor<SortBy>?,
        val isLoadingMore: Boolean = false
    ) : PaginatedDataState<T>

    data class Error(val error: OperationError) : PaginatedDataState<Nothing>
}
