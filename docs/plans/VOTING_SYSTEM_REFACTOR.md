# Voting System Refactoring Plan

## Problem
Voting logic duplicated across PromoCode/Post/Comment. PromocodeDetailViewModel: 700 lines with ~200 lines duplicate upvote/downvote code.

## Solution
Centralize voting in `VotingCoordinator` - handles optimistic updates, network calls, rollback.

**Impact:** PromoCode ViewModel: 700→400 lines. Post/Comment: ~50 lines each (vs 200+ duplicated).

---

## Implementation

### 1. Core Utils (`shared/.../voting/VotingUtils.kt`)

```kotlin
data class VoteDeltas(val upvoteDelta: Int, val downvoteDelta: Int)

fun calculateVoteDeltas(currentVote: VoteState?, targetVote: VoteState, isRemoving: Boolean): VoteDeltas

fun createOptimisticInteraction(
    itemId: String, itemType: ContentType, userId: UserId,
    newVoteState: VoteState, currentInteraction: UserInteraction?
): UserInteraction
```

### 2. Votable Interface (`shared/.../voting/Votable.kt`)

```kotlin
interface Votable {
    val id: String
    val upvotes: Int
    val downvotes: Int
    fun applyVoteDeltas(deltas: VoteDeltas): Votable
}
```

### 3. VotingCoordinator (`core/data/coordinator/VotingCoordinator.kt`)

```kotlin
data class VotingState<T>(val optimisticItem: T, val optimisticInteraction: UserInteraction)

sealed class VotingResult {
    data class Success(val finalInteraction: UserInteraction) : VotingResult()
    data class Error(val error: OperationError) : VotingResult()
}

enum class VoteType { UPVOTE, DOWNVOTE }

@Singleton
class VotingCoordinator @Inject constructor(
    private val toggleVoteUseCase: ToggleVoteUseCase
) {
    suspend fun <T : Votable> handleVote(
        item: T, voteType: VoteType, itemType: ContentType,
        userId: UserId, currentInteraction: UserInteraction?
    ): Pair<VotingState<T>, VotingResult> {
        // Calculate deltas
        val deltas = calculateVoteDeltas(...)

        // Create optimistic state
        val optimistic = VotingState(
            optimisticItem = item.applyVoteDeltas(deltas) as T,
            optimisticInteraction = createOptimisticInteraction(...)
        )

        // Execute vote
        val result = when { ... } // Call toggleVoteUseCase

        return optimistic to result
    }
}
```

### 4. PromoCode Extension (`feature/promocode/extensions/PromocodeVotingExtensions.kt`)

```kotlin
fun PromoCode.asVotable(): Votable = object : Votable {
    override val id = this@asVotable.id.value
    override val upvotes = this@asVotable.upvotes
    override val downvotes = this@asVotable.downvotes

    override fun applyVoteDeltas(deltas: VoteDeltas) = when (this@asVotable) {
        is Discount.Percentage -> this@asVotable.copy(
            upvotes = maxOf(0, upvotes + deltas.upvoteDelta),
            downvotes = maxOf(0, downvotes + deltas.downvoteDelta)
        )
        is Discount.FixedAmount -> this@asVotable.copy(
            upvotes = maxOf(0, upvotes + deltas.upvoteDelta),
            downvotes = maxOf(0, downvotes + deltas.downvoteDelta)
        )
    }.asVotable()
}
```

### 5. ViewModel Usage (`PromocodeDetailViewModel.kt`)

**Before: 140 lines**
```kotlin
private fun handleUpvote() { /* 70 lines */ }
private fun handleDownvote() { /* 70 lines */ }
```

**After: 50 lines**
```kotlin
private fun handleUpvote() = handleVote(VoteType.UPVOTE)
private fun handleDownvote() = handleVote(VoteType.DOWNVOTE)

private fun handleVote(voteType: VoteType) {
    viewModelScope.launch {
        // 1. Check auth
        if (!isAuthenticated) { showAuthSheet(); return@launch }

        // 2. Save original for rollback
        val original = _uiState.value.promoCodeWithUserState

        // 3. Call coordinator
        val (optimistic, result) = votingCoordinator.handleVote(
            item = currentPromoCode.asVotable(),
            voteType = voteType,
            itemType = ContentType.PROMO_CODE,
            userId = user.id,
            currentInteraction = userInteraction
        )

        // 4. Apply optimistic immediately
        _uiState.update { it.copy(
            promoCodeWithUserState = PromoCodeWithUserState(
                optimistic.optimisticItem as PromoCode,
                optimistic.optimisticInteraction
            ),
            isVoting = true
        )}

        // 5. Handle result
        when (result) {
            is Success -> _uiState.update { it.copy(isVoting = false) }
            is Error -> _uiState.update { it.copy(
                promoCodeWithUserState = original, // Rollback
                errorType = result.error
            )}
        }
    }
}
```

### 6. DI (`core/data/di/DataModule.kt`)

```kotlin
@Provides @Singleton
fun provideVotingCoordinator(
    toggleVoteUseCase: ToggleVoteUseCase
): VotingCoordinator = VotingCoordinator(toggleVoteUseCase)
```

---

## Future: Post/Comment

Create `Post.asVotable()` extension (5 lines), copy ViewModel pattern (50 lines). Done.

---

## Checklist

- [ ] Create VotingUtils.kt
- [ ] Create Votable.kt
- [ ] Create VotingCoordinator.kt
- [ ] Add DI binding
- [ ] Create PromocodeVotingExtensions.kt
- [ ] Refactor PromocodeDetailViewModel
- [ ] Test voting flows
- [ ] Remove old code

---

## Benefits

✅ 150+ lines removed from PromocodeDetailViewModel
✅ Zero duplication across features
✅ Post/Comment voting: ~30 lines each
