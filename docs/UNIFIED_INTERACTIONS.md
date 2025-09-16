# Unified User Interactions Implementation Plan

## 🎯 Executive Summary

**Current Problem**: 3 Firestore reads per detail screen (content + vote + bookmark)
**Target Solution**: 2 Firestore reads per detail screen (content + unified_interaction)
**Cost Reduction**: 33% fewer reads + atomic updates + cleaner architecture

## 🏗️ Final Architecture Decision

### Database Structure
```
/promo_codes/{id}                           // Clean content (no user fields)
/user_interactions/{itemId}_{userId}        // Unified vote + bookmark
/comments/{commentId}                       // Separate collection (loaded on comments screen)
```

### Interaction Flow
```
Detail Screen:    2 reads (content + user_interaction)
Comments Screen:  2 reads (comments + user_interaction)
Feed Screen:      20 reads (content only, no user data)
```

### Key Models
```kotlin
// Clean content model (no user fields)
sealed class PromoCode {
    // ✅ Keep: id, code, upvotes, downvotes, voteScore
    // ❌ Remove: isUpvotedByCurrentUser, isBookmarkedByCurrentUser
}

// Unified interaction model
data class UserInteraction(
    val id: String,                    // itemId_userId
    val itemId: String,               // Content ID
    val itemType: InteractionType,    // PROMO_CODE, POST, COMMENT
    val userId: UserId,
    val voteState: VoteState?,        // UPVOTE, DOWNVOTE, null
    val isBookmarked: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)

// UI view model combining clean content + user state
data class PromoCodeWithUserState(
    val promoCode: PromoCode,
    val userState: UserInteraction?
) {
    val isUpvotedByCurrentUser = userState?.voteState == VoteState.UPVOTE
    val isBookmarkedByCurrentUser = userState?.isBookmarked == true
}
```

---

## 📋 Implementation Phases

### 🔧 **Phase 1: Foundation & Models**

#### **Data Layer**
- [ ] **Create** `shared/model/UserInteractionModels.kt`
  - `UserInteraction` data class with validation
  - `InteractionType` enum (PROMO_CODE, POST, COMMENT)
  - Helper functions for ID generation

- [ ] **Create** `shared/model/ViewModels.kt`
  - `PromoCodeWithUserState` view model
  - `PostWithUserState` view model
  - Extension functions for combining content + user state

- [ ] **Update** `shared/model/ContentModels.kt`
  - Remove `isUpvotedByCurrentUser`, `isBookmarkedByCurrentUser` from `PromoCode`
  - Remove `isDownvotedByCurrentUser` field
  - Keep vote counts (`upvotes`, `downvotes`, `voteScore`)

- [ ] **Create** `core/data/model/UserInteractionDtos.kt`
  - `UserInteractionDto` for Firestore
  - Firebase Timestamp handling
  - Validation and sanitization

- [ ] **Create** `core/data/mapper/UserInteractionMapper.kt`
  - Map between `UserInteraction` ↔ `UserInteractionDto`
  - Handle null states and edge cases
  - Consistent ID generation with sanitization

#### **Domain Layer**
- [ ] **Create** `shared/domain/repository/UserInteractionRepository.kt`
  - `suspend fun getUserInteraction(itemId: String, userId: UserId): UserInteraction?`
  - `suspend fun upsertUserInteraction(interaction: UserInteraction): UserInteraction`
  - `fun observeUserInteraction(itemId: String, userId: UserId): Flow<UserInteraction?>`
  - `suspend fun deleteUserInteraction(itemId: String, userId: UserId)`


---

### 🗄️ **Phase 2: Data Sources & Repository Implementation**

#### **Data Layer**
- [ ] **Create** `core/data/datasource/FirestoreUserInteractionDataSource.kt`
  ```kotlin
  class FirestoreUserInteractionDataSource {
      suspend fun getUserInteraction(itemId: String, userId: String): UserInteractionDto?
      suspend fun upsertUserInteraction(dto: UserInteractionDto): UserInteractionDto
      fun observeUserInteraction(itemId: String, userId: String): Flow<UserInteractionDto?>
      suspend fun deleteUserInteraction(itemId: String, userId: String)

      // Batch operations for efficiency
      suspend fun getUserInteractions(userId: String): List<UserInteractionDto>
      suspend fun getInteractionsForContent(itemId: String): List<UserInteractionDto>
  }
  ```

- [ ] **Create** `core/data/repository/UserInteractionRepositoryImpl.kt`
  - Implement repository interface
  - Error handling with `IOException`, `SecurityException`
  - Proper Flow management with `callbackFlow`
  - Document ID generation: `sanitize(itemId)_sanitize(userId)`

- [ ] **Update** `core/data/di/DataModule.kt`
  - Bind `UserInteractionRepository` → `UserInteractionRepositoryImpl`
  - Provide `FirestoreUserInteractionDataSource`

#### **Domain Layer**
- [ ] **Create** `shared/domain/usecase/interaction/GetUserInteractionUseCase.kt`
  ```kotlin
  class GetUserInteractionUseCase {
      suspend operator fun invoke(itemId: String, userId: UserId): Result<UserInteraction?> =
          resultOf { repository.getUserInteraction(itemId, userId) }
  }
  ```

- [ ] **Create** `shared/domain/usecase/interaction/UpsertUserInteractionUseCase.kt`
  - Handle vote state changes
  - Handle bookmark toggles
  - Atomic updates for both vote + bookmark

- [ ] **Create** `shared/domain/usecase/interaction/ObserveUserInteractionUseCase.kt`
  - Real-time Flow with error handling
  - `.asResult()` integration


---

### 🎨 **Phase 3: UI Integration & ViewModels**

#### **Data Layer**
- [ ] **Update** `core/data/datasource/FirestorePromocodeDataSource.kt`
  - Add parallel fetching method: `getPromoCodeWithUserState()`
  - Use `coroutineScope` and `async` for concurrent reads
  ```kotlin
  suspend fun getPromoCodeWithUserState(
      id: PromoCodeId,
      userId: UserId
  ): PromoCodeWithUserStateDto? = coroutineScope {
      val promoCodeDeferred = async { getPromoCodeById(id) }
      val userStateDeferred = async { userInteractionDataSource.getUserInteraction(id.value, userId.value) }

      val promoCode = promoCodeDeferred.await() ?: return@coroutineScope null
      val userState = userStateDeferred.await()

      PromoCodeWithUserStateDto(promoCode, userState)
  }
  ```

#### **Domain Layer**
- [ ] **Update** `shared/domain/usecase/promocode/GetPromoCodeByIdUseCase.kt`
  - Add overload for user context: `invoke(id: PromoCodeId, userId: UserId): Result<PromoCodeWithUserState?>`
  - Keep existing method for cases without user context

- [ ] **Create** `shared/domain/usecase/interaction/VoteAndBookmarkUseCase.kt`
  - Combined operations for atomic updates
  - Handle optimistic UI updates
  - Proper error recovery

#### **UI Layer**
- [ ] **Update** `feature/promocode/detail/PromocodeDetailViewModel.kt`
  - Replace 3 separate calls with unified `getPromoCodeWithUserState()`
  - Update state management to use `PromoCodeWithUserState`
  - Remove direct access to user-specific fields from `PromoCode`

- [ ] **Update** `feature/promocode/detail/PromocodeDetailUiState.kt`
  - Change `promoCode: PromoCode` → `promoCodeWithUserState: PromoCodeWithUserState?`
  - Remove separate `vote: Vote?` and `bookmark: UserBookmark?` fields
  - Add computed properties for UI

- [ ] **Update** `feature/promocode/detail/component/ActionButtonsSection.kt`
  - Use `promoCodeWithUserState.isUpvotedByCurrentUser` instead of `promoCode.isUpvotedByCurrentUser`
  - Update click handlers to use unified interaction use cases

- [ ] **Update** All Mappers:
  - `core/data/mapper/PromoCodeMapper.kt` - Remove user field mappings
  - Update preview components to work with new models


---

### ☁️ **Phase 4: Cloud Functions & Production Deployment**

#### **Cloud Functions**
- [ ] **Create** `functions/src/userInteractionHandler.ts`
  ```typescript
  export const handleUserInteraction = functions.firestore
      .document('user_interactions/{interactionId}')
      .onWrite(async (change, context) => {
          // Handle vote count updates on content documents
          // Handle bookmark count updates if needed
          // Update user stats (upvotes received, etc.)
      });
  ```

- [ ] **Update** `firestore.rules`
  ```javascript
  match /user_interactions/{interactionId} {
      allow read, write: if request.auth != null
          && resource.data.userId == request.auth.uid;
  }
  ```

#### **Data Migration**
- [ ] **Create** migration script to convert existing data:
  - Combine existing `/votes` and `/bookmarks` into `/user_interactions`
  - Validate data consistency
  - Handle edge cases and conflicts

- [ ] **Create** rollback strategy:
  - Backup existing collections
  - Rollback scripts if issues arise
  - Data validation checks


---

## ✅ Success Criteria

### **Phase 1 Complete When:**
- [ ] All new models compile and validate correctly
- [ ] Content models no longer have user-specific fields
- [ ] ViewModels integrate cleanly with new models

### **Phase 2 Complete When:**
- [ ] Repository works with Firestore correctly
- [ ] Error handling works for all edge cases
- [ ] Real-time subscriptions work with proper cleanup

### **Phase 3 Complete When:**
- [ ] Detail screens load with exactly 2 Firestore reads
- [ ] All UI components work with new state management
- [ ] No references to old user-specific fields in content models

### **Phase 4 Complete When:**
- [ ] Cloud functions deployed and working in production
- [ ] Data migration completed successfully
- [ ] Performance metrics show 33% read reduction
- [ ] All existing functionality preserved

---

## 🚨 Risk Mitigation

### **Data Loss Prevention**
- Full backup before any migration
- Gradual rollout with feature flags
- Rollback scripts tested and ready

### **Performance Monitoring**
- Monitor Firestore read/write costs
- Track response times before/after
- Alert on any performance degradation

### **User Experience**
- Ensure zero downtime during migration
- Preserve all existing functionality
- Graceful error handling for edge cases

---

## 📚 File Changes Summary

### **New Files Created:**
- `shared/model/UserInteractionModels.kt`
- `shared/model/ViewModels.kt`
- `core/data/model/UserInteractionDtos.kt`
- `core/data/mapper/UserInteractionMapper.kt`
- `core/data/datasource/FirestoreUserInteractionDataSource.kt`
- `core/data/repository/UserInteractionRepositoryImpl.kt`
- `shared/domain/repository/UserInteractionRepository.kt`
- `shared/domain/usecase/interaction/*` (multiple use cases)
- `functions/src/userInteractionHandler.ts`

### **Files Modified:**
- `shared/model/ContentModels.kt` (remove user fields)
- `feature/promocode/detail/PromocodeDetailViewModel.kt`
- `feature/promocode/detail/PromocodeDetailUiState.kt`
- `feature/promocode/detail/component/ActionButtonsSection.kt`
- `core/data/datasource/FirestorePromocodeDataSource.kt`
- `core/data/di/DataModule.kt`
- `firestore.rules`

### **Files Deprecated:**
- Existing vote and bookmark specific data sources (gradual migration)
- Old use cases for separate vote/bookmark operations

---

*This implementation plan ensures a clean, scalable, and cost-effective user interaction system that maintains all existing functionality while dramatically improving performance and maintainability.*