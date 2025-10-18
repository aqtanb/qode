package com.qodein.core.ui.preview

import com.qodein.shared.model.Post
import com.qodein.shared.model.PostId
import com.qodein.shared.model.Tag
import com.qodein.shared.model.UserId
import kotlin.time.Instant

/**
 * Centralized preview data for Post composables
 * Provides consistent test data across all preview functions
 *
 * Note: This is separate from core:testing/TestPosts to avoid test dependencies in production code
 */
object PostPreviewData {

    private val sampleTimestamp = Instant.parse("2024-01-15T12:00:00Z")

    /**
     * Post with multiple images - use for carousel/pager previews
     */
    val postWithImages = Post(
        id = PostId("a1b2c3d4e5f6789012345678abcdef01"),
        authorId = UserId("user123"),
        authorName = "John Doe",
        authorAvatarUrl = null,
        title = "Amazing new features in Android 14 that will change everything",
        content = "Android 14 brings incredible new features that will revolutionize " +
            "how we develop mobile apps. From enhanced privacy controls to better performance " +
            "optimizations, this release is packed with improvements that every developer " +
            "should know about.",
        imageUrls = listOf(
            "https://picsum.photos/400/300?random=1",
            "https://picsum.photos/400/300?random=2",
            "https://picsum.photos/400/300?random=3",
        ),
        tags = listOf(
            Tag("android", 100),
            Tag("mobile", 50),
            Tag("development", 75),
        ),
        upvotes = 142,
        downvotes = 12,
        commentCount = 28,
        createdAt = sampleTimestamp,
        updatedAt = sampleTimestamp,
    )

    /**
     * Post without images - use for text-only layout previews
     */
    val postWithoutImages = Post(
        id = PostId("b2c3d4e5f6789012345678abcdef0123"),
        authorId = UserId("user456"),
        authorName = "Sarah Smith",
        authorAvatarUrl = "https://picsum.photos/100/100?random=10",
        title = "Best coding practices for clean architecture",
        content = "Clean architecture is essential for maintainable code. Here are the " +
            "top practices that every developer should follow to write better, more organized " +
            "code that scales.",
        imageUrls = emptyList(),
        tags = listOf(
            Tag("coding", 60),
            Tag("architecture", 40),
            Tag("best_practices", 25),
        ),
        upvotes = 89,
        downvotes = 3,
        commentCount = 15,
        createdAt = sampleTimestamp,
        updatedAt = sampleTimestamp,
    )

    /**
     * Post with single image - use for single image layout previews
     */
    val postWithSingleImage = Post(
        id = PostId("c3d4e5f6789012345678abcdef012345"),
        authorId = UserId("user789"),
        authorName = "Alex Chen",
        authorAvatarUrl = "https://picsum.photos/100/100?random=20",
        title = "Jetpack Compose state management deep dive",
        content = "Understanding state in Compose is crucial for building reactive UIs. " +
            "This guide covers everything from remember to derivedStateOf and beyond.",
        imageUrls = listOf("https://picsum.photos/400/300?random=4"),
        tags = listOf(
            Tag("compose", 80),
            Tag("android", 100),
            Tag("state_management", 45),
        ),
        upvotes = 256,
        downvotes = 8,
        commentCount = 42,
        createdAt = sampleTimestamp,
        updatedAt = sampleTimestamp,
    )

    /**
     * High engagement post - use for vote/comment count previews
     */
    val popularPost = Post(
        id = PostId("d4e5f6789012345678abcdef01234567"),
        authorId = UserId("user999"),
        authorName = "Maria Garcia",
        authorAvatarUrl = null,
        title = "Kotlin coroutines: From basics to advanced patterns",
        content = "Master coroutines with this comprehensive guide covering flows, " +
            "channels, and structured concurrency patterns that will make your async code shine.",
        imageUrls = listOf(
            "https://picsum.photos/400/300?random=5",
            "https://picsum.photos/400/300?random=6",
        ),
        tags = listOf(
            Tag("kotlin", 120),
            Tag("coroutines", 90),
            Tag("async", 55),
        ),
        upvotes = 512,
        downvotes = 24,
        commentCount = 87,
        createdAt = sampleTimestamp,
        updatedAt = sampleTimestamp,
    )

    /**
     * Low engagement post - use for edge case previews
     */
    val unpopularPost = Post(
        id = PostId("e5f6789012345678abcdef0123456789"),
        authorId = UserId("user111"),
        authorName = "Dev Newbie",
        authorAvatarUrl = "https://picsum.photos/100/100?random=30",
        title = "My first Android app journey",
        content = "Just published my first app to the Play Store! Here's what I learned " +
            "along the way and mistakes I made so you don't have to.",
        imageUrls = emptyList(),
        tags = listOf(
            Tag("beginner", 15),
        ),
        upvotes = 12,
        downvotes = 1,
        commentCount = 8,
        createdAt = sampleTimestamp,
        updatedAt = sampleTimestamp,
    )
}
