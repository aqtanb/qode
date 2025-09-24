package com.qodein.feature.feed.preview

import com.qodein.shared.common.result.ErrorType
import com.qodein.shared.model.Post
import com.qodein.shared.model.PostId
import com.qodein.shared.model.Tag
import com.qodein.shared.model.UserId
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import kotlin.time.Clock

/**
 * Mock data factory for Feed screen previews
 * Provides consistent test data across all preview variants
 */
object MockFeedData {

    // MARK: - Mock Tags

    val suggestedTags = listOf(
        Tag.create("streaming", "#FF6B6B"),
        Tag.create("music", "#4ECDC4"),
        Tag.create("fitness", "#45B7D1"),
        Tag.create("gaming", "#96CEB4"),
        Tag.create("food", "#FFEAA7"),
        Tag.create("mobile", "#DDA0DD"),
        Tag.create("education", "#98D8C8"),
        Tag.create("productivity", "#F7DC6F"),
    )

    val selectedTags = listOf(
        Tag.create("streaming", "#FF6B6B"),
        Tag.create("gaming", "#96CEB4"),
    )

    // MARK: - Mock Posts

    private val mockUsers = listOf(
        "tech_hunter",
        "deal_finder",
        "promo_master",
        "code_collector",
        "savings_guru",
        "discount_pro",
    )

    private val mockContent = listOf(
        "🔥 Found an amazing subscription deal! Anyone tried this service before? The savings are incredible and I'm curious about the user experience.",
        "Looking for recommendations on music streaming services. What's everyone using these days? Need something with good playlist features.",
        "💡 Pro tip: Stack these codes for maximum savings! Who else does this strategy? It's been working great for me lately.",
        "Can anyone help me find codes for fitness apps? Training season is here and I need motivation tools that won't break the bank.",
        "📱 Best mobile plan deals right now? Need unlimited data suggestions for heavy streaming usage.",
        "Sharing my go-to streaming bundle setup. What's your stack look like? Always looking for optimization tips.",
    )

    val samplePosts = mockContent.mapIndexed { index, content ->
        Post(
            id = PostId("preview_post_$index"),
            authorId = UserId("user_$index"),
            authorUsername = mockUsers[index % mockUsers.size],
            authorAvatarUrl = "https://picsum.photos/seed/$index/150/150",
            content = content,
            tags = suggestedTags.shuffled().take((1..3).random()),
            upvotes = when (index) {
                0 -> 127 // Hot post
                1 -> 42
                2 -> 18
                3 -> 85
                4 -> 7
                else -> (5..50).random()
            },
            downvotes = (0..5).random(),
            shares = when (index) {
                0 -> 12
                1 -> 3
                2 -> 0
                3 -> 7
                else -> (0..3).random()
            },
            createdAt = Clock.System.now().minus((index + 1) * 2, DateTimeUnit.HOUR),
            isUpvotedByCurrentUser = index % 3 == 0, // Every third post is liked
        )
    }

    // MARK: - UI State Factories

    fun createLoadingState(
        searchQuery: String = "",
        selectedTags: List<Tag> = emptyList(),
        suggestedTags: List<Tag> = MockFeedData.suggestedTags
    ) = com.qodein.feature.feed.FeedUiState.Loading(
        searchQuery = searchQuery,
        selectedTags = selectedTags,
        suggestedTags = suggestedTags,
        isSearchFocused = false,
    )

    fun createContentState(
        searchQuery: String = "",
        selectedTags: List<Tag> = emptyList(),
        suggestedTags: List<Tag> = MockFeedData.suggestedTags,
        posts: List<Post> = samplePosts,
        hasMorePosts: Boolean = true,
        isLoadingMore: Boolean = false,
        isRefreshing: Boolean = false
    ) = com.qodein.feature.feed.FeedUiState.Content(
        searchQuery = searchQuery,
        selectedTags = selectedTags,
        suggestedTags = suggestedTags,
        isSearchFocused = false,
        posts = posts,
        hasMorePosts = hasMorePosts,
        isLoadingMore = isLoadingMore,
        isRefreshing = isRefreshing,
    )

    fun createEmptyState(
        searchQuery: String = "rare streaming service",
        selectedTags: List<Tag> = listOf(Tag.create("streaming", "#FF6B6B")),
        suggestedTags: List<Tag> = MockFeedData.suggestedTags
    ) = com.qodein.feature.feed.FeedUiState.Content(
        searchQuery = searchQuery,
        selectedTags = selectedTags,
        suggestedTags = suggestedTags,
        isSearchFocused = false,
        posts = emptyList(),
        hasMorePosts = false,
        isLoadingMore = false,
        isRefreshing = false,
    )

    fun createErrorState(
        searchQuery: String = "streaming deals",
        selectedTags: List<Tag> = emptyList(),
        suggestedTags: List<Tag> = MockFeedData.suggestedTags,
        errorType: ErrorType = ErrorType.NETWORK_GENERAL,
        isRetryable: Boolean = true
    ) = com.qodein.feature.feed.FeedUiState.Error(
        searchQuery = searchQuery,
        selectedTags = selectedTags,
        suggestedTags = suggestedTags,
        isSearchFocused = false,
        errorType = errorType,
        isRetryable = isRetryable,
        shouldShowSnackbar = false,
        errorCode = null,
    )

    // MARK: - Specialized States

    fun createContentWithFiltersState() =
        createContentState(
            searchQuery = "streaming",
            selectedTags = selectedTags,
            posts = samplePosts.take(3), // Fewer posts to simulate filtered results
        )

    fun createLoadingMoreState() =
        createContentState(
            posts = samplePosts,
            isLoadingMore = true,
            hasMorePosts = true,
        )

    fun createNoFiltersEmptyState() =
        createContentState(
            searchQuery = "",
            selectedTags = emptyList(),
            posts = emptyList(),
            hasMorePosts = false,
        )
}
