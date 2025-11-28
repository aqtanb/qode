package com.qodein.feature.post.detail

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.qodein.core.ui.auth.IdTokenProvider
import com.qodein.core.ui.component.AuthPromptAction
import com.qodein.feature.post.navigation.PostDetailRoute
import com.qodein.shared.common.Result
import com.qodein.shared.domain.AuthState
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import com.qodein.shared.domain.usecase.auth.SignInWithGoogleUseCase
import com.qodein.shared.domain.usecase.interaction.GetUserInteractionUseCase
import com.qodein.shared.domain.usecase.interaction.ToggleVoteUseCase
import com.qodein.shared.domain.usecase.post.GetPostByIdUseCase
import com.qodein.shared.model.ContentType
import com.qodein.shared.model.PostId
import com.qodein.shared.model.UserId
import com.qodein.shared.model.VoteState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPostByIdUseCase: GetPostByIdUseCase,
    private val getUserInteractionUseCase: GetUserInteractionUseCase,
    private val toggleVoteUseCase: ToggleVoteUseCase,
    private val getAuthStateUseCase: GetAuthStateUseCase,
    private val idTokenProvider: IdTokenProvider,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase
) : ViewModel() {
    private val _uiState: MutableStateFlow<PostDetailUiState> = MutableStateFlow(PostDetailUiState())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    private val _events: MutableSharedFlow<PostDetailEvent> = MutableSharedFlow()
    val events: SharedFlow<PostDetailEvent> = _events.asSharedFlow()

    private val args: PostDetailRoute = savedStateHandle.toRoute()
    private val postId: PostId = PostId(args.postId)

    init {
        viewModelScope.launch {
            observeAuthState()
            loadPost()
        }
    }

    internal fun onAction(action: PostDetailAction) {
        when (action) {
            is PostDetailAction.UpvoteClicked -> {
                val currentUserId = _uiState.value.userId
                if (currentUserId == null) {
                    viewModelScope.launch {
                        _events.emit(PostDetailEvent.ShowAuthPrompt(authPromptAction = AuthPromptAction.UpvotePrompt))
                    }
                } else {
                    handleUpvote(
                        itemId = action.postId,
                        userId = currentUserId,
                        currentVoteState = action.currentVoteState,
                    )
                }
            }
            is PostDetailAction.DownvoteClicked -> {
                val currentUserId = _uiState.value.userId
                if (currentUserId == null) {
                    viewModelScope.launch {
                        _events.emit(PostDetailEvent.ShowAuthPrompt(AuthPromptAction.DownvotePrompt))
                    }
                } else {
                    handleDownVote(
                        itemId = action.postId,
                        userId = currentUserId,
                        currentVoteState = action.currentVoteState,
                    )
                }
            }

            is PostDetailAction.SignInWithGoogleClicked -> signInWithGoogle(action.context)
        }
    }

    private suspend fun loadPost() {
        when (val result = getPostByIdUseCase(postId)) {
            is Result.Error -> _uiState.update { it.copy(postState = DataState.Error(result.error)) }
            is Result.Success -> _uiState.update { it.copy(postState = DataState.Success(result.data)) }
        }
    }

    private suspend fun loadUserInteractions(
        itemId: String,
        userId: UserId
    ) {
        when (val result = getUserInteractionUseCase(itemId, userId)) {
            is Result.Error -> _events.emit(PostDetailEvent.ShowError(result.error))
            is Result.Success -> _uiState.update {
                it.copy(
                    userVoteState = result.data?.voteState ?: VoteState.NONE,
                    isBookmarked = result.data?.isBookmarked ?: false,
                )
            }
        }
    }

    private fun handleUpvote(
        itemId: String,
        userId: UserId,
        currentVoteState: VoteState
    ) {
        viewModelScope.launch {
            toggleVoteUseCase.toggleUpvote(
                itemId = itemId,
                itemType = ContentType.POST,
                userId = userId,
                currentVoteState = currentVoteState,
            )
        }
    }

    private fun handleDownVote(
        itemId: String,
        userId: UserId,
        currentVoteState: VoteState
    ) {
        viewModelScope.launch {
            toggleVoteUseCase.toggleDownvote(
                itemId = itemId,
                itemType = ContentType.POST,
                userId = userId,
                currentVoteState = currentVoteState,
            )
        }
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            getAuthStateUseCase().collectLatest { authState ->
                when (authState) {
                    is AuthState.Authenticated -> {
                        _uiState.update { it.copy(userId = authState.user.id) }
                        loadUserInteractions(
                            userId = authState.user.id,
                            itemId = args.postId,
                        )
                    }

                    AuthState.Unauthenticated -> {
                        _uiState.update { it.copy(userId = null, userVoteState = VoteState.NONE, isBookmarked = false) }
                    }
                }
            }
        }
    }

    private fun signInWithGoogle(context: Context) {
        _uiState.update { it.copy(isSigningIn = true) }
        viewModelScope.launch {
            when (val tokenResult = idTokenProvider.getIdToken(context)) {
                is Result.Error -> {
                    _uiState.update { it.copy(isSigningIn = false) }
                    _events.emit(PostDetailEvent.ShowError(tokenResult.error))
                }
                is Result.Success -> {
                    when (val signInResult = signInWithGoogleUseCase(tokenResult.data)) {
                        is Result.Success -> {
                            _uiState.update { it.copy(isSigningIn = false) }
                        }
                        is Result.Error -> {
                            _uiState.update { it.copy(isSigningIn = false) }
                            _events.emit(PostDetailEvent.ShowError(signInResult.error))
                        }
                    }
                }
            }
        }
    }
}
