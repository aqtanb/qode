package com.qodein.feature.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qodein.core.analytics.AnalyticsEvent
import com.qodein.core.analytics.AnalyticsHelper
import com.qodein.core.analytics.logFilterContent
import com.qodein.core.analytics.logSearch
import com.qodein.shared.common.result.getErrorCode
import com.qodein.shared.common.result.isRetryable
import com.qodein.shared.common.result.shouldShowSnackbar
import com.qodein.shared.common.result.toErrorType
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
class FeedViewModel @Inject constructor(
    private val analyticsHelper: AnalyticsHelper
    // TODO: Inject actual repositories when available
) : ViewModel() {

    private val _state = MutableStateFlow<FeedUiState>(FeedUiState.initial())
    val state: StateFlow<FeedUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<FeedEvent>()
    val events: SharedFlow<FeedEvent> = _events.asSharedFlow()

    private var searchJob: Job? = null

    init {
        loadInitialData()
    }

    fun handleAction(action: FeedAction) {
        when (action) {
            is FeedAction.FeedQueryChanged -> updateSearchQuery(action.query)
            is FeedAction.FeedSubmitted -> performSearch()
            is FeedAction.ClearFeed -> clearSearch()
            is FeedAction.TagSelected -> selectTag(action.tag)
            is FeedAction.TagRemoved -> removeTag(action.tag)
            is FeedAction.ClearAllTags -> clearAllTags()
            is FeedAction.PostLiked -> likePost(action.postId)
            is FeedAction.PostUnliked -> unlikePost(action.postId)
            is FeedAction.PostShared -> sharePost(action.postId)
            is FeedAction.PostCommentClicked -> openComments(action.postId)
            is FeedAction.LoadMorePosts -> loadMorePosts()
            is FeedAction.RefreshPosts -> refreshPosts()
            is FeedAction.RetryClicked -> retryLastAction()
            is FeedAction.ErrorDismissed -> dismissError()
        }
    }

    private fun updateSearchQuery(query: String) {
        _state.value = when (val currentState = _state.value) {
            is FeedUiState.Loading -> currentState.copy(searchQuery = query)
            is FeedUiState.Content -> currentState.copy(searchQuery = query)
            is FeedUiState.Error -> currentState.copy(searchQuery = query)
        }

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
            val currentState = _state.value
            _state.value = FeedUiState.Loading(
                searchQuery = currentState.searchQuery,
                selectedTags = currentState.selectedTags,
                suggestedTags = currentState.suggestedTags,
                isSearchFocused = currentState.isSearchFocused,
            )

            try {
                // Track search analytics
                val query = currentState.searchQuery
                if (query.isNotBlank()) {
                    analyticsHelper.logSearch(query)
                }

                // TODO: Replace with actual repository call
                delay(800) // Simulate network delay
                val posts = generateMockPosts()

                _state.value = FeedUiState.Content(
                    searchQuery = currentState.searchQuery,
                    selectedTags = currentState.selectedTags,
                    suggestedTags = currentState.suggestedTags,
                    isSearchFocused = currentState.isSearchFocused,
                    posts = posts,
                    hasMorePosts = true,
                )
            } catch (e: Exception) {
                _state.value = FeedUiState.Error(
                    searchQuery = currentState.searchQuery,
                    selectedTags = currentState.selectedTags,
                    suggestedTags = currentState.suggestedTags,
                    isSearchFocused = currentState.isSearchFocused,
                    errorType = e.toErrorType(),
                    isRetryable = e.isRetryable(),
                    shouldShowSnackbar = e.shouldShowSnackbar(),
                    errorCode = e.getErrorCode(),
                )
                emitEvent(FeedEvent.ShowError("Search failed"))
            }
        }
    }

    private fun clearSearch() {
        val currentState = _state.value
        _state.value = when (currentState) {
            is FeedUiState.Loading -> currentState.copy(searchQuery = "")
            is FeedUiState.Content -> currentState.copy(searchQuery = "")
            is FeedUiState.Error -> currentState.copy(searchQuery = "")
        }
        loadInitialData()
    }

    private fun selectTag(tag: Tag) {
        val currentState = _state.value
        val currentTags = currentState.selectedTags
        if (!currentTags.contains(tag)) {
            // Track tag selection analytics
            analyticsHelper.logFilterContent(
                filterType = "tag",
                filterValue = tag.name,
            )

            _state.value = when (currentState) {
                is FeedUiState.Loading -> currentState.copy(selectedTags = currentTags + tag)
                is FeedUiState.Content -> currentState.copy(selectedTags = currentTags + tag)
                is FeedUiState.Error -> currentState.copy(selectedTags = currentTags + tag)
            }
            performSearch()
        }
    }

    private fun removeTag(tag: Tag) {
        val currentState = _state.value
        val currentTags = currentState.selectedTags

        // Track tag removal analytics
        analyticsHelper.logFilterContent(
            filterType = "tag_removed",
            filterValue = tag.name,
        )

        _state.value = when (currentState) {
            is FeedUiState.Loading -> currentState.copy(selectedTags = currentTags - tag)
            is FeedUiState.Content -> currentState.copy(selectedTags = currentTags - tag)
            is FeedUiState.Error -> currentState.copy(selectedTags = currentTags - tag)
        }
        performSearch()
    }

    private fun clearAllTags() {
        val currentState = _state.value
        _state.value = when (currentState) {
            is FeedUiState.Loading -> currentState.copy(selectedTags = emptyList())
            is FeedUiState.Content -> currentState.copy(selectedTags = emptyList())
            is FeedUiState.Error -> currentState.copy(selectedTags = emptyList())
        }
        performSearch()
    }

    private fun likePost(postId: PostId) {
        viewModelScope.launch {
            try {
                // Optimistic update
                updatePostLike(postId, true)

                // Track analytics
                analyticsHelper.logEvent(
                    AnalyticsEvent(
                        type = "post_liked",
                        extras = listOf(
                            AnalyticsEvent.Param("post_id", postId.value),
                            AnalyticsEvent.Param("source", "search"),
                        ),
                    ),
                )

                // TODO: Call actual repository
                delay(500)

                emitEvent(FeedEvent.ShowSuccess("Post liked!"))
            } catch (e: Exception) {
                // Revert optimistic update
                updatePostLike(postId, false)
                emitEvent(FeedEvent.ShowError("Failed to like post"))
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
                emitEvent(FeedEvent.ShowError("Failed to unlike post"))
            }
        }
    }

    private fun sharePost(postId: PostId) {
        viewModelScope.launch {
            // Track share analytics
            analyticsHelper.logEvent(
                AnalyticsEvent(
                    type = "post_shared",
                    extras = listOf(
                        AnalyticsEvent.Param("post_id", postId.value),
                        AnalyticsEvent.Param("source", "search"),
                    ),
                ),
            )

            emitEvent(FeedEvent.ShowShareDialog(postId))
        }
    }

    private fun openComments(postId: PostId) {
        viewModelScope.launch {
            // Track comment click analytics
            analyticsHelper.logEvent(
                AnalyticsEvent(
                    type = "post_comment_clicked",
                    extras = listOf(
                        AnalyticsEvent.Param("post_id", postId.value),
                        AnalyticsEvent.Param("source", "search"),
                    ),
                ),
            )

            emitEvent(FeedEvent.NavigateToComments(postId))
        }
    }

    private fun loadMorePosts() {
        val currentState = _state.value
        if (currentState !is FeedUiState.Content || currentState.isLoadingMore || !currentState.hasMorePosts) return

        viewModelScope.launch {
            _state.value = currentState.copy(isLoadingMore = true)

            try {
                // TODO: Replace with actual repository call
                delay(1000)
                val newPosts = generateMockPosts(offset = currentState.posts.size)

                _state.value = currentState.copy(
                    posts = currentState.posts + newPosts,
                    isLoadingMore = false,
                    hasMorePosts = newPosts.isNotEmpty(),
                )
            } catch (e: Exception) {
                _state.value = FeedUiState.Error(
                    searchQuery = currentState.searchQuery,
                    selectedTags = currentState.selectedTags,
                    suggestedTags = currentState.suggestedTags,
                    isSearchFocused = currentState.isSearchFocused,
                    errorType = e.toErrorType(),
                    isRetryable = e.isRetryable(),
                    shouldShowSnackbar = e.shouldShowSnackbar(),
                    errorCode = e.getErrorCode(),
                )
            }
        }
    }

    private fun refreshPosts() {
        val currentState = _state.value
        if (currentState !is FeedUiState.Content) return

        viewModelScope.launch {
            _state.value = currentState.copy(isRefreshing = true)

            try {
                // TODO: Replace with actual repository call
                delay(1000)
                val posts = generateMockPosts()

                _state.value = currentState.copy(
                    posts = posts,
                    isRefreshing = false,
                    hasMorePosts = true,
                )
            } catch (e: Exception) {
                _state.value = FeedUiState.Error(
                    searchQuery = currentState.searchQuery,
                    selectedTags = currentState.selectedTags,
                    suggestedTags = currentState.suggestedTags,
                    isSearchFocused = currentState.isSearchFocused,
                    errorType = e.toErrorType(),
                    isRetryable = e.isRetryable(),
                    shouldShowSnackbar = e.shouldShowSnackbar(),
                    errorCode = e.getErrorCode(),
                )
            }
        }
    }

    private fun retryLastAction() {
        loadInitialData()
    }

    private fun dismissError() {
        val currentState = _state.value
        if (currentState is FeedUiState.Error) {
            _state.value = FeedUiState.Content(
                searchQuery = currentState.searchQuery,
                selectedTags = currentState.selectedTags,
                suggestedTags = currentState.suggestedTags,
                isSearchFocused = currentState.isSearchFocused,
            )
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val currentState = _state.value
            _state.value = FeedUiState.Loading(
                searchQuery = currentState.searchQuery,
                selectedTags = currentState.selectedTags,
                suggestedTags = currentState.suggestedTags,
                isSearchFocused = currentState.isSearchFocused,
            )

            try {
                // TODO: Replace with actual repository calls
                delay(800)
                val posts = generateMockPosts()
                val suggestedTags = generateMockTags()

                _state.value = FeedUiState.Content(
                    searchQuery = currentState.searchQuery,
                    selectedTags = currentState.selectedTags,
                    suggestedTags = suggestedTags,
                    isSearchFocused = currentState.isSearchFocused,
                    posts = posts,
                    hasMorePosts = true,
                )
            } catch (e: Exception) {
                _state.value = FeedUiState.Error(
                    searchQuery = currentState.searchQuery,
                    selectedTags = currentState.selectedTags,
                    suggestedTags = currentState.suggestedTags,
                    isSearchFocused = currentState.isSearchFocused,
                    errorType = e.toErrorType(),
                    isRetryable = e.isRetryable(),
                    shouldShowSnackbar = e.shouldShowSnackbar(),
                    errorCode = e.getErrorCode(),
                )
            }
        }
    }

    private fun updatePostLike(
        postId: PostId,
        liked: Boolean
    ) {
        val currentState = _state.value
        if (currentState is FeedUiState.Content) {
            val updatedPosts = currentState.posts.map { post ->
                if (post.id == postId) {
                    post.copy(
                        isLikedByCurrentUser = liked,
                        likes = if (liked) post.likes + 1 else post.likes - 1,
                    )
                } else {
                    post
                }
            }
            _state.value = currentState.copy(posts = updatedPosts)
        }
    }

    private fun emitEvent(event: FeedEvent) {
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
