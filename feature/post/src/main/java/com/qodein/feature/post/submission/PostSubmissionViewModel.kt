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

    private val _uiState = MutableStateFlow(PostSubmissionUiState())
    val uiState: StateFlow<PostSubmissionUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<PostSubmissionEvent>()
    val events = _events.asSharedFlow()

    private var currentAuthState: AuthState = AuthState.Unauthenticated

    init {
        observeAuthState()
    }

    private inline fun updateState(update: (PostSubmissionUiState) -> PostSubmissionUiState) {
        _uiState.update(update)
    }

    fun onAction(action: PostSubmissionAction) {
        when (action) {
            is PostSubmissionAction.UpdateTitle -> {
                updateState { it.copy(title = Post.filterTitle(action.title)) }
            }
            is PostSubmissionAction.UpdateContent -> {
                updateState { it.copy(content = Post.filterContent(action.content)) }
            }

            is PostSubmissionAction.UpdateTag -> {
                updateTag(action.tagInput)
            }
            is PostSubmissionAction.AddTag -> addTag(action.tag)
            is PostSubmissionAction.RemoveTag -> removeTag(action.tag)

            is PostSubmissionAction.RemoveImage -> removeImage(action.index)
            is PostSubmissionAction.UpdateImageUris -> updateImageUris(action.uris)
            PostSubmissionAction.PickImages -> pickImages()

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
            updateState { it.copy(tagInput = filtered) }
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
        updateState { state ->
            state.copy(tags = filterTags(state.tags + tag), tagInput = "")
        }
    }

    private fun removeTag(tag: String) {
        updateState { state ->
            state.copy(tags = state.tags - tag)
        }
    }

    private fun removeImage(index: Int) {
        updateState { state ->
            state.copy(imageUris = state.imageUris.filterIndexed { i, _ -> i != index })
        }
    }

    private fun updateImageUris(uris: List<String>) {
        viewModelScope.launch {
            val currentState = uiState.value
            val availableSlots = Post.MAX_IMAGES - currentState.imageUris.size

            if (availableSlots == 0) {
                _events.emit(PostSubmissionEvent.ImageLimitReached)
                return@launch
            }
            val urisToCompress = uris.take(availableSlots).map { it.toUri() }
            if (uris.size > availableSlots) {
                _events.emit(PostSubmissionEvent.ImagesPartiallyAdded(urisToCompress.size))
            }

            if (urisToCompress.isEmpty()) return@launch

            updateState { it.copy(compressingImages = true) }

            val result = ImageCompressor.compressImages(
                context = getApplication(),
                uris = urisToCompress,
            )

            when (result) {
                is Result.Success -> {
                    val compressedUriStrings = result.data.map { it.toString() }
                    updateState { state ->
                        state.copy(
                            imageUris = state.imageUris + compressedUriStrings,
                            compressingImages = false,
                        )
                    }
                }
                is Result.Error -> {
                    updateState { it.copy(compressingImages = false) }
                    _events.emit(PostSubmissionEvent.ShowError(result.error.toUiText()))
                }
            }
        }
    }

    private fun submitPost() {
        val currentState = _uiState.value
        val userId = (currentAuthState as? AuthState.Authenticated)?.userId ?: return

        updateState { it.copy(isSubmitting = true) }

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
                    updateState { it.copy(isSubmitting = false) }
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

    private fun pickImages() {
        viewModelScope.launch {
            if (_uiState.value.imageUris.size >= Post.MAX_IMAGES) return@launch
            _events.emit(PostSubmissionEvent.PickImagesRequested)
        }
    }
}
