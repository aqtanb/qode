package com.qodein.feature.post.submission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.qodein.core.analytics.AnalyticsHelper
import com.qodein.shared.common.Result
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import com.qodein.shared.domain.usecase.auth.SignInWithGoogleUseCase
import com.qodein.shared.domain.usecase.post.CreatePostUseCase
import com.qodein.shared.model.Tag
import com.qodein.shared.model.Tag.Companion.MAX_TAGS_SELECTED
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostSubmissionViewModel @Inject constructor(
    private val createPostUseCase: CreatePostUseCase,
    private val getAuthStateUseCase: GetAuthStateUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow<PostSubmissionUiState>(PostSubmissionUiState.Success.initial())
    val uiState: StateFlow<PostSubmissionUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<PostSubmissionEvent>()
    val events = _events.asSharedFlow()

    companion object {
        private const val TAG = "PostSubmissionVM"
    }

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
            // Input actions
            is PostSubmissionAction.UpdateTitle -> updateTitle(action.title)
            is PostSubmissionAction.UpdateContent -> updateContent(action.content)

            // Tag actions
            is PostSubmissionAction.AddTag -> addTag(action.tag)
            is PostSubmissionAction.RemoveTag -> removeTag(action.tag)

            is PostSubmissionAction.RemoveImage -> removeImage(action.index)
            is PostSubmissionAction.UpdateImageUris -> updateImageUris(action.uris)

            // Submission
            PostSubmissionAction.Submit -> submitPost()

            // Navigation
            PostSubmissionAction.NavigateBack -> navigateBack()

            // Auth
            PostSubmissionAction.SignInWithGoogle -> signInWithGoogle()
            PostSubmissionAction.DismissAuthSheet -> navigateBack()

            // Error handling
            PostSubmissionAction.ClearValidationErrors -> clearValidationErrors()
            PostSubmissionAction.RetryPostSubmission -> submitPost()
        }
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            getAuthStateUseCase().collect { user ->
                updateSuccessState { state ->
                    val authState = when (user) {
                        null -> PostAuthenticationState.Unauthenticated
                        else -> PostAuthenticationState.Authenticated(user)
                    }
                    state.copy(authentication = authState)
                }
            }
        }
    }

    private fun updateTitle(title: String) {
        updateSuccessState { it.copy(title = title) }
    }

    private fun updateContent(content: String) {
        updateSuccessState { it.copy(content = content) }
    }

    private fun addTag(tag: Tag) {
        updateSuccessState { state ->
            if (state.tags.size < MAX_TAGS_SELECTED && tag !in state.tags) {
                state.copy(tags = state.tags + tag)
            } else {
                state
            }
        }
    }

    private fun removeTag(tag: Tag) {
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
        updateSuccessState { state ->
            val newUris = (state.imageUris + uris).take(5) // Max 5 images
            state.copy(imageUris = newUris)
        }
    }

    private fun submitPost() {
        val currentState = _uiState.value
        if (currentState !is PostSubmissionUiState.Success) return

        // Validate
        val validationErrors = validateInputs(currentState)
        if (validationErrors.hasErrors) {
            updateSuccessState { it.copy(validationErrors = validationErrors) }
            return
        }

        updateSuccessState { it.copy(submission = PostSubmissionState.Submitting) }

        viewModelScope.launch {
            // TODO: Implement post submission
            // createPostUseCase(...).collect { result ->
            //     when (result) {
            //         is Result.Success -> {
            //             _events.emit(PostSubmissionEvent.PostSubmitted)
            //         }
            //         is Result.Error -> {
            //             updateSuccessState { it.copy(submission = PostSubmissionState.Idle) }
            //             _events.emit(PostSubmissionEvent.ShowError(result.error))
            //         }
            //     }
            // }
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

        if (state.tags.isEmpty()) {
            tagsError = "At least one tag is required"
        } else if (state.tags.size > MAX_TAGS_SELECTED) {
            tagsError = "Too many tags (max 10)"
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

    private fun signInWithGoogle() {
        updateSuccessState { it.copy(authentication = PostAuthenticationState.Loading) }

        viewModelScope.launch {
            signInWithGoogleUseCase().collect { result ->
                when (result) {
                    is Result.Success -> {
                        Logger.i(TAG) { "Sign in successful" }
                        // Auth state will be updated via observeAuthState()
                    }
                    is Result.Error -> {
                        Logger.w(TAG) { "Sign in failed: ${result.error}" }
                        // Reset to unauthenticated
                        updateSuccessState { it.copy(authentication = PostAuthenticationState.Unauthenticated) }
                        // Show error snackbar
                        _events.emit(PostSubmissionEvent.ShowError(result.error))
                    }
                }
            }
        }
    }

    private fun clearValidationErrors() {
        updateSuccessState { it.copy(validationErrors = ValidationErrors()) }
    }
}
