# Models Architecture

This document describes the comprehensive model architecture after the major refactoring that consolidated 20+ scattered model files into 6 organized files with proper Firebase integration.

## 📁 **Model Organization**

### **shared/src/commonMain/kotlin/com/qodein/shared/model/**
- `UserModels.kt` - All user-related models (User, UserProfile, UserStats, etc.)
- `ContentModels.kt` - Content models (Post, PromoCode, Comment, Banner, Tag, Promo)
- `InteractionModels.kt` - User interaction models (UserBookmark, UserVote, UserActivity)
- `Service.kt` - Service/brand models

### **core/data/src/main/java/com/qodein/core/data/model/**
- `ContentDtos.kt` - Firebase DTOs for all content models
- `InteractionDtos.kt` - Firebase DTOs for user interaction models

### **core/data/src/main/java/com/qodein/core/data/mapper/**
- Individual mappers for each model type (PostMapper, PromoCodeMapper, etc.)

## 🏗️ **Model Architecture Principles**

### **1. Clean Domain Models**
- **Enterprise-level validation**: Comprehensive `init` blocks with meaningful error messages
- **Factory methods**: Safe creation with `Result` patterns and validation
- **Immutable data classes**: All models are immutable with `val` properties
- **Type safety**: Value classes for IDs (`UserId`, `PostId`, `PromoCodeId`, etc.)

### **2. Firebase Optimization**
- **Denormalization**: Author info (username, avatar, country) denormalized for fast display
- **NoSQL-friendly**: Flat structures optimized for Firestore queries
- **Auto-verification**: PromoCode/Promo auto-verified at 10+ upvotes
- **Country targeting**: Support for global and country-specific content

### **3. Simplified Architecture**
- **Removed over-engineering**: Eliminated redundant computed properties
- **Single metrics**: Only `voteScore` (upvotes - downvotes) matters
- **No comment counts**: Lazy loading strategy to avoid data inconsistency
- **Single-word categories**: Clean, simple categorization

## 📋 **Content Models**

### **Post Model**
```kotlin
data class Post(
    val id: PostId,
    val authorId: UserId,
    val authorUsername: String, // Denormalized
    val authorAvatarUrl: String? = null, // Denormalized
    val authorCountry: String? = null, // Denormalized
    val title: String? = null,
    val content: String,
    val imageUrls: List<String> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val shares: Int = 0,
    val createdAt: Instant = Clock.System.now(),
    // User interaction flags (computed at query time)
    val isUpvotedByCurrentUser: Boolean = false,
    val isDownvotedByCurrentUser: Boolean = false,
    val isBookmarkedByCurrentUser: Boolean = false
) {
    val voteScore: Int get() = upvotes - downvotes
}
```

### **PromoCode Model (Sealed Hierarchy)**
```kotlin
sealed class PromoCode {
    data class PercentagePromoCode(
        val id: PromoCodeId,
        val code: String,
        val serviceId: ServiceId? = null, // Reference to Service document
        val serviceName: String, // Denormalized for fast display/filtering
        val category: String?, // Denormalized for fast filtering
        val title: String,
        val description: String?,
        val discountPercentage: Double,
        val minimumOrderAmount: Double = 0.0,
        val startDate: Instant,
        val endDate: Instant,
        val isFirstUserOnly: Boolean = false,
        val upvotes: Int = 0,
        val downvotes: Int = 0,
        val views: Int = 0,
        val shares: Int = 0,
        val screenshotUrl: String? = null,
        val targetCountries: List<String> = emptyList(),
        val isVerified: Boolean = false, // Auto-verified at voteScore >= 10
        val createdAt: Instant = Clock.System.now(),
        val createdBy: UserId? = null,
        val isUpvotedByCurrentUser: Boolean = false,
        val isDownvotedByCurrentUser: Boolean = false,
        val isBookmarkedByCurrentUser: Boolean = false
    ) : PromoCode()

    data class FixedAmountPromoCode(
        // Similar structure with serviceId reference and discountAmount instead of discountPercentage
    ) : PromoCode()
}
```

### **Promo Model (User-submitted deals)**
```kotlin
data class Promo(
    val id: PromoId,
    val title: String,
    val description: String,
    val imageUrls: List<String> = emptyList(),
    val serviceName: String,
    val category: String?,
    val targetCountries: List<String> = emptyList(),
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val views: Int = 0,
    val shares: Int = 0,
    val isVerified: Boolean = false, // Auto-verified at voteScore >= 10
    val createdBy: UserId,
    val createdAt: Instant = Clock.System.now(),
    val expiresAt: Instant? = null,
    val isUpvotedByCurrentUser: Boolean = false,
    val isDownvotedByCurrentUser: Boolean = false,
    val isBookmarkedByCurrentUser: Boolean = false
) {
    val voteScore: Int get() = upvotes - downvotes
    val isExpired: Boolean get() = expiresAt?.let { Clock.System.now() > it } ?: false
}
```

### **Comment Model (Unified)**
```kotlin
data class Comment(
    val id: CommentId,
    val parentId: String, // PromoCodeId.value or PostId.value
    val parentType: CommentParentType, // PROMO_CODE or POST
    val authorId: UserId,
    val authorUsername: String, // Denormalized
    val authorAvatarUrl: String? = null, // Denormalized
    val authorCountry: String? = null, // Denormalized
    val content: String,
    val imageUrls: List<String> = emptyList(),
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val createdAt: Instant = Clock.System.now(),
    val isUpvotedByCurrentUser: Boolean = false,
    val isDownvotedByCurrentUser: Boolean = false
)
```

## 🏪 **Service Models**

### **Service Model (Category → Service Hierarchy)**
```kotlin
data class Service(
    val id: ServiceId, // Composite ID: "food_mcdonalds", "transport_uber"
    val name: String, // "McDonalds", "Uber"
    val category: String, // "Food", "Transport" 
    val logoUrl: String? = null,
    val isPopular: Boolean = false,
    val promoCodeCount: Int = 0, // Denormalized counter for UI display
    val createdAt: Instant = Clock.System.now()
) {
    companion object {
        // Generates composite IDs like "food_mcdonalds"
        private fun generateServiceId(name: String, category: String): String {
            val sanitizedName = name.trim().lowercase().replace(Regex("[^a-z0-9]"), "_")
            val sanitizedCategory = category.trim().lowercase().replace(Regex("[^a-z0-9]"), "_")
            return "${sanitizedCategory}_$sanitizedName"
        }
        
        // Predefined categories for consistency
        object Categories {
            const val STREAMING = "Streaming"
            const val FOOD = "Food"
            const val TRANSPORT = "Transport"
            // ... 15 total categories
            val ALL = listOf(STREAMING, FOOD, TRANSPORT, /*...*/)
        }
    }
}
```

**Benefits of Composite Service IDs:**
- 🎯 **Natural hierarchy**: `food_mcdonalds` clearly shows category → service relationship
- 🔍 **Unique identification**: No collision between services with same name in different categories
- 📊 **Firebase optimization**: Works great as document IDs in `/services/{category_servicename}`
- ⚡ **Query efficiency**: Can filter by category prefix if needed

## 👤 **User Models**

### **User Model**
```kotlin
data class User(
    val id: UserId,
    val email: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val country: String? = null, // ISO country code
    val isEmailVerified: Boolean = false,
    val karma: Int = 0, // Points from community contributions
    val createdAt: Instant = Clock.System.now(),
    val lastActiveAt: Instant = Clock.System.now()
)
```

## 🔗 **Interaction Models**

### **UserBookmark**
```kotlin
data class UserBookmark(
    val id: String, // Generated: userId_itemId
    val userId: UserId,
    val itemId: String, // PromoCodeId.value or PostId.value
    val itemType: BookmarkType, // PROMO_CODE or POST
    val itemTitle: String, // Denormalized for quick display
    val itemCategory: String? = null, // Denormalized for filtering
    val createdAt: Instant = Clock.System.now()
)
```

### **UserVote**
```kotlin
data class UserVote(
    val id: String, // Generated: userId_itemId
    val userId: UserId,
    val itemId: String, // PromoCodeId.value, PostId.value, or CommentId.value
    val itemType: VoteType, // PROMO_CODE, POST, or COMMENT
    val isUpvote: Boolean, // true = upvote, false = downvote
    val createdAt: Instant = Clock.System.now(),
    val updatedAt: Instant = Clock.System.now()
)
```

## 🗃️ **Firestore Structure**

### **Collections**
```
/services/{category_servicename}
  - id: "food_mcdonalds", "transport_uber"
  - name: "McDonalds", "Uber"  
  - category: "Food", "Transport"
  - promoCodeCount: 15 (denormalized counter)
  - isPopular: true
  - logoUrl: "https://..."

/promocodes/{promoCodeId}
  - serviceId: "food_mcdonalds" (reference to Service)
  - serviceName: "McDonalds" (denormalized for fast queries)
  - category: "Food" (denormalized for filtering)
  - code, title, description, etc.
  /comments/{commentId} - CommentDto
  /votes/{userId} - Vote data

/posts/{postId}
  - PostDto fields
  /comments/{commentId} - CommentDto  
  /votes/{userId} - Vote data

/promos/{promoId}
  - PromoDto fields
  /votes/{userId} - Vote data
  /bookmarks/{userId} - Bookmark data

/users/{userId}
  - UserDto fields
  /bookmarks/{itemId} - UserBookmarkDto

/user_activities/{activityId}
  - UserActivityDto fields
```

### **Firebase Collections Strategy**
- **Main collections**: `promocodes`, `posts`, `promos`, `services`, `users`
- **Hierarchical services**: Composite IDs (`food_mcdonalds`) enable category → service navigation
- **Denormalized relationships**: ServiceId + serviceName in promo codes for performance
- **Subcollections**: `comments`, `votes`, `bookmarks` under parent documents
- **Global collections**: `user_activities` for cross-content analytics
- **Service counters**: `promoCodeCount` denormalized for fast UI display

## 🛠️ **Mappers**

Each model has a corresponding mapper for Firebase DTO conversion:

### **Mapper Pattern**
```kotlin
object PostMapper {
    fun toDomain(dto: PostDto): Post { /* validation + conversion */ }
    fun toDto(domain: Post): PostDto { /* conversion */ }
    fun toDomainList(dtos: List<PostDto>): List<Post> { /* with error handling */ }
    fun toDtoList(posts: List<Post>): List<PostDto> { /* batch conversion */ }
}
```

### **Error Handling**
- **Validation**: `require()` statements with meaningful messages
- **Graceful degradation**: `mapNotNull` for list conversions
- **Exception safety**: Try-catch blocks to prevent cascading failures

## 🔄 **Repository Layer**

### **Repository Interfaces**
- `PromoCodeRepository` - PromoCode CRUD + voting + services
- `PostRepository` - Post CRUD + voting + bookmarking + trending
- `PromoRepository` - Promo CRUD + voting + country targeting
- `CommentRepository` - Unified comments with subcollection handling
- `UserInteractionRepository` - Bookmarks, votes, activity tracking

### **Data Source Layer**
- `FirestorePromoCodeDataSource` - Firebase operations for promo codes
- `FirestorePostDataSource` - Firebase operations for posts
- `FirestorePromoDataSource` - Firebase operations for promos
- `FirestoreCommentDataSource` - Subcollection-based comment operations

## 📊 **Key Improvements**

### **Before Refactoring**
- ❌ 20+ scattered model files
- ❌ Over-engineered computed properties (totalVotes, popularityScore, engagementScore)
- ❌ Inconsistent validation patterns
- ❌ Comment count tracking (data inconsistency risk)
- ❌ Multi-word categories
- ❌ Redundant repository methods

### **After Refactoring**
-  6 organized model files (600 lines, well-structured)
-  Single essential metric: `voteScore = upvotes - downvotes`
-  Consistent factory methods with Result patterns
-  Lazy comment loading via subcollections
-  Single-word categories ("Food", "Transport", "Beauty")
-  Clean repository interfaces following NIA patterns
-  Proper separation: PromoCode (actual codes) vs Promo (user deals)
-  Auto-verification system (10+ upvotes = verified)
-  Country-specific targeting for Kazakhstan market

## 🎯 **Usage Examples**

### **Creating a Post**
```kotlin
val post = Post.create(
    authorId = currentUser.id,
    authorUsername = currentUser.username,
    title = "Best local deals in Almaty",
    content = "Found these amazing discounts...",
    tags = listOf(Tag.create("deals"), Tag.create("almaty"))
).getOrThrow()
```

### **Repository Usage**
```kotlin
// Get posts with real-time updates
postRepository.getPosts(
    sortBy = PostSortBy.TRENDING,
    limit = 20
).asResult().collect { result ->
    when (result) {
        is Result.Loading -> showLoading()
        is Result.Success -> displayPosts(result.data)
        is Result.Error -> showError(result.exception)
    }
}
```

This architecture provides a solid foundation for the promo code app with proper Firebase integration, enterprise-level validation, and clean separation of concerns.
