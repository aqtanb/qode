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
import com.qodein.shared.model.User
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

    private val _uiState = MutableStateFlow(PostSubmissionUiState())
    val uiState: StateFlow<PostSubmissionUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<PostSubmissionEvent>()
    val events = _events.asSharedFlow()

    private var currentUser: User? = null

    companion object {
        private const val TAG = "PostSubmissionVM"
    }

    init {
        observeAuthState()
    }

    fun onAction(action: PostSubmissionAction) {
        when (action) {
            // Input actions
            is PostSubmissionAction.UpdateTitle -> updateTitle(action.title)
            is PostSubmissionAction.UpdateContent -> updateContent(action.content)

            // Tag actions
            is PostSubmissionAction.AddTag -> addTag(action.tag)
            is PostSubmissionAction.RemoveTag -> removeTag(action.tag)
            PostSubmissionAction.ShowTagSelector -> showTagSelector()
            PostSubmissionAction.HideTagSelector -> hideTagSelector()
            is PostSubmissionAction.SearchTags -> searchTags(action.query)

            // Image actions
            PostSubmissionAction.AddImage -> openImagePicker()
            is PostSubmissionAction.RemoveImage -> removeImage(action.index)
            is PostSubmissionAction.UpdateImageUris -> updateImageUris(action.uris)

            // Submission
            PostSubmissionAction.Submit -> submitPost()
            is PostSubmissionAction.SubmitWithUser -> submitPostWithUser(action.user)

            // Navigation
            PostSubmissionAction.NavigateBack -> navigateBack()

            // Auth
            PostSubmissionAction.SignInWithGoogle -> signInWithGoogle()
            PostSubmissionAction.DismissAuthSheet -> dismissAuthSheet()
            PostSubmissionAction.ClearAuthError -> clearAuthError()

            // Error handling
            PostSubmissionAction.ClearValidationErrors -> clearValidationErrors()
            PostSubmissionAction.RetryPostSubmission -> submitPost()
        }
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            getAuthStateUseCase().collect { authState ->
                currentUser = authState
            }
        }
    }

    private fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    private fun updateContent(content: String) {
        _uiState.update { it.copy(content = content) }
    }

    private fun addTag(tag: Tag) {
        _uiState.update { state ->
            if (state.tags.size < 10 && tag !in state.tags) {
                state.copy(tags = state.tags + tag)
            } else {
                state
            }
        }
    }

    private fun removeTag(tag: Tag) {
        _uiState.update { state ->
            state.copy(tags = state.tags - tag)
        }
    }

    private fun showTagSelector() {
        _uiState.update { it.copy(isTagSelectorVisible = true) }
        // TODO: Load popular tags
    }

    private fun hideTagSelector() {
        _uiState.update { it.copy(isTagSelectorVisible = false) }
    }

    private fun searchTags(query: String) {
        _uiState.update { it.copy(tagSearchQuery = query) }
        // TODO: Search tags
    }

    private fun openImagePicker() {
        viewModelScope.launch {
            _events.emit(PostSubmissionEvent.OpenImagePicker)
        }
    }

    private fun removeImage(index: Int) {
        _uiState.update { state ->
            state.copy(imageUris = state.imageUris.filterIndexed { i, _ -> i != index })
        }
    }

    private fun updateImageUris(uris: List<String>) {
        _uiState.update { state ->
            val newUris = (state.imageUris + uris).take(5) // Max 5 images
            state.copy(imageUris = newUris)
        }
    }

    private fun submitPost() {
    }

    private fun submitPostWithUser(user: User) {
        val state = _uiState.value

        // Validate
        val validationErrors = validateInputs(state)
        if (validationErrors.hasErrors) {
            _uiState.update { it.copy(validationErrors = validationErrors) }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, submissionError = null) }

        viewModelScope.launch {
        }
    }

    private fun validateInputs(state: PostSubmissionUiState): PostSubmissionUiState.ValidationErrors {
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
        } else if (state.tags.size > 10) {
            tagsError = "Too many tags (max 10)"
        }

        if (state.imageUris.size > 5) {
            imagesError = "Too many images (max 5)"
        }

        return PostSubmissionUiState.ValidationErrors(
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
        viewModelScope.launch {
            signInWithGoogleUseCase().collect { result ->
                when (result) {
                    is Result.Success -> {
                        Logger.i(TAG) { "Sign in successful" }
                        _uiState.update { it.copy(isAuthSheetVisible = false, authError = null) }
                    }
                    is Result.Error -> {
                        Logger.e(TAG) { "Sign in failed: ${result.error}" }
                        _uiState.update { it.copy(authError = result.error) }
                    }
                }
            }
        }
    }

    private fun dismissAuthSheet() {
        _uiState.update { it.copy(isAuthSheetVisible = false) }
    }

    private fun clearAuthError() {
        _uiState.update { it.copy(authError = null) }
    }

    private fun clearValidationErrors() {
        _uiState.update { it.copy(validationErrors = PostSubmissionUiState.ValidationErrors()) }
    }
}
