package com.qodein.feature.post.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qodein.shared.common.Result
import com.qodein.shared.domain.usecase.post.GetPostsUseCase
import com.qodein.shared.domain.usecase.user.GetUserByIdUseCase
import com.qodein.shared.domain.usecase.user.ObserveCurrentUserUseCase
import com.qodein.shared.model.UserId
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

class FeedViewModel(
    private val getPostsUseCase: GetPostsUseCase,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
) : ViewModel() {

    private val _uiState: MutableStateFlow<FeedUiState> = MutableStateFlow(FeedUiState.Loading)
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private val _events: MutableSharedFlow<FeedEvent> = MutableSharedFlow()
    val events: SharedFlow<FeedEvent> = _events.asSharedFlow()

    fun onAction(action: FeedAction) {
        when (action) {
            is FeedAction.LoadPosts -> loadPosts()
            is FeedAction.PostClicked -> emitEvent(FeedEvent.NavigateToPost(action.postId))
            FeedAction.ProfileClicked -> emitEvent(FeedEvent.NavigateToProfile)
            FeedAction.SettingsClicked -> emitEvent(FeedEvent.NavigateToSettings)
            FeedAction.RetryClicked -> loadPosts()
        }
    }

    init {
        observeCurrentUser()
        loadPosts()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            observeCurrentUserUseCase().onEach { result ->
                _uiState.update { currentState ->
                    val user = (result as? Result.Success)?.data

                    when (currentState) {
                        is FeedUiState.Success -> {
                            currentState.copy(currentUser = user)
                        }
                        else -> {
                            FeedUiState.Success(currentUser = user)
                        }
                    }
                }
            }
                .launchIn(viewModelScope)
        }
    }

    private fun loadPosts(cursor: Any? = null) {
        viewModelScope.launch {
            _uiState.update { FeedUiState.Loading }
            val result = getPostsUseCase(cursor)

            _uiState.update { currentState ->
                val postsState = when (result) {
                    is Result.Success -> PostsUiState.Success(result.data.data)
                    is Result.Error -> PostsUiState.Error(result.error)
                }

                when (currentState) {
                    is FeedUiState.Success -> currentState.copy(posts = postsState)
                    else -> FeedUiState.Success(posts = postsState)
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
