package com.qodein.feature.post.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    fun onAction(action: FeedAction) {
        when (action) {
            is FeedAction.LoadPosts -> loadPosts()
            is FeedAction.PostClicked -> emitEvent(FeedEvent.NavigateToPost(action.postId))
            FeedAction.ProfileClicked -> {
                if (_uiState.value.currentUser == null) {
                    emitEvent(FeedEvent.NavigateToAuth)
                } else {
                    emitEvent(FeedEvent.NavigateToProfile)
                }
            }
            FeedAction.SettingsClicked -> emitEvent(FeedEvent.NavigateToSettings)
            FeedAction.RetryClicked -> loadPosts()
        }
    }

    init {
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

    private fun loadPosts(cursor: Any? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(postsState = PostsUiState.Loading) }

            val result = getPostsUseCase(cursor)

            _uiState.update { currentState ->
                val newPostsState = when (result) {
                    is Result.Success -> PostsUiState.Success(result.data.data)
                    is Result.Error -> PostsUiState.Error(result.error)
                }
                currentState.copy(postsState = newPostsState)
            }
        }
    }

    private fun emitEvent(event: FeedEvent) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }
}
