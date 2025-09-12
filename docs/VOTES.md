# Voting System Documentation

## Overview

Enterprise-level voting system for Qode application supporting 3-state voting (upvote, downvote, remove) across all content types: promo codes, posts, comments, and promos.

## Architecture

### Database Structure

**Unified Votes Collection**: `/votes/{sanitized_itemId}_{sanitized_userId}`

```typescript
interface VoteDocument {
  itemId: string;           // ID of the content being voted on
  itemType: 'PROMO_CODE' | 'POST' | 'COMMENT' | 'PROMO';
  userId: string;           // ID of the user voting
  isUpvote: boolean;        // true = upvote, false = downvote
  createdAt: Timestamp;     // When vote was created
  updatedAt: Timestamp;     // When vote was last modified
}
```

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

## Implementation Plan

### Phase 1: Foundation - Unified Vote Models ✅ **COMPLETED**

**Files updated:**
- ✅ `shared/src/commonMain/kotlin/com/qodein/shared/model/InteractionModels.kt`
- ✅ `shared/src/commonMain/kotlin/com/qodein/shared/model/ContentModels.kt` (removed PromoCodeVote)

**Changes implemented:**
1. ✅ Replaced `UserVote.isUpvote: Boolean` with `VoteState` enum (UPVOTE, DOWNVOTE, NONE)
2. ✅ Consolidated `UserVote` and `PromoCodeVote` into single unified `Vote` model
3. ✅ Added `VoteType.PROMO` for complete content type support
4. ✅ Added proper validation and business logic with init blocks
5. ✅ Created vote state transition methods (`toggleUpvote()`, `toggleDownvote()`, `remove()`)
6. ✅ Updated ID generation to match Firebase format: `sanitized_itemId_sanitized_userId`

```kotlin
@Serializable
data class Vote(
    val id: String,
    val userId: UserId,
    val itemId: String,
    val itemType: VoteType,
    val voteState: VoteState,
    val createdAt: Instant = Clock.System.now(),
    val updatedAt: Instant = Clock.System.now()
) {
    companion object {
        fun create(
            userId: UserId,
            itemId: String, 
            itemType: VoteType,
            voteState: VoteState
        ): Vote
        
        private fun generateId(itemId: String, userId: String): String
    }
    
    fun toggleUpvote(): VoteState
    fun toggleDownvote(): VoteState
    fun remove(): VoteState
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


### Phase 3: Business Logic & UI Integration 🚧 **NEXT PHASE**

**Files to update:**
- `feature/promocode/src/main/java/com/qodein/feature/promocode/detail/PromocodeDetailViewModel.kt`

**Current Issues Identified:**
1. **Legacy Model References**: Still imports `PromoCodeVote` instead of `Vote`
2. **Boolean Vote Logic**: Uses `handleVote(isUpvote: Boolean)` instead of 3-state `VoteState`
3. **Inconsistent Vote Mapping**: `updatePromoCodeWithVoteState` uses old Boolean logic
4. **Wrong Use Case Integration**: Expects different parameters than new use cases provide

**Required Changes:**
1. **Update Imports**: Replace `PromoCodeVote` with `Vote`, remove legacy references
2. **Refactor Vote Handling**: Replace Boolean logic with `VoteState` transitions
3. **Fix State Management**: Use `Vote.voteState` instead of `isUpvote` Boolean
4. **Update Use Case Calls**: Match new suspend signatures and `VoteState` parameters
5. **Implement 3-State Logic**: Clean toggle behavior (tap upvote twice = remove)
6. **Fix Optimistic Updates**: Handle all vote states in UI predictions

**Benefits After Completion:**
- ✅ **Fixes NOT_FOUND Errors**: Proper model integration and user ID propagation
- ✅ **Superior UX**: 3-state voting with intuitive toggle behavior
- ✅ **Type Safety**: No more Boolean/VoteType confusion
- ✅ **Real-time Updates**: Live vote monitoring with proper error handling

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
- Vote change notifications
- Vote analytics and engagement metrics
- Admin moderation tools
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

## Testing Strategy

### Unit Tests
- Vote state transitions
- Document ID generation
- Error handling scenarios
- Use case logic

### Integration Tests
- Cloud Functions voting workflow
- Real-time vote count updates
- Authentication integration
- Cross-platform consistency

### UI Tests
- Vote button interactions
- Loading states and animations
- Error message display
- Accessibility compliance

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
1. **Vote Migration**: Convert existing votes to new unified structure
2. **Count Verification**: Validate vote counts match actual vote records
3. **Backward Compatibility**: Maintain old API during transition period
4. **Rollback Plan**: Quick revert capability if issues arise

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
