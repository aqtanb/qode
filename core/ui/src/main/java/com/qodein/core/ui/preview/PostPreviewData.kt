package com.qodein.core.ui.preview

import com.qodein.shared.common.Result
import com.qodein.shared.model.Post
import com.qodein.shared.model.UserId

/**
 * Centralized preview data for Post composables
 * Provides consistent test data across all preview functions
 *
 * Note: This is separate from core:testing/TestPosts to avoid test dependencies in production code
 */
object PostPreviewData {

    /**
     * Post with multiple images - use for carousel/pager previews
     */
    val postWithImages by lazy {
        (
            Post.create(
                authorId = UserId("user123"),
                authorUsername = "John Doe",
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
                tags = listOf("android", "mobile", "development"),
                authorAvatarUrl = null,
            ) as Result.Success
            ).data
    }

    /**
     * Post without images - use for text-only layout previews
     */
    val postWithoutImages by lazy {
        (
            Post.create(
                authorId = UserId("user456"),
                authorUsername = "Sarah Smith",
                title = "Best coding practices for clean architecture",
                content = "Clean architecture is essential for maintainable code. Here are the " +
                    "top practices that every developer should follow to write better, more organized " +
                    "code that scales.",
                imageUrls = emptyList(),
                tags = listOf("coding", "architecture", "best_practices"),
                authorAvatarUrl = "https://picsum.photos/100/100?random=10",
            ) as Result.Success
            ).data
    }

    /**
     * Post with single image - use for single image layout previews
     */
    val postWithSingleImage by lazy {
        (
            Post.create(
                authorId = UserId("user789"),
                authorUsername = "Alex Chen",
                title = "Jetpack Compose state management deep dive",
                content = "Understanding state in Compose is crucial for building reactive UIs. " +
                    "This guide covers everything from remember to derivedStateOf and beyond.",
                imageUrls = listOf("https://picsum.photos/400/300?random=4"),
                tags = listOf("compose", "android", "state_management"),
                authorAvatarUrl = "https://picsum.photos/100/100?random=20",
            ) as Result.Success
            ).data
    }

    /**
     * High engagement post - use for vote/comment count previews
     */
    val popularPost by lazy {
        (
            Post.create(
                authorId = UserId("user999"),
                authorUsername = "Maria Garcia",
                title = "Kotlin coroutines: From basics to advanced patterns",
                content = "Master coroutines with this comprehensive guide covering flows, " +
                    "channels, and structured concurrency patterns that will make your async code shine.",
                imageUrls = listOf(
                    "https://picsum.photos/400/300?random=5",
                    "https://picsum.photos/400/300?random=6",
                ),
                tags = listOf("kotlin", "coroutines", "async"),
                authorAvatarUrl = null,
            ) as Result.Success
            ).data
    }

    /**
     * Low engagement post - use for edge case previews
     */
    val unpopularPost by lazy {
        (
            Post.create(
                authorId = UserId("user111"),
                authorUsername = "Dev Newbie",
                title = "My first Android app journey",
                content = "Just published my first app to the Play Store! Here's what I learned " +
                    "along the way and mistakes I made so you don't have to.",
                imageUrls = emptyList(),
                tags = listOf("beginner"),
                authorAvatarUrl = "https://picsum.photos/100/100?random=30",
            ) as Result.Success
            ).data
    }

    val postWithLongEverything by lazy {
        (
            Post.create(
                authorId = UserId("user123"),
                authorUsername = "VeryLongUsernameToTestWrapping",
                title = "A".repeat(200), // Exactly 200 chars - max title length
                content = "B".repeat(2000), // Exactly 2000 chars - max content length
                imageUrls = listOf(
                    "https://picsum.photos/400/300?random=1",
                    "https://picsum.photos/400/300?random=2",
                ),
                tags = listOf(
                    "verylongtagnamethatshould",
                    "wraptomultiplelines",
                    "development",
                    "quiteverylong",
                    "android",
                ),
                authorAvatarUrl = null,
            ) as Result.Success
            ).data
    }

    val allPosts by lazy {
        listOf(
            postWithImages,
            postWithoutImages,
            postWithSingleImage,
            popularPost,
            unpopularPost,
        )
    }
}
