package com.qodein.feature.post.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.qodein.core.ui.AuthPromptAction
import com.qodein.feature.post.navigation.PostDetailRoute
import com.qodein.shared.common.Result
import com.qodein.shared.domain.AuthState
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import com.qodein.shared.domain.usecase.interaction.GetUserInteractionUseCase
import com.qodein.shared.domain.usecase.interaction.ToggleVoteUseCase
import com.qodein.shared.domain.usecase.post.GetPostByIdUseCase
import com.qodein.shared.model.ContentType
import com.qodein.shared.model.PostId
import com.qodein.shared.model.UserId
import com.qodein.shared.model.VoteState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PostDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val getPostByIdUseCase: GetPostByIdUseCase,
    private val getUserInteractionUseCase: GetUserInteractionUseCase,
    private val toggleVoteUseCase: ToggleVoteUseCase,
    private val getAuthStateUseCase: GetAuthStateUseCase
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
            is PostDetailAction.BlockUserClicked -> handleBlockUser(action.userId)
            is PostDetailAction.ReportPostClicked -> handleReportPost(action.postId)
            is PostDetailAction.ToggleVoteClicked -> toggleVote(action.voteState)
        }
    }

    private suspend fun loadPost() {
        when (val result = getPostByIdUseCase(postId)) {
            is Result.Error -> _uiState.update { it.copy(postState = PostUiState.Error(result.error)) }
            is Result.Success -> _uiState.update { it.copy(postState = PostUiState.Success(result.data)) }
        }
    }

    private suspend fun loadUserInteractions(
        itemId: String,
        userId: UserId
    ) {
        when (val result = getUserInteractionUseCase(itemId, ContentType.POST, userId)) {
            is Result.Error -> _events.emit(PostDetailEvent.ShowError(result.error))
            is Result.Success -> _uiState.update {
                it.copy(
                    userVoteState = result.data?.voteState ?: VoteState.NONE,
                    isBookmarked = result.data?.isBookmarked ?: false,
                )
            }
        }
    }

    private fun toggleVote(targetVoteState: VoteState) {
        viewModelScope.launch {
            val userId = _uiState.value.currentUserId
            if (userId == null) {
                _events.emit(PostDetailEvent.NavigateToAuth(AuthPromptAction.Vote))
                return@launch
            }

            val currentVoteState = _uiState.value.userVoteState
            val optimisticVoteState = currentVoteState.toggleTo(targetVoteState)
            val scoreDelta = VoteState.computeScoreDelta(currentVoteState, optimisticVoteState)
            val currentDelta = _uiState.value.voteScoreDelta

            _uiState.update {
                it.copy(
                    userVoteState = optimisticVoteState,
                    voteScoreDelta = currentDelta + scoreDelta,
                )
            }

            when (
                val result = toggleVoteUseCase(
                    itemId = postId.value,
                    itemType = ContentType.POST,
                    userId = userId,
                    currentVoteState = currentVoteState,
                    isBookmarked = _uiState.value.isBookmarked,
                    targetVoteState = targetVoteState,
                )
            ) {
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            userVoteState = currentVoteState,
                            voteScoreDelta = currentDelta,
                        )
                    }
                    _events.emit(PostDetailEvent.ShowError(result.error))
                }
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            userVoteState = result.data.voteState,
                            isBookmarked = result.data.isBookmarked,
                        )
                    }
                }
            }
        }
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            getAuthStateUseCase().collectLatest { authState ->
                when (authState) {
                    is AuthState.Authenticated -> {
                        _uiState.update { it.copy(currentUserId = authState.userId) }
                        loadUserInteractions(
                            userId = authState.userId,
                            itemId = postId.value,
                        )
                    }

                    AuthState.Unauthenticated -> {
                        _uiState.update { it.copy(currentUserId = null, userVoteState = VoteState.NONE, isBookmarked = false) }
                    }
                }
            }
        }
    }

    private fun handleBlockUser(userId: UserId) {
        val postState = _uiState.value.postState
        if (postState !is PostUiState.Success) return
        val currentPost = postState.post

        viewModelScope.launch {
            _events.emit(
                PostDetailEvent.NavigateToBlockUser(
                    userId = userId,
                    username = currentPost.authorName,
                    photoUrl = currentPost.authorAvatarUrl,
                ),
            )
        }
    }

    private fun handleReportPost(postId: String) {
        val postState = _uiState.value.postState
        if (postState !is PostUiState.Success) return
        val currentPost = postState.post

        viewModelScope.launch {
            _uiState.value.currentUserId ?: run {
                _events.emit(PostDetailEvent.NavigateToAuth(AuthPromptAction.ReportContent))
                return@launch
            }

            _events.emit(
                PostDetailEvent.NavigateToReport(
                    reportedItemId = postId,
                    itemTitle = currentPost.title,
                    itemAuthor = currentPost.authorName,
                ),
            )
        }
    }
}
