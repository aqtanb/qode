package com.qodein.feature.post.submission

import android.content.Context
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.qodein.core.ui.error.toUiText
import com.qodein.core.ui.util.ImageCompressor
import com.qodein.shared.common.Result
import com.qodein.shared.domain.AuthState
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import com.qodein.shared.model.Post
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PostSubmissionViewModel(private val context: Context, private val getAuthStateUseCase: GetAuthStateUseCase) : ViewModel() {

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

            // Submission
            PostSubmissionAction.Submit -> submitPost()

            // Navigation
            PostSubmissionAction.NavigateBack -> navigateBack()

            // Error handling
            PostSubmissionAction.ClearValidationErrors -> clearValidationErrors()
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
                context = context,
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
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState !is PostSubmissionUiState.Success) return@launch

            val userId = (currentAuthState as? AuthState.Authenticated)?.userId
            if (userId == null) {
                Logger.w { "Cannot submit: user is not authenticated" }
                return@launch
            }

            _uiState.update { PostSubmissionUiState.Loading }

            /*val workRequest = UploadPostWorker.createWorkRequest(
                title = currentState.title,
                content = currentState.content,
                tags = currentState.tags.map { it.value },
                imageUris = currentState.imageUris,
                authorId = userId.value,
                authorUsername = userId.displayName.orEmpty(),
                authorAvatarUrl = userId.profile.photoUrl,
            )
            WorkManager.getInstance(context).enqueue(workRequest)
            _events.emit(PostSubmissionEvent.PostSubmitted)*/
        }
    }

    private fun validateInputs(state: PostSubmissionUiState.Success): ValidationErrors {
        var titleError: String? = null
        var contentError: String? = null
        var tagsError: String? = null
        var imagesError: String? = null

        if (state.title.isBlank()) {
            titleError = "Title is required"
        } else if (state.title.length > 200) {
            titleError = "Title is too long (max 200 characters)"
        }

        if (state.content.isBlank()) {
            contentError = "Content is required"
        } else if (state.content.length > 2000) {
            contentError = "Content is too long (max 2000 characters)"
        }

        if (state.imageUris.size > 5) {
            imagesError = "Too many images (max 5)"
        }

        return ValidationErrors(
            titleError = titleError,
            contentError = contentError,
            tagsError = tagsError,
            imagesError = imagesError,
        )
    }

    private fun navigateBack() {
        viewModelScope.launch {
            _events.emit(PostSubmissionEvent.NavigateBack)
        }
    }

    private fun clearValidationErrors() {
        updateSuccessState { it.copy(validationErrors = ValidationErrors()) }
    }
}
