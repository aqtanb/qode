package com.qodein.feature.post.submission

import android.content.Context
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import co.touchlab.kermit.Logger
import com.qodein.core.analytics.AnalyticsHelper
import com.qodein.core.data.worker.UploadPostWorker
import com.qodein.core.ui.auth.IdTokenProvider
import com.qodein.core.ui.state.UiAuthState
import com.qodein.core.ui.util.ImageCompressor
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.PostError
import com.qodein.shared.domain.AuthState
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import com.qodein.shared.domain.usecase.auth.SignInWithGoogleUseCase
import com.qodein.shared.model.Tag
import com.qodein.shared.model.Tag.Companion.MAX_TAGS_SELECTED
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostSubmissionViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val getAuthStateUseCase: GetAuthStateUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val idTokenProvider: IdTokenProvider,
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
            is PostSubmissionAction.SignInWithGoogle -> signInWithGoogle(action.context)
            PostSubmissionAction.DismissAuthSheet -> navigateBack()

            // Error handling
            PostSubmissionAction.ClearValidationErrors -> clearValidationErrors()
            PostSubmissionAction.RetryPostSubmission -> submitPost()
        }
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            getAuthStateUseCase().collect { authState ->
                updateSuccessState { state ->
                    val mapped = when (authState) {
                        is AuthState.Authenticated -> UiAuthState.Authenticated(authState.user)
                        AuthState.Unauthenticated -> UiAuthState.Unauthenticated
                    }
                    state.copy(authentication = mapped)
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
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState !is PostSubmissionUiState.Success) return@launch

            val availableSlots = 5 - currentState.imageUris.size
            val urisToCompress = uris.take(availableSlots).map { it.toUri() }

            if (urisToCompress.isEmpty()) return@launch

            Logger.d(TAG) { "Starting compression for ${urisToCompress.size} images" }

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
                    Logger.i(TAG) { "Successfully compressed ${result.data.size} images" }
                    val compressedUriStrings = result.data.map { it.toString() }
                    updateSuccessState { state ->
                        state.copy(
                            imageUris = state.imageUris + compressedUriStrings,
                            compression = ImageCompressionState.Idle,
                        )
                    }
                }
                is Result.Error -> {
                    Logger.e(TAG) { "Image compression failed: ${result.error}" }
                    updateSuccessState {
                        it.copy(compression = ImageCompressionState.Idle)
                    }
                    _events.emit(PostSubmissionEvent.ShowError(result.error))
                }
            }
        }
    }

    private fun submitPost() {
        viewModelScope.launch {
            val currentState = uiState.value
            if (currentState !is PostSubmissionUiState.Success) return@launch
            val user = when (val authState = getAuthStateUseCase().firstOrNull()) {
                is AuthState.Authenticated -> authState.user
                else -> {
                    _uiState.update { PostSubmissionUiState.Error(PostError.SubmissionFailure.NotAuthorized) }
                    return@launch
                }
            }
            _uiState.update { PostSubmissionUiState.Loading }

            val workRequest = UploadPostWorker.createWorkRequest(
                title = currentState.title,
                content = currentState.content,
                tags = currentState.tags.map { it.value },
                imageUris = currentState.imageUris,
                authorId = user.id.value,
                authorUsername = user.displayName.orEmpty(),
                authorAvatarUrl = user.profile.photoUrl,
            )
            WorkManager.getInstance(context).enqueue(workRequest)
            _events.emit(PostSubmissionEvent.PostSubmitted)
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

    private fun signInWithGoogle(activityContext: Context) {
        updateSuccessState { it.copy(authentication = UiAuthState.Loading) }

        viewModelScope.launch {
            when (val tokenResult = idTokenProvider.getIdToken(activityContext)) {
                is Result.Success -> {
                    when (val signInResult = signInWithGoogleUseCase(tokenResult.data)) {
                        is Result.Success -> {
                            Logger.i(TAG) { "Sign in successful" }
                            // Auth state will update via observeAuthState()
                        }
                        is Result.Error -> {
                            Logger.w(TAG) { "Sign in failed: ${signInResult.error}" }
                            updateSuccessState { it.copy(authentication = UiAuthState.Unauthenticated) }
                            _events.emit(PostSubmissionEvent.ShowError(signInResult.error))
                        }
                    }
                }
                is Result.Error -> {
                    Logger.w(TAG) { "Failed to get ID token: ${tokenResult.error}" }
                    updateSuccessState { it.copy(authentication = UiAuthState.Unauthenticated) }
                    _events.emit(PostSubmissionEvent.ShowError(tokenResult.error))
                }
            }
        }
    }

    private fun clearValidationErrors() {
        updateSuccessState { it.copy(validationErrors = ValidationErrors()) }
    }
}
