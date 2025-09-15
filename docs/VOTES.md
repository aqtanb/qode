# User Interactions System Documentation

## Overview

**Cost-Optimized** user interaction system for Qode application supporting unified votes and bookmarks with **33% read reduction** through single-document architecture.

## Architecture Philosophy

### **Read Efficiency First**
- **Detail Screen**: 2 reads (content + user_interaction) vs 3 reads (content + vote + bookmark)
- **Feed Screen**: 0 extra reads (no user-specific data loaded)
- **Cost Savings**: 33% reduction in Firestore reads + cleaner separation of concerns

### **Clean Model Separation**
- **Content Models**: Pure content data (PromoCode, Post, Comment) with no user-specific fields
- **User Interaction**: Separate model containing all user-specific states
- **View Models**: Combined models for UI layer when user context needed

## Database Structure

### **Unified User Interactions Collection**

**Collection**: `/user_interactions/{itemId}_{userId}`

```typescript
interface UserInteractionDocument {
  itemId: string;           // ID of the content (promo code, post, etc.)
  itemType: 'PROMO_CODE' | 'POST' | 'COMMENT' | 'PROMO';
  userId: string;           // ID of the user
  voteState: 'UPVOTE' | 'DOWNVOTE' | null;  // Vote state or null for no vote
  isBookmarked: boolean;    // Bookmark status
  createdAt: Timestamp;     // When interaction was created
  updatedAt: Timestamp;     // When interaction was last modified
}
```

**Key Benefits:**
- **Single Document**: All user interactions in one place
- **Atomic Updates**: Vote + bookmark changes in single transaction
- **Predictable Costs**: Exactly 1 read per user-content pair

### Vote States

```kotlin
enum class VoteState {
    UPVOTE,    // User has upvoted
    DOWNVOTE,  // User has downvoted  
    NONE       // User has no vote (removed or never voted)
}
```

### Content Collections

Vote counts are denormalized into content documents:

```typescript
interface ContentDocument {
  upvotes: number;      // Total upvote count
  downvotes: number;    // Total downvote count  
  voteScore: number;    // upvotes - downvotes (computed)
  // ... other fields
}
```

## Architecture Decisions

### **Suspend vs Flow Pattern**

**Decision**: Use `suspend fun` for voting actions and `Flow<T>` for vote monitoring.

**Rationale**:
- **Voting Actions**: Fire-and-forget operations that complete once
- **Vote Monitoring**: Continuous stream of real-time updates from Firestore
- **Error Handling**: Integrates perfectly with `resultOf {}` and `.asResult()` patterns
- **Performance**: Avoids unnecessary Flow overhead for one-time operations

```kotlin
// Voting: One-time action
suspend fun voteOnContent(...): Vote?

// Monitoring: Real-time stream  
fun getUserVote(...): Flow<Vote?>
```

### **Error Handling Integration**

**Pattern**: Repository throws exceptions, Use cases handle with Result patterns.

```kotlin
// Repository: Throws IOException, SecurityException
suspend fun voteOnContent(...): Vote?

// Use Case: Converts to Result<T>
suspend fun invoke(...): Result<Vote?> = resultOf { repository.voteOnContent(...) }
```

### **DRY Principle Application**

**Problem**: Original code had redundant methods for each content type.

**Solution**: Single generic repository methods with type-safe use case wrappers.

```kotlin
// Data Layer: Generic, reusable
fun voteOnContent(itemId: String, itemType: VoteType, ...)

// Use Case Layer: Type-safe, domain-specific
class VoteOnPromocodeUseCase {
    suspend fun invoke(promoCodeId: PromoCodeId, ...) = 
        repository.voteOnContent(promoCodeId.value, VoteType.PROMO_CODE, ...)
}
```

## New Unified Architecture

### **Core Models**

#### **Clean Content Models (No User Fields)**
```kotlin
// Pure content data - globally cacheable
data class PromoCode(
    val id: PromoCodeId,
    val code: String,
    val serviceName: String,
    val upvotes: Int,
    val downvotes: Int,
    val voteScore: Int,
    // ❌ Removed: isUpvotedByCurrentUser, isDownvotedByCurrentUser, isBookmarkedByCurrentUser
)
```

#### **Unified User Interaction Model**
```kotlin
// All user-specific interactions in one place
@Serializable
data class UserInteraction(
    val id: String,                    // itemId_userId
    val itemId: String,               // Content ID
    val itemType: InteractionType,    // PROMO_CODE, POST, COMMENT
    val userId: UserId,               // User ID
    val voteState: VoteState?,        // UPVOTE, DOWNVOTE, or null
    val isBookmarked: Boolean,        // Bookmark status
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        fun create(
            itemId: String,
            itemType: InteractionType,
            userId: UserId,
            voteState: VoteState? = null,
            isBookmarked: Boolean = false
        ): UserInteraction
    }
}
```

#### **View Models for UI**
```kotlin
// Combined models when user context needed
data class PromoCodeWithUserState(
    val promoCode: PromoCode,
    val userState: UserInteraction?
) {
    val isUpvotedByCurrentUser: Boolean
        get() = userState?.voteState == VoteState.UPVOTE
    val isDownvotedByCurrentUser: Boolean
        get() = userState?.voteState == VoteState.DOWNVOTE
    val isBookmarkedByCurrentUser: Boolean
        get() = userState?.isBookmarked == true
}
```

## Implementation Plan - Unified Architecture

### Phase 6: **CURRENT** - Unified User Interactions ⏳ **IN PROGRESS**

**Goal**: Combine votes + bookmarks into single document, remove user fields from content models.

**Benefits**:
- **33% Read Reduction**: 2 reads instead of 3 per detail screen
- **Cleaner Architecture**: Pure content models + separate user state
- **Atomic Updates**: Single transaction for vote + bookmark changes
- **Better Caching**: Content cached globally, user state per-user

**Files to Update**:

1. **Models & Core Architecture** (5 files):
   - `shared/model/InteractionModels.kt` - Add UserInteraction model
   - `shared/model/ContentModels.kt` - Remove user fields from PromoCode/Post
   - `core/data/model/ContentDtos.kt` - Clean Firestore DTOs
   - `shared/model/ViewModels.kt` - Add PromoCodeWithUserState

2. **Data Sources** (3 files):
   - `core/data/datasource/FirestoreUserInteractionDataSource.kt` - New unified data source
   - `core/data/datasource/FirestorePromocodeDataSource.kt` - Update with parallel fetching
   - Deprecate separate VoteDataSource and BookmarkDataSource

3. **Repository Layer** (2 files):
   - `core/data/repository/UserInteractionRepositoryImpl.kt` - New repository
   - `shared/domain/repository/UserInteractionRepository.kt` - Repository interface

4. **UI Components** (8 files):
   - Update all components using user interaction fields
   - `feature/promocode/detail/component/ActionButtonsSection.kt`
   - `feature/promocode/detail/PromocodeDetailViewModel.kt`
   - All mappers and preview components

5. **Cloud Functions** (2 files):
   - `functions/src/userInteractionHandler.ts` - New unified handler
   - Update Firestore security rules

**Architecture Changes**:

```kotlin
// Before: 3 separate reads
suspend fun getPromoCodeById(id: PromoCodeId): PromoCode?
suspend fun getUserVote(itemId: String, userId: UserId): Vote?
suspend fun getUserBookmark(itemId: String, userId: UserId): UserBookmark?

// After: 2 parallel reads
suspend fun getPromoCodeWithUserState(
    id: PromoCodeId,
    userId: UserId
): PromoCodeWithUserState? = coroutineScope {
    val promoCodeDeferred = async { getPromoCodeById(id) }
    val userStateDeferred = async { getUserInteraction(id.value, userId) }

    val promoCode = promoCodeDeferred.await() ?: return@coroutineScope null
    val userState = userStateDeferred.await()

    PromoCodeWithUserState(promoCode, userState)
}
```

### Phase 2: Complete Data Layer & Repository Implementation ✅ **COMPLETED**

**Files updated:**
- ✅ `core/data/src/main/java/com/qodein/core/data/datasource/FirestoreVoteDataSource.kt`
- ✅ `core/data/src/main/java/com/qodein/core/data/model/InteractionDtos.kt`
- ✅ `core/data/src/main/java/com/qodein/core/data/mapper/UserVoteMapper.kt` → `VoteMapper.kt`
- ✅ `shared/src/commonMain/kotlin/com/qodein/shared/domain/repository/VoteRepository.kt`
- ✅ `core/data/src/main/java/com/qodein/core/data/repository/VoteRepositoryImpl.kt`
- ✅ Use cases updated with enterprise-level error handling

**Architecture improvements implemented:**
1. ✅ **Clean Suspend/Flow Pattern**: `suspend fun` for actions, `Flow<T>` for monitoring
2. ✅ **Enterprise Error Handling**: `resultOf {}` and `.asResult()` integration
3. ✅ **DRY Principle**: Single generic methods, no redundant vote logic
4. ✅ **Real-time Updates**: Firestore listeners for live vote monitoring
5. ✅ **Firebase Compatibility**: Document ID generation matches Cloud Functions
6. ✅ **Legacy Code Removal**: Complete elimination of `PromoCodeVote` system

**Final Architecture:**
```kotlin
// Data Source: Clean separation of concerns
suspend fun voteOnContent(...): Vote?     // Fire-and-forget voting
fun getUserVote(...): Flow<Vote?>          // Real-time monitoring

// Use Cases: Proper error handling integration
suspend fun invoke(...): Result<Vote?> = resultOf { repository.voteOnContent(...) }
fun invoke(...): Flow<Result<Vote?>> = repository.getUserVote(...).asResult()
```


### Phase 3: Business Logic & UI Integration ✅ **COMPLETED**

**Files updated:**
- ✅ `feature/promocode/src/main/java/com/qodein/feature/promocode/detail/PromocodeDetailViewModel.kt`
- ✅ `feature/promocode/src/main/java/com/qodein/feature/promocode/detail/PromocodeDetailUiState.kt`
- ✅ `feature/promocode/src/main/java/com/qodein/feature/promocode/detail/component/ActionButtonsSection.kt`
- ✅ `functions/src/voteHandler.ts` (fixed TypeScript errors and deployment)

**Issues Fixed:**
1. ✅ **Legacy Model References**: Replaced `PromoCodeVote` with `Vote`, updated all imports
2. ✅ **Boolean Vote Logic**: Converted to 3-state `VoteState` with `handleUpvote()`/`handleDownvote()` methods
3. ✅ **State Management**: Updated `updatePromoCodeWithVoteState` to use `vote.voteState` enum matching
4. ✅ **Use Case Integration**: Fixed method calls to match new suspend signatures with `VoteState` parameters
5. ✅ **3-State Logic**: Implemented clean toggle behavior (tap upvote twice = remove vote)
6. ✅ **Optimistic Updates**: Fixed to handle all vote states (UPVOTE/DOWNVOTE/NONE) correctly
7. ✅ **Cloud Function Deployment**: Fixed TypeScript errors and deployed missing `handleContentVote` function
8. ✅ **Authentication Flow**: Verified Firebase Auth context properly passed to Cloud Functions

**Major Achievements:**
- ✅ **Fixed NOT_FOUND Errors**: Root cause was missing Cloud Function deployment, not auth issues
- ✅ **Superior UX**: 3-state voting with intuitive toggle behavior implemented
- ✅ **Type Safety**: Eliminated Boolean/VoteType confusion with direct `VoteState` enum usage
- ✅ **Real-time Updates**: Live vote monitoring with proper error handling integration
- ✅ **Cloud Function Integration**: All 4 functions deployed and working (`handleContentVote`, `initializeVoteScores`, `updateServicePromoCounts`, `updatePromoCodeVoteScore`)

**Architecture Notes:**
- ViewModel is 673 lines (architectural debt to address in future refactor)
- Current implementation is functional and maintainable
- Error handling uses existing Result/ErrorType system effectively

### Phase 4: Firebase Integration & Cloud Functions ✅ **VERIFIED**

**Files verified:**
- ✅ `functions/src/voteHandler.ts` (already well-implemented)
- ✅ `firestore.rules` (already configured correctly)

**Status**: Cloud Functions are properly implemented with 3-state support. No changes needed.

**Key features already working:**
- ✅ Atomic transactions for vote + count updates
- ✅ Proper document ID sanitization matching client format
- ✅ 3-state vote logic (add/remove/switch)
- ✅ Authentication validation
- ✅ Comprehensive error handling
- ✅ Real-time vote count updates

### Phase 5: UI/UX Polish & Testing

**Files to update:**
- `feature/promocode/src/main/java/com/qodein/feature/promocode/detail/component/ActionButtonsSection.kt`

**Changes:**
1. Add visual feedback for "remove vote" action (3-state button indicators)
2. Update button states to clearly show all 3 vote states
3. Improve error handling with proper user messaging via `QodeActionErrorCard`
4. Add haptic feedback for vote state transitions
5. Implement loading states during vote operations

### Phase 6: Universal Content Support

**New files to create:**
- Generic voting components for Posts, Comments, Promos
- Unified vote count management system
- Real-time vote sync across content types
- Content-specific use case wrappers (PostVoteUseCase, CommentVoteUseCase, etc.)

### Phase 7: Enterprise Features

**Advanced features:**
- Vote analytics and engagement metrics
- Comprehensive logging and monitoring
- Vote fraud detection
- Performance optimization

## Error Handling Strategy

### Common Errors & Solutions

1. **NOT_FOUND Error**
   - **Cause**: Document ID mismatch or missing user ID
   - **Fix**: Ensure consistent ID generation and proper user propagation

2. **Authentication Errors**
   - **Cause**: User not signed in or invalid token
   - **Fix**: Proper auth state management and token refresh

3. **Network Errors**
   - **Cause**: Connectivity issues or Firebase timeout
   - **Fix**: Retry logic with exponential backoff

4. **Optimistic Update Conflicts**
   - **Cause**: Local state out of sync with server
   - **Fix**: Server state refresh and conflict resolution

## Performance & Security Considerations

### **Performance Optimizations**

1. **Smart Flow Management**: 
   - `suspend fun` for one-time vote actions (no Flow overhead)
   - `Flow<Vote?>` only for real-time vote monitoring
   - Firestore listeners automatically managed with `awaitClose`

2. **Denormalized Architecture**: 
   - Vote counts stored in content documents for fast reads
   - Individual votes in unified `/votes` collection for management
   - Cloud Functions handle atomic updates to both collections

3. **Real-time Efficiency**: 
   - Only subscribe to user's own vote state (not all votes)
   - Vote counts updated via content document listeners
   - Optimistic UI updates with server reconciliation

4. **Document ID Optimization**:
   - Format: `sanitized_itemId_sanitized_userId`
   - Predictable IDs enable efficient queries and caching
   - Matches Cloud Functions format exactly

### **Security Implementation**

1. **Server-side Validation**: 
   - All vote operations validated by Firebase Cloud Functions
   - Client cannot directly modify vote counts or documents
   - Atomic transactions prevent race conditions

2. **Authentication Integration**: 
   - No anonymous voting allowed
   - User ID validated on every operation
   - Proper error handling for auth state changes

3. **Data Integrity**: 
   - Document ID sanitization prevents injection attacks
   - Type-safe error handling with `IOException` and `SecurityException`
   - Firestore security rules enforce proper access patterns

4. **Error Recovery**:
   - Optimistic updates with automatic rollback on failure
   - Real-time listeners ensure UI stays in sync
   - Proper exception propagation through Result patterns

## Monitoring & Analytics

### Vote Metrics
- Vote engagement rates by content type
- Vote state transition patterns
- User voting behavior analysis
- Content popularity trends

### System Health
- Vote operation success rates
- Response time monitoring  
- Error rate tracking
- Database performance metrics

## Migration Strategy

### Existing Data
2. **Count Verification**: Validate vote counts match actual vote records

### Deployment Phases
1. **Backend First**: Deploy Cloud Functions and database changes
2. **Gradual Rollout**: Feature flag controlled client deployment  
3. **Monitoring Period**: Watch metrics before full activation
4. **Legacy Cleanup**: Remove old code after successful migration

## Future Enhancements

1. **Vote Reasons**: Optional feedback on why users vote
2. **Vote Weighting**: Trusted user votes carry more weight
3. **Anti-Manipulation**: Advanced fraud detection
4. **Social Features**: Vote-based recommendations and discovery
