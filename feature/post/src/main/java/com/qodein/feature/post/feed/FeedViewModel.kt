package com.qodein.feature.post.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qodein.shared.common.Result
import com.qodein.shared.domain.usecase.post.GetPostsUseCase
import com.qodein.shared.domain.usecase.user.GetUserByIdUseCase
import com.qodein.shared.model.User
import com.qodein.shared.model.UserId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(private val getPostsUseCase: GetPostsUseCase, private val getUserByIdUseCase: GetUserByIdUseCase) :
    ViewModel() {
    private val _uiState: MutableStateFlow<FeedUiState> = MutableStateFlow(FeedUiState.Loading)
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private val _user: MutableStateFlow<User?> = MutableStateFlow(null)
    val user: StateFlow<User?> = _user.asStateFlow()

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
        loadPosts()
    }

    fun setUserId(userId: UserId?) {
        if (userId == null) {
            _user.value = null
            return
        }
        viewModelScope.launch {
            when (val result = getUserByIdUseCase(userId.value)) {
                is Result.Success -> _user.value = result.data
                is Result.Error -> _user.value = null
            }
        }
    }

    private fun loadPosts() {
        viewModelScope.launch {
            _uiState.value = FeedUiState.Loading
            getPostsUseCase().collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.value = FeedUiState.Success(result.data.data)
                    }
                    is Result.Error -> _uiState.value = FeedUiState.Error(result.error)
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
