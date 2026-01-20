package com.qodein.feature.post.submission

import android.app.Application
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.qodein.core.ui.error.toUiText
import com.qodein.core.ui.util.ImageCompressor
import com.qodein.shared.common.Result
import com.qodein.shared.domain.AuthState
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import com.qodein.shared.domain.usecase.post.EnqueuePostSubmissionUseCase
import com.qodein.shared.model.Post
import com.qodein.shared.model.Post.Companion.filterTags
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PostSubmissionViewModel(
    application: Application,
    private val getAuthStateUseCase: GetAuthStateUseCase,
    private val enqueuePostSubmissionUseCase: EnqueuePostSubmissionUseCase
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<PostSubmissionUiState>(PostSubmissionUiState.Success.initial())
    val uiState: StateFlow<PostSubmissionUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<PostSubmissionEvent>()
    val events = _events.asSharedFlow()

    private var currentAuthState: AuthState = AuthState.Unauthenticated

    init {
        observeAuthState()
    }

    /**
     * Helper to update only Success state, ignoring Loading/Error states.
     */
    private inline fun updateSuccessState(update: (PostSubmissionUiState.Success) -> PostSubmissionUiState.Success) {
        _uiState.update { currentState ->
            when (currentState) {
                is PostSubmissionUiState.Success -> update(currentState)
                else -> currentState
            }
        }
    }

    fun onAction(action: PostSubmissionAction) {
        when (action) {
            is PostSubmissionAction.UpdateTitle -> {
                updateSuccessState { it.copy(title = Post.filterTitle(action.title)) }
            }
            is PostSubmissionAction.UpdateContent -> {
                updateSuccessState { it.copy(content = Post.filterContent(action.content)) }
            }

            is PostSubmissionAction.UpdateTag -> {
                updateTag(action.tagInput)
            }
            is PostSubmissionAction.AddTag -> addTag(action.tag)
            is PostSubmissionAction.RemoveTag -> removeTag(action.tag)

            is PostSubmissionAction.RemoveImage -> removeImage(action.index)
            is PostSubmissionAction.UpdateImageUris -> updateImageUris(action.uris)

            PostSubmissionAction.Submit -> submitPost()

            PostSubmissionAction.NavigateBack -> navigateBack()
            PostSubmissionAction.RetryPostSubmission -> submitPost()
        }
    }

    private fun updateTag(tagInput: String) {
        if (tagInput.endsWith(" ") || tagInput.endsWith(",")) {
            val cleanTag = Post.filterTagInput(tagInput)
            if (cleanTag.isNotEmpty()) {
                onAction(PostSubmissionAction.AddTag(cleanTag))
            }
        } else {
            val filtered = Post.filterTagInput(tagInput)
            updateSuccessState { it.copy(tagInput = filtered) }
        }
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            getAuthStateUseCase().collect { authState ->
                currentAuthState = authState
            }
        }
    }
    private fun addTag(tag: String) {
        updateSuccessState { state ->
            state.copy(tags = filterTags(state.tags + tag), tagInput = "")
        }
    }

    private fun removeTag(tag: String) {
        updateSuccessState { state ->
            state.copy(tags = state.tags - tag)
        }
    }

    private fun removeImage(index: Int) {
        updateSuccessState { state ->
            state.copy(imageUris = state.imageUris.filterIndexed { i, _ -> i != index })
        }
    }

    private fun updateImageUris(uris: List<String>) {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState !is PostSubmissionUiState.Success) return@launch

            val availableSlots = 5 - currentState.imageUris.size
            val urisToCompress = uris.take(availableSlots).map { it.toUri() }

            if (urisToCompress.isEmpty()) return@launch

            Logger.d { "Starting compression for ${urisToCompress.size} images" }

            // Update state to show compression progress
            updateSuccessState {
                it.copy(compression = ImageCompressionState.Compressing(0, urisToCompress.size))
            }

            val result = ImageCompressor.compressImages(
                context = getApplication(),
                uris = urisToCompress,
                onProgress = { current, total ->
                    updateSuccessState {
                        it.copy(compression = ImageCompressionState.Compressing(current, total))
                    }
                },
            )

            when (result) {
                is Result.Success -> {
                    Logger.i { "Successfully compressed ${result.data.size} images" }
                    val compressedUriStrings = result.data.map { it.toString() }
                    updateSuccessState { state ->
                        state.copy(
                            imageUris = state.imageUris + compressedUriStrings,
                            compression = ImageCompressionState.Idle,
                        )
                    }
                }
                is Result.Error -> {
                    Logger.e { "Image compression failed: ${result.error}" }
                    updateSuccessState {
                        it.copy(compression = ImageCompressionState.Idle)
                    }
                    _events.emit(PostSubmissionEvent.ShowError(result.error.toUiText()))
                }
            }
        }
    }

    private fun submitPost() {
        val currentState = _uiState.value as? PostSubmissionUiState.Success ?: return
        val userId = (currentAuthState as? AuthState.Authenticated)?.userId ?: return

        viewModelScope.launch {
            when (
                val result = enqueuePostSubmissionUseCase(
                    authorId = userId,
                    title = currentState.title,
                    content = currentState.content,
                    imageUris = currentState.imageUris,
                    tags = currentState.tags,
                )
            ) {
                is Result.Success -> {
                    Logger.i { "Post submission scheduled successfully" }
                    _events.emit(PostSubmissionEvent.PostSubmitted)
                }
                is Result.Error -> {
                    Logger.e { "Failed to schedule post submission: ${result.error}" }
                    _events.emit(PostSubmissionEvent.ShowError(result.error.toUiText()))
                }
            }
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _events.emit(PostSubmissionEvent.NavigateBack)
        }
    }
}
