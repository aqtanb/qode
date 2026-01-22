package com.qodein.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.UserError
import com.qodein.shared.domain.usecase.auth.SignOutUseCase
import com.qodein.shared.domain.usecase.post.GetPostsByUserUseCase
import com.qodein.shared.domain.usecase.promocode.GetPromocodesByUserUseCase
import com.qodein.shared.domain.usecase.user.ObserveCurrentUserUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val getPromocodesByUserUseCase: GetPromocodesByUserUseCase,
    private val getPostsByUserUseCase: GetPostsByUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProfileEvent>()
    val events = _events.asSharedFlow()

    init {
        observeUser()
    }

    fun onAction(action: ProfileAction) {
        when (action) {
            ProfileAction.SignOutClicked -> signOut()
            ProfileAction.BlockedClicked -> {
                emitEvent(ProfileEvent.NavigateToBlockedUsers)
            }
            ProfileAction.LoadMorePosts -> loadMorePosts()
            ProfileAction.LoadMorePromocodes -> loadMorePromocodes()
            is ProfileAction.PostClicked -> {
                emitEvent(ProfileEvent.NavigateToPostDetail(action.postId))
            }
            is ProfileAction.PromocodeClicked -> {
                emitEvent(ProfileEvent.NavigateToPromocodeDetail(action.promocodeId))
            }
            ProfileAction.RetryPostsClicked -> getPosts()
            ProfileAction.RetryPromocodesClicked -> getPromocodes()
            is ProfileAction.TabSelected -> {
                switchTabs(action.tab)
            }
        }
    }

    private fun emitEvent(event: ProfileEvent) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }

    private fun observeUser() {
        observeCurrentUserUseCase()
            .onEach { result ->
                when (result) {
                    is Result.Success -> {
                        val shouldLoadData = _uiState.value.user == null
                        _uiState.update { it.copy(user = result.data) }

                        if (shouldLoadData) {
                            getPromocodes()
                        }
                    }
                    is Result.Error -> {
                        if (result.error is UserError.AuthenticationFailure) {
                            emitEvent(ProfileEvent.SignedOut)
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun getPromocodes() {
        val user = _uiState.value.user ?: return
        _uiState.update { it.copy(promocodesState = PaginatedDataState.Loading) }
        viewModelScope.launch {
            when (val result = getPromocodesByUserUseCase(userId = user.id)) {
                is Result.Error -> _uiState.update { it.copy(promocodesState = PaginatedDataState.Error(result.error)) }
                is Result.Success -> _uiState.update {
                    it.copy(
                        promocodesState = PaginatedDataState.Success(
                            items = result.data.data,
                            hasMore = result.data.hasMore,
                            nextCursor = result.data.nextCursor,
                        ),
                    )
                }
            }
        }
    }

    private fun loadMorePromocodes() {
        val user = _uiState.value.user ?: return
        val promocodesState = _uiState.value.promocodesState as? PaginatedDataState.Success ?: return

        if (!promocodesState.hasMore || promocodesState.isLoadingMore) return

        _uiState.update {
            it.copy(promocodesState = promocodesState.copy(isLoadingMore = true))
        }

        viewModelScope.launch {
            when (
                val result = getPromocodesByUserUseCase(
                    userId = user.id,
                    cursor = promocodesState.nextCursor,
                )
            ) {
                is Result.Error -> _uiState.update {
                    it.copy(promocodesState = promocodesState.copy(isLoadingMore = false))
                }
                is Result.Success -> _uiState.update {
                    it.copy(
                        promocodesState = PaginatedDataState.Success(
                            items = promocodesState.items + result.data.data,
                            hasMore = result.data.hasMore,
                            nextCursor = result.data.nextCursor,
                            isLoadingMore = false,
                        ),
                    )
                }
            }
        }
    }

    private fun getPosts() {
        val user = _uiState.value.user ?: return
        _uiState.update { it.copy(postsState = PaginatedDataState.Loading) }
        viewModelScope.launch {
            when (val result = getPostsByUserUseCase(userId = user.id)) {
                is Result.Error -> _uiState.update { it.copy(postsState = PaginatedDataState.Error(result.error)) }
                is Result.Success -> _uiState.update {
                    it.copy(
                        postsState = PaginatedDataState.Success(
                            result.data.data,
                            result.data.hasMore,
                            result.data.nextCursor,
                        ),
                    )
                }
            }
        }
    }

    private fun loadMorePosts() {
        val user = _uiState.value.user ?: return
        val postsState = _uiState.value.postsState as? PaginatedDataState.Success ?: return

        if (!postsState.hasMore || postsState.isLoadingMore) return

        _uiState.update { it.copy(postsState = postsState.copy(isLoadingMore = true)) }

        viewModelScope.launch {
            when (val result = getPostsByUserUseCase(userId = user.id, cursor = postsState.nextCursor)) {
                is Result.Error -> _uiState.update { it.copy(postsState = postsState.copy(isLoadingMore = false)) }
                is Result.Success -> _uiState.update {
                    it.copy(
                        postsState = PaginatedDataState.Success(
                            postsState.items + result.data.data,
                            result.data.hasMore,
                            result.data.nextCursor,
                            isLoadingMore = false,
                        ),
                    )
                }
            }
        }
    }

    private fun switchTabs(tab: ProfileTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        if (tab == ProfileTab.POSTS && _uiState.value.postsState !is PaginatedDataState.Success) {
            getPosts()
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            signOutUseCase()
            emitEvent(ProfileEvent.SignedOut)
        }
    }
}
