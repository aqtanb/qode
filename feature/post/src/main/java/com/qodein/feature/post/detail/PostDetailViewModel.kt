package com.qodein.feature.post.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.qodein.feature.post.navigation.PostDetailRoute
import com.qodein.shared.common.Result
import com.qodein.shared.domain.usecase.post.GetPostByIdUseCase
import com.qodein.shared.model.PostId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostDetailViewModel @Inject constructor(savedStateHandle: SavedStateHandle, private val getPostByIdUseCase: GetPostByIdUseCase) :
    ViewModel() {
    private val _uiState: MutableStateFlow<PostDetailUiState> = MutableStateFlow(PostDetailUiState(postState = DataState.Loading))
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    private val args: PostDetailRoute = savedStateHandle.toRoute()
    private val postId: PostId = PostId(args.postId)

    init {
        viewModelScope.launch {
            loadPost()
        }
    }

    private suspend fun loadPost() {
        when (val result = getPostByIdUseCase(postId)) {
            is Result.Error -> _uiState.update { it.copy(postState = DataState.Error(result.error)) }
            is Result.Success -> _uiState.update { it.copy(postState = DataState.Success(result.data)) }
        }
    }
}
