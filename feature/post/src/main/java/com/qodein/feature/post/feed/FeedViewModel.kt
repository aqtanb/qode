package com.qodein.feature.post.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.qodein.shared.common.Result
import com.qodein.shared.domain.usecase.post.GetPostsUseCase
import com.qodein.shared.domain.usecase.user.ObserveCurrentUserUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FeedViewModel(private val getPostsUseCase: GetPostsUseCase, private val observeCurrentUserUseCase: ObserveCurrentUserUseCase) :
    ViewModel() {

    private val _uiState: MutableStateFlow<FeedUiState> = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private val _events: MutableSharedFlow<FeedEvent> = MutableSharedFlow()
    val events: SharedFlow<FeedEvent> = _events.asSharedFlow()

    private var savedScrollIndex = 0
    private var savedScrollOffset = 0

    fun getSavedScrollIndex(): Int = savedScrollIndex
    fun getSavedScrollOffset(): Int = savedScrollOffset

    fun saveScrollPosition(
        index: Int,
        offset: Int
    ) {
        savedScrollIndex = index
        savedScrollOffset = offset
    }

    fun onAction(action: FeedAction) {
        when (action) {
            is FeedAction.LoadPosts -> loadPosts()
            is FeedAction.LoadMorePosts -> loadMorePosts()
            is FeedAction.PostClicked -> emitEvent(FeedEvent.NavigateToPost(action.postId))
            FeedAction.ProfileClicked -> {
                if (_uiState.value.currentUser == null) {
                    emitEvent(FeedEvent.NavigateToAuth)
                } else {
                    emitEvent(FeedEvent.NavigateToProfile)
                }
            }
            FeedAction.SettingsClicked -> emitEvent(FeedEvent.NavigateToSettings)
            FeedAction.RetryClicked -> handleRefresh()
            FeedAction.RefreshData -> handleRefresh()
        }
    }

    fun handleRefresh() {
        Logger.d { "Refreshing posts" }
        _uiState.update { it.copy(isRefreshing = true) }
        loadPosts()
    }

    init {
        Logger.d { "Initializing feed view model" }
        observeCurrentUser()
        loadPosts()
    }

    private fun observeCurrentUser() {
        observeCurrentUserUseCase()
            .onEach { result ->
                val user = (result as? Result.Success)?.data
                _uiState.update { it.copy(currentUser = user) }
            }
            .launchIn(viewModelScope)
    }

    private fun loadPosts() {
        viewModelScope.launch {
            _uiState.update { it.copy(postsState = PostsUiState.Loading) }

            val result = getPostsUseCase(cursor = null)

            _uiState.update { currentState ->
                val newPostsState = when (result) {
                    is Result.Success -> PostsUiState.Success(
                        posts = result.data.data,
                        hasMore = result.data.hasMore,
                        nextCursor = result.data.nextCursor,
                    )
                    is Result.Error -> PostsUiState.Error(result.error)
                }
                currentState.copy(postsState = newPostsState, isRefreshing = false)
            }
        }
    }

    private fun loadMorePosts() {
        val postsState = _uiState.value.postsState as? PostsUiState.Success ?: return

        if (!postsState.hasMore || postsState.isLoadingMore) return

        _uiState.update { it.copy(postsState = postsState.copy(isLoadingMore = true)) }

        viewModelScope.launch {
            when (val result = getPostsUseCase(cursor = postsState.nextCursor)) {
                is Result.Error -> _uiState.update {
                    it.copy(postsState = postsState.copy(isLoadingMore = false))
                }
                is Result.Success -> _uiState.update {
                    it.copy(
                        postsState = PostsUiState.Success(
                            posts = postsState.posts + result.data.data,
                            hasMore = result.data.hasMore,
                            nextCursor = result.data.nextCursor,
                            isLoadingMore = false,
                        ),
                    )
                }
            }
        }
    }

    private fun emitEvent(event: FeedEvent) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }
}
