# Firebase Backend Guide

## Why Firebase for Qode

###  **Perfect for Our Use Case**
- **Kazakhstan market focus**: 100k users, infrequent promo code browsing
- **Simple query patterns**: Category + country + rating filters work great with Firestore
- **Real-time voting**: Essential for community engagement, Firebase excels here
- **Fast MVP development**: Auth + database + real-time out of the box
- **Cost-effective at our scale**: ~$15-20/month for 4M reads (100k users × 20 codes × 2 times/month)

### ❌ **Why Not Supabase**
- **Over-engineered for our needs**: Complex SQL joins not needed for simple promo code filtering
- **Migration risk**: Already have working Firebase implementation
- **Time cost**: Rebuilding delays investor demos and market validation
- **Scale timing**: Better to prove product-market fit first, then optimize backend

## Data Architecture Principles

### **1. Embrace NoSQL Denormalization** 📊
```kotlin
//  Store author data directly in promo codes
data class PromoCode(
    val authorId: UserId,
    val authorName: String,      // Denormalized - faster reads
    val authorCountry: String?,  // Denormalized - avoid user lookup
    val title: String,
    val upvotes: Int,            // Denormalized counter
    val targetCountries: List<String> // Array for filtering
)

// ❌ Don't force relational thinking
data class PromoCode(
    val authorId: UserId,        // Requires separate user fetch = 2x reads
    val voteCollectionId: String // Requires aggregation query = expensive
)
```

### **2. Smart Counter Strategy** 💰
```kotlin
//  Keep essential counters (good UX/cost ratio)
val upvotes: Int = 0,           // Users need to see quality
val downvotes: Int = 0,         // Users need to see problems
val views: Int = 0,             // Shows popularity

// ❌ Skip expensive counters (poor UX/cost ratio)  
// val commentCount: Int        // Load lazily: "Comments" → tap → load count
// val shareCount: Int          // Nobody checks share counts
// val totalEngagement: Int     // Calculate on-demand if needed
```

### **3. Collection Structure** 🗂️
```
/promocodes/{id}                 // Main promo code documents
/promocodes/{id}/comments/{id}   // Comments as subcollections
/users/{id}                      // User profiles
/users/{id}/bookmarks/{itemId}   // User's saved items
/posts/{id}                      // Community posts (family subscriptions)
```

## Essential Queries & Indexes

### **1. Core Promo Code Query** 🎯
```kotlin
// Most common user flow: browse promo codes by category in Kazakhstan
val query = promocodesRef
    .whereEqualTo("category", "Food Delivery")
    .whereArrayContains("targetCountries", "KZ") 
    .whereGreaterThan("upvotes", 5)
    .orderBy("upvotes", Query.Direction.DESCENDING)
    .limit(20)

// Required composite index: [category ASC, targetCountries ASC, upvotes ASC]
```

### **2. Text Search for Posts** 🔍
```kotlin
// Simple prefix search for family subscription posts
val searchQuery = postsRef
    .whereGreaterThanOrEqualTo("title", searchTerm)
    .whereLessThan("title", searchTerm + '\uf8ff')
    .limit(10)

// For complex search later: integrate Algolia (~$5/month at our scale)
```

### **3. User's Activity Feed** 👤
```kotlin
// User's submitted promo codes
val userCodesQuery = promocodesRef
    .whereEqualTo("authorId", userId)
    .orderBy("createdAt", Query.Direction.DESCENDING)

// User's bookmarked items  
val bookmarksQuery = userRef.collection("bookmarks")
    .orderBy("createdAt", Query.Direction.DESCENDING)
```

## Cost Optimization

### **1. Minimize Reads** 💸
```kotlin
//  Batch operations
val promoIds = userBookmarks.map { it.promoCodeId }
val promoCodes = promocodesRef.whereIn("id", promoIds).get()
// Cost: 1 query + N documents

// ❌ Individual fetches  
userBookmarks.forEach { bookmark ->
    promocodesRef.document(bookmark.promoCodeId).get() // N separate queries
}
```

### **2. Use Offline Persistence** 📱
```kotlin
// Free caching - essential for mobile
val settings = firestoreSettings {
    isPersistenceEnabled = true
}
Firebase.firestore.firestoreSettings = settings
```

### **3. Lazy Load Non-Critical Data** ⚡
```kotlin
// Show list immediately with essential data
data class PromoCodeListItem(val title: String, val upvotes: Int, val category: String)

// Load full details when user taps
data class PromoCodeDetails(/* all fields including description, comments, etc */)
```

## Data Consistency Patterns

### **1. Atomic Voting (No Cloud Functions Needed)** ⚛️
```kotlin
suspend fun voteOnPromoCode(promoCodeId: String, userId: String, isUpvote: Boolean) {
    firestore.runTransaction { transaction ->
        val promoRef = promocodesRef.document(promoCodeId)
        val voteRef = promoRef.collection("votes").document(userId)
        
        val existingVote = transaction.get(voteRef).toObjectOrNull<Vote>()
        
        when {
            existingVote == null -> {
                // New vote
                transaction.set(voteRef, Vote(userId, isUpvote))
                val field = if (isUpvote) "upvotes" else "downvotes"
                transaction.update(promoRef, field, FieldValue.increment(1))
            }
            existingVote.isUpvote != isUpvote -> {
                // Change vote direction
                transaction.update(voteRef, "isUpvote", isUpvote)
                transaction.update(promoRef, mapOf(
                    "upvotes" to FieldValue.increment(if (isUpvote) 1 else -1),
                    "downvotes" to FieldValue.increment(if (isUpvote) -1 else 1)
                ))
            }
            // else: same vote = no change (idempotent)
        }
    }
}
```

### **2. Optimistic UI Updates** 🚀
```kotlin
// Show change immediately, sync in background
fun upvotePromoCode(promoCodeId: String) {
    // 1. Update UI optimistically
    _uiState.value = _uiState.value.copy(
        upvotes = _uiState.value.upvotes + 1,
        isUpvoted = true
    )
    
    // 2. Sync to Firebase
    viewModelScope.launch {
        try {
            repository.upvotePromoCode(promoCodeId)
        } catch (e: Exception) {
            // 3. Revert on failure
            _uiState.value = _uiState.value.copy(
                upvotes = _uiState.value.upvotes - 1,
                isUpvoted = false
            )
            // Show error toast
        }
    }
}
```

## Security Rules

### **1. Basic Access Control** 🔒
```javascript
// Promo codes: public read, auth write, author edit
match /promocodes/{promoId} {
  allow read: if true;
  allow create: if request.auth != null;
  allow update: if request.auth != null && 
                request.auth.uid == resource.data.authorId;
}

// Votes: users can only vote as themselves
match /promocodes/{promoId}/votes/{userId} {
  allow write: if request.auth != null && 
               request.auth.uid == userId;
}
```

### **2. Data Validation** 
```javascript
// Prevent malicious data
function validPromoCode(data) {
  return data.keys().hasAll(['title', 'code', 'category']) &&
         data.title is string && data.title.size() <= 100 &&
         data.upvotes >= 0 && data.downvotes >= 0 &&
         data.targetCountries is list;
}
```

## Development Guidelines

### **1. New Feature Checklist** 
- [ ] Will this query scale to 50k+ documents?
- [ ] Do I need this counter for UX or can I calculate on-demand?
- [ ] Have I created the necessary composite index?
- [ ] Does the security rule properly restrict access?
- [ ] Am I denormalizing data to avoid multiple reads?

### **2. Performance Monitoring** 📊
```kotlin
// Monitor query performance in debug builds
if (BuildConfig.DEBUG) {
    val startTime = System.currentTimeMillis()
    val results = query.get().await()
    val queryTime = System.currentTimeMillis() - startTime
    Log.d("Firebase", "Query: ${queryTime}ms, ${results.size()} docs")
}
```

## Migration Strategy

### **When to Consider Switching** ⚠️
- Daily read costs exceed $100 (means you're successful!)
- Need complex analytics queries (user behavior analysis)
- Require advanced search (full-text across multiple fields)
- Hit Firestore's concurrent write limits (400/sec per collection)

### **How to Prepare for Migration** 🔄
```kotlin
// Use repository pattern to abstract database
interface PromoCodeRepository {
    suspend fun getPromoCodes(filters: PromoCodeFilters): List<PromoCode>
    suspend fun submitPromoCode(promoCode: PromoCode): Result<Unit>
}

// Current: Firebase implementation
class FirebasePromoCodeRepository : PromoCodeRepository

// Future: Supabase/PostgreSQL implementation  
class SupabasePromoCodeRepository : PromoCodeRepository

// ViewModels stay unchanged during migration
```

## Common Pitfalls

### ❌ **Avoid These Mistakes**
- **Over-normalizing**: Don't be afraid to duplicate author names across documents
- **Missing indexes**: Always create composite indexes for multi-field queries  
- **Ignoring offline**: Handle network failures gracefully with cached data
- **Counting everything**: Only maintain counters users actually see
- **Complex transactions**: Keep transactions simple and fast

###  **Follow These Patterns**
- **Think documents, not tables**: Embrace NoSQL data duplication
- **Optimize for reads**: 90% of operations are browsing promo codes
- **Monitor costs early**: Set billing alerts at $10, $50, $100
- **Test with real data**: Don't optimize empty collections

## Real-World Cost Examples

### **Our Projected Usage** 📈
```
100k users in Kazakhstan
Each user browses 20 promo codes, 2x per month
Total: 4M document reads/month
Firebase cost: ~$15-20/month
Supabase cost: $25/month flat

Break-even point: ~6M reads/month
"Success problem": If we exceed this, we can afford to migrate!
```

## Conclusion

Firebase fits our promo code app perfectly because:
- **Simple data model** (users, promo codes, votes, comments)
- **Predictable query patterns** (category, country, rating filters) 
- **Community engagement** (real-time voting works great)
- **Kazakhstan market scale** (manageable costs at 100k users)

Focus on shipping fast, validating the market, and getting users. Backend optimization is a good problem to have later.
