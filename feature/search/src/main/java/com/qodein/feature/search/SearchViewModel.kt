package com.qodein.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qodein.shared.model.Post
import com.qodein.shared.model.PostId
import com.qodein.shared.model.Tag
import com.qodein.shared.model.UserId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.minus
import javax.inject.Inject

/**
 * ViewModel for search screen following MVI pattern with Events system
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    // TODO: Inject actual repositories when available
) : ViewModel() {

    private val _state = MutableStateFlow(SearchUiState.initial())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<SearchEvent>()
    val events: SharedFlow<SearchEvent> = _events.asSharedFlow()

    private var searchJob: Job? = null

    init {
        loadInitialData()
    }

    fun handleAction(action: SearchAction) {
        when (action) {
            is SearchAction.SearchQueryChanged -> updateSearchQuery(action.query)
            is SearchAction.SearchSubmitted -> performSearch()
            is SearchAction.ClearSearch -> clearSearch()
            is SearchAction.TagSelected -> selectTag(action.tag)
            is SearchAction.TagRemoved -> removeTag(action.tag)
            is SearchAction.ClearAllTags -> clearAllTags()
            is SearchAction.PostLiked -> likePost(action.postId)
            is SearchAction.PostUnliked -> unlikePost(action.postId)
            is SearchAction.PostShared -> sharePost(action.postId)
            is SearchAction.PostCommentClicked -> openComments(action.postId)
            is SearchAction.LoadMorePosts -> loadMorePosts()
            is SearchAction.RefreshPosts -> refreshPosts()
            is SearchAction.RetryClicked -> retryLastAction()
            is SearchAction.ErrorDismissed -> dismissError()
        }
    }

    private fun updateSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)

        // Debounce search
        searchJob?.cancel()
        if (query.isNotBlank()) {
            searchJob = viewModelScope.launch {
                delay(300) // Debounce delay
                performSearch()
            }
        }
    }

    private fun performSearch() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            try {
                // TODO: Replace with actual repository call
                delay(800) // Simulate network delay
                val posts = generateMockPosts()

                _state.value = _state.value.copy(
                    posts = posts,
                    isLoading = false,
                    hasMorePosts = true,
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to search posts. Please try again.",
                )
                emitEvent(SearchEvent.ShowError("Search failed"))
            }
        }
    }

    private fun clearSearch() {
        _state.value = _state.value.copy(searchQuery = "")
        loadInitialData()
    }

    private fun selectTag(tag: Tag) {
        val currentTags = _state.value.selectedTags
        if (!currentTags.contains(tag)) {
            _state.value = _state.value.copy(selectedTags = currentTags + tag)
            performSearch()
        }
    }

    private fun removeTag(tag: Tag) {
        val currentTags = _state.value.selectedTags
        _state.value = _state.value.copy(selectedTags = currentTags - tag)
        performSearch()
    }

    private fun clearAllTags() {
        _state.value = _state.value.copy(selectedTags = emptyList())
        performSearch()
    }

    private fun likePost(postId: PostId) {
        viewModelScope.launch {
            try {
                // Optimistic update
                updatePostLike(postId, true)

                // TODO: Call actual repository
                delay(500)

                emitEvent(SearchEvent.ShowSuccess("Post liked!"))
            } catch (e: Exception) {
                // Revert optimistic update
                updatePostLike(postId, false)
                emitEvent(SearchEvent.ShowError("Failed to like post"))
            }
        }
    }

    private fun unlikePost(postId: PostId) {
        viewModelScope.launch {
            try {
                // Optimistic update
                updatePostLike(postId, false)

                // TODO: Call actual repository
                delay(500)
            } catch (e: Exception) {
                // Revert optimistic update
                updatePostLike(postId, true)
                emitEvent(SearchEvent.ShowError("Failed to unlike post"))
            }
        }
    }

    private fun sharePost(postId: PostId) {
        viewModelScope.launch {
            emitEvent(SearchEvent.ShowShareDialog(postId))
        }
    }

    private fun openComments(postId: PostId) {
        viewModelScope.launch {
            emitEvent(SearchEvent.NavigateToComments(postId))
        }
    }

    private fun loadMorePosts() {
        if (_state.value.isLoadingMore || !_state.value.hasMorePosts) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingMore = true)

            try {
                // TODO: Replace with actual repository call
                delay(1000)
                val newPosts = generateMockPosts(offset = _state.value.posts.size)

                _state.value = _state.value.copy(
                    posts = _state.value.posts + newPosts,
                    isLoadingMore = false,
                    hasMorePosts = newPosts.isNotEmpty(),
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoadingMore = false,
                    errorMessage = "Failed to load more posts",
                )
            }
        }
    }

    private fun refreshPosts() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRefreshing = true)

            try {
                // TODO: Replace with actual repository call
                delay(1000)
                val posts = generateMockPosts()

                _state.value = _state.value.copy(
                    posts = posts,
                    isRefreshing = false,
                    hasMorePosts = true,
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isRefreshing = false,
                    errorMessage = "Failed to refresh posts",
                )
            }
        }
    }

    private fun retryLastAction() {
        loadInitialData()
    }

    private fun dismissError() {
        _state.value = _state.value.copy(errorMessage = null)
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            try {
                // TODO: Replace with actual repository calls
                delay(800)
                val posts = generateMockPosts()
                val suggestedTags = generateMockTags()

                _state.value = _state.value.copy(
                    posts = posts,
                    suggestedTags = suggestedTags,
                    isLoading = false,
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load posts. Please try again.",
                )
            }
        }
    }

    private fun updatePostLike(
        postId: PostId,
        liked: Boolean
    ) {
        val updatedPosts = _state.value.posts.map { post ->
            if (post.id == postId) {
                post.copy(
                    isLikedByCurrentUser = liked,
                    likes = if (liked) post.likes + 1 else post.likes - 1,
                )
            } else {
                post
            }
        }
        _state.value = _state.value.copy(posts = updatedPosts)
    }

    private fun emitEvent(event: SearchEvent) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }

    // TODO: Replace with actual repository data
    private fun generateMockPosts(offset: Int = 0): List<Post> {
        val mockUsers = listOf(
            "tech_hunter",
            "deal_finder",
            "promo_master",
            "code_collector",
            "savings_guru",
            "discount_pro",
            "offer_seeker",
            "bargain_hunter",
        )

        val mockContent = listOf(
            "🔥 Found an amazing subscription deal! Anyone tried this service before?",
            "Looking for recommendations on music streaming services. What's everyone using?",
            "💡 Pro tip: Stack these codes for maximum savings! Who else does this?",
            "Can anyone help me find codes for fitness apps? Training season is here!",
            "📱 Best mobile plan deals right now? Need unlimited data suggestions.",
            "Sharing my go-to streaming bundle setup. What's your stack look like?",
            "🎮 Gaming subscription recommendations needed! What's worth it?",
            "Food delivery apps getting expensive. Anyone found good deals lately?",
        )

        val mockTags = generateMockTags()

        return (0..7).map { index ->
            val actualIndex = offset + index
            Post(
                id = com.qodein.shared.model.PostId("post_$actualIndex"),
                authorId = UserId("user_${actualIndex % mockUsers.size}"),
                authorUsername = mockUsers[actualIndex % mockUsers.size],
                authorAvatarUrl = "https://picsum.photos/seed/$actualIndex/150/150",
                content = mockContent[actualIndex % mockContent.size],
                tags = mockTags.shuffled().take((1..3).random()),
                likes = (5..150).random(),
                comments = (0..25).random(),
                shares = (0..10).random(),
                createdAt = Clock.System.now().minus((1..48).random(), kotlinx.datetime.DateTimeUnit.HOUR),
                isLikedByCurrentUser = (0..10).random() < 3,
            )
        }
    }

    private fun generateMockTags(): List<Tag> =
        listOf(
            Tag.create("streaming", "#FF6B6B"),
            Tag.create("music", "#4ECDC4"),
            Tag.create("fitness", "#45B7D1"),
            Tag.create("gaming", "#96CEB4"),
            Tag.create("food", "#FFEAA7"),
            Tag.create("mobile", "#DDA0DD"),
            Tag.create("education", "#98D8C8"),
            Tag.create("productivity", "#F7DC6F"),
            Tag.create("entertainment", "#BB8FCE"),
            Tag.create("shopping", "#85C1E9"),
            Tag.create("travel", "#F8C471"),
            Tag.create("health", "#82E0AA"),
        )
}
