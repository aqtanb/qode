# Data Layer Architecture

## Overview
Comprehensive data layer following clean architecture principles with Firebase integration, sophisticated caching strategies, and domain-driven design. Features repository pattern, DTO mapping, real-time data synchronization, and robust error handling following NIA (Now in Android) patterns.

## Architecture
- **Repository Pattern**: Clean separation between domain interfaces and data implementations
- **Firebase Integration**: Firestore for data persistence with real-time updates
- **DTO Mapping**: Clean conversion between domain models and data transfer objects
- **Error Handling**: Standard exception patterns with comprehensive error categorization
- **Dependency Injection**: Dagger Hilt modules for clean dependency management
- **Real-Time Sync**: LiveData and Flow-based real-time data synchronization

## Key Files

### Repository Implementations
- `core/data/repository/AuthRepositoryImpl.kt` - Authentication data operations with Firebase Auth
- `core/data/repository/PromoCodeRepositoryImpl.kt` - PromoCode CRUD operations with Firestore

### Data Sources
- `core/data/datasource/GoogleAuthService.kt` - Google Sign-In with Credential Manager API
- `core/data/datasource/FirestorePromoCodeDataSource.kt` - Firestore operations with complex queries

### Data Mapping
- `core/data/mapper/PromoCodeMapper.kt` - Bidirectional mapping between domain and DTO models
- `core/data/model/PromoCodeDto.kt` - Firestore document structure definitions

### Domain Interfaces
- `core/domain/repository/AuthRepository.kt` - Authentication repository contract
- `core/domain/repository/PromoCodeRepository.kt` - PromoCode repository contract

### Dependency Injection
- `core/data/di/DataModule.kt` - Hilt modules for data layer dependencies

## Repository Pattern Implementation

### Authentication Repository
```kotlin
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val googleAuthService: GoogleAuthService
) : AuthRepository {
    
    // Direct Flow delegation - no unnecessary wrappers
    override fun signInWithGoogle(): Flow<User> = googleAuthService.signIn()
    
    override fun signOut(): Flow<Unit> = googleAuthService.signOut()
    
    override fun getAuthStateFlow(): Flow<User?> =
        callbackFlow {
            val auth = Firebase.auth
            
            val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                val firebaseUser = firebaseAuth.currentUser
                val user = firebaseUser?.let { googleAuthService.createUserFromFirebaseUser(it) }
                trySend(user)
            }
            
            auth.addAuthStateListener(authStateListener)
            
            awaitClose {
                auth.removeAuthStateListener(authStateListener)
            }
        }
    
    override fun isSignedIn(): Boolean = googleAuthService.isSignedIn()
}
```

### PromoCode Repository
```kotlin
@Singleton
class PromoCodeRepositoryImpl @Inject constructor(
    private val dataSource: FirestorePromoCodeDataSource
) : PromoCodeRepository {
    
    override fun createPromoCode(promoCode: PromoCode): Flow<PromoCode> =
        flow {
            emit(dataSource.createPromoCode(promoCode))
        }
    
    override fun getPromoCodes(
        query: String?,
        sortBy: ContentSortBy,
        filterByType: String?,
        filterByService: String?,
        filterByCategory: String?,
        isFirstUserOnly: Boolean?,
        limit: Int,
        offset: Int
    ): Flow<List<PromoCode>> =
        flow {
            emit(
                dataSource.getPromoCodes(
                    query = query,
                    sortBy = sortBy,
                    filterByType = filterByType,
                    filterByService = filterByService,
                    filterByCategory = filterByCategory,
                    isFirstUserOnly = isFirstUserOnly,
                    limit = limit,
                    offset = offset,
                )
            )
        }
    
    // Real-time data observation
    override fun observePromoCodes(ids: List<PromoCodeId>): Flow<List<PromoCode>> = 
        dataSource.observePromoCodes(ids)
}
```

### Repository Features
- **Flow-Based**: All operations return Flow for reactive programming
- **Direct Delegation**: Minimal wrapping for clean data flow
- **Real-Time Updates**: Live data synchronization with Firestore
- **Error Propagation**: Standard exceptions propagated to domain layer

## Firebase Integration

### Google Authentication Service
```kotlin
@Singleton
class GoogleAuthService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val auth = Firebase.auth
    private val credentialManager = CredentialManager.create(context)
    
    fun signIn(): Flow<User> =
        flow {
            val option = GetGoogleIdOption.Builder()
                .setServerClientId(serverClientId)
                .setFilterByAuthorizedAccounts(false)
                .build()
            
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(option)
                .build()
            
            val response = credentialManager.getCredential(context, request)
            val cred = response.credential
            
            if (cred is CustomCredential && cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(cred.data).idToken
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                val firebaseUser = authResult.user
                
                val user = firebaseUser?.let { user ->
                    createUserFromFirebaseUser(user)
                } ?: throw IllegalStateException("Null user after authentication")
                
                emit(user)
            } else {
                throw IllegalArgumentException("Invalid credentials")
            }
        }
    
    fun signOut(): Flow<Unit> =
        flow {
            val clearRequest = ClearCredentialStateRequest()
            credentialManager.clearCredentialState(clearRequest)
            auth.signOut()
            emit(Unit)
        }
}
```

### Firestore Integration
```kotlin
@Singleton
class FirestorePromoCodeDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val PROMOCODES_COLLECTION = "promocodes"
        private const val VOTES_COLLECTION = "votes"
        private const val USAGE_COLLECTION = "usage"
    }
    
    suspend fun createPromoCode(promoCode: PromoCode): PromoCode {
        val dto = PromoCodeMapper.toDto(promoCode)
        
        try {
            firestore.collection(PROMOCODES_COLLECTION)
                .document(dto.id)
                .set(dto)
                .await()
        } catch (e: Exception) {
            throw IllegalStateException("PromoCode with code '${promoCode.code}' already exists for service '${promoCode.serviceName}'", e)
        }
        
        return promoCode
    }
    
    suspend fun getPromoCodes(
        query: String?,
        sortBy: ContentSortBy,
        filterByType: String?,
        filterByService: String?,
        filterByCategory: String?,
        isFirstUserOnly: Boolean?,
        limit: Int,
        offset: Int
    ): List<PromoCode> {
        var firestoreQuery: Query = firestore.collection(PROMOCODES_COLLECTION)
        
        // Apply filters
        query?.let { searchQuery ->
            if (searchQuery.isNotBlank()) {
                firestoreQuery = firestoreQuery
                    .whereGreaterThanOrEqualTo("code", searchQuery.uppercase())
                    .whereLessThanOrEqualTo("code", searchQuery.uppercase() + "\uf8ff")
            }
        }
        
        filterByType?.let { type ->
            firestoreQuery = firestoreQuery.whereEqualTo("type", type)
        }
        
        filterByService?.let { service ->
            firestoreQuery = firestoreQuery.whereEqualTo("serviceName", service)
        }
        
        // Apply sorting
        firestoreQuery = when (sortBy) {
            ContentSortBy.POPULARITY -> firestoreQuery.orderBy("upvotes", Query.Direction.DESCENDING)
            ContentSortBy.NEWEST -> firestoreQuery.orderBy("createdAt", Query.Direction.DESCENDING)
            ContentSortBy.EXPIRING_SOON -> firestoreQuery.orderBy("endDate", Query.Direction.ASCENDING)
            // ... other sorting options
        }
        
        firestoreQuery = firestoreQuery.limit(limit.toLong())
        
        val querySnapshot = firestoreQuery.get().await()
        
        return querySnapshot.documents.mapNotNull { document ->
            try {
                document.toObject<PromoCodeDto>()?.let { dto ->
                    PromoCodeMapper.toDomain(dto)
                }
            } catch (e: Exception) {
                null // Skip malformed documents
            }
        }
    }
}
```

### Firebase Features
- **Modern APIs**: Credential Manager API for Google Sign-In
- **Real-Time Listeners**: CallbackFlow for live data updates
- **Complex Queries**: Multi-field filtering and sorting
- **Atomic Operations**: Batch writes for data consistency
- **Error Resilience**: Graceful handling of malformed documents

## Data Mapping System

### PromoCode Mapper
```kotlin
object PromoCodeMapper {
    
    fun toDomain(dto: PromoCodeDto): PromoCode {
        val promoCodeId = PromoCodeId(dto.id)
        val createdBy = dto.createdBy?.let { UserId(it) }
        
        return when (dto.type) {
            "percentage" -> PromoCode.PercentagePromoCode(
                id = promoCodeId,
                code = dto.code,
                serviceName = dto.serviceName,
                category = dto.category,
                title = dto.title,
                description = dto.description,
                discountPercentage = dto.discountPercentage
                    ?: throw IllegalArgumentException("Percentage promo code missing discountPercentage"),
                minimumOrderAmount = dto.minimumOrderAmount,
                startDate = dto.startDate.toInstant(),
                endDate = dto.endDate.toInstant(),
                isFirstUserOnly = dto.isFirstUserOnly,
                upvotes = dto.upvotes,
                downvotes = dto.downvotes,
                views = dto.views,
                screenshotUrl = dto.screenshotUrl,
                comments = dto.comments,
                createdAt = dto.createdAt?.toInstant() ?: Clock.System.now(),
                createdBy = createdBy,
            )
            
            "fixed" -> PromoCode.FixedAmountPromoCode(
                // ... similar mapping for fixed amount
            )
            
            else -> throw IllegalArgumentException("Unknown promo code type: ${dto.type}")
        }
    }
    
    fun toDto(domain: PromoCode): PromoCodeDto =
        when (domain) {
            is PromoCode.PercentagePromoCode -> PromoCodeDto(
                id = domain.id.value,
                code = domain.code,
                serviceName = domain.serviceName,
                category = domain.category,
                title = domain.title,
                description = domain.description,
                type = "percentage",
                discountPercentage = domain.discountPercentage,
                minimumOrderAmount = domain.minimumOrderAmount,
                isFirstUserOnly = domain.isFirstUserOnly,
                upvotes = domain.upvotes,
                downvotes = domain.downvotes,
                views = domain.views,
                screenshotUrl = domain.screenshotUrl,
                comments = domain.comments,
                startDate = domain.startDate.let { Timestamp(it.epochSecond, it.nano) },
                endDate = domain.endDate.let { Timestamp(it.epochSecond, it.nano) },
                createdAt = Timestamp(domain.createdAt.epochSecond, domain.createdAt.nano),
                createdBy = domain.createdBy?.value,
            )
            
            is PromoCode.FixedAmountPromoCode -> PromoCodeDto(
                // ... similar mapping for fixed amount
            )
        }
}
```

### Mapping Features
- **Bidirectional Conversion**: Domain ↔ DTO conversion
- **Type Safety**: Sealed class handling with validation
- **Timestamp Conversion**: Firestore Timestamp ↔ Java Instant
- **Error Handling**: Validation during mapping with clear error messages
- **Null Safety**: Proper handling of optional fields

## Complex Query Operations

### Advanced Filtering
```kotlin
suspend fun getPromoCodes(
    query: String?,
    sortBy: ContentSortBy,
    filterByType: String?,
    filterByService: String?,
    filterByCategory: String?,
    isFirstUserOnly: Boolean?,
    limit: Int,
    offset: Int
): List<PromoCode> {
    var firestoreQuery: Query = firestore.collection(PROMOCODES_COLLECTION)
    
    // Text search with prefix matching
    query?.let { searchQuery ->
        if (searchQuery.isNotBlank()) {
            firestoreQuery = firestoreQuery
                .whereGreaterThanOrEqualTo("code", searchQuery.uppercase())
                .whereLessThanOrEqualTo("code", searchQuery.uppercase() + "\uf8ff")
        }
    }
    
    // Multiple filter application
    filterByType?.let { type ->
        firestoreQuery = firestoreQuery.whereEqualTo("type", type)
    }
    
    filterByService?.let { service ->
        firestoreQuery = firestoreQuery.whereEqualTo("serviceName", service)
    }
    
    filterByCategory?.let { category ->
        firestoreQuery = firestoreQuery.whereEqualTo("category", category)
    }
    
    isFirstUserOnly?.let { firstUserOnly ->
        firestoreQuery = firestoreQuery.whereEqualTo("isFirstUserOnly", firstUserOnly)
    }
    
    // Dynamic sorting
    firestoreQuery = when (sortBy) {
        ContentSortBy.POPULARITY -> firestoreQuery.orderBy("upvotes", Query.Direction.DESCENDING)
        ContentSortBy.NEWEST -> firestoreQuery.orderBy("createdAt", Query.Direction.DESCENDING)
        ContentSortBy.OLDEST -> firestoreQuery.orderBy("createdAt", Query.Direction.ASCENDING)
        ContentSortBy.EXPIRING_SOON -> firestoreQuery.orderBy("endDate", Query.Direction.ASCENDING)
        ContentSortBy.MOST_VIEWED -> firestoreQuery.orderBy("views", Query.Direction.DESCENDING)
        ContentSortBy.MOST_USED -> firestoreQuery.orderBy("createdAt", Query.Direction.DESCENDING)
        ContentSortBy.ALPHABETICAL -> firestoreQuery.orderBy("code", Query.Direction.ASCENDING)
    }
    
    // Pagination
    firestoreQuery = firestoreQuery.limit(limit.toLong())
    
    return executeQuerySafely(firestoreQuery)
}
```

### Voting System Operations
```kotlin
suspend fun voteOnPromoCode(
    promoCodeId: PromoCodeId,
    userId: UserId,
    isUpvote: Boolean
): PromoCodeVote {
    val voteDto = PromoCodeVoteDto(
        id = "${promoCodeId.value}_${userId.value}",
        promoCodeId = promoCodeId.value,
        userId = userId.value,
        isUpvote = isUpvote,
    )
    
    // Atomic batch operation
    val batch = firestore.batch()
    
    // Add/update vote
    val voteRef = firestore.collection(VOTES_COLLECTION).document(voteDto.id)
    batch.set(voteRef, voteDto)
    
    // Update vote counts on promocode
    val promoCodeRef = firestore.collection(PROMOCODES_COLLECTION).document(promoCodeId.value)
    if (isUpvote) {
        batch.update(promoCodeRef, "upvotes", FieldValue.increment(1))
    } else {
        batch.update(promoCodeRef, "downvotes", FieldValue.increment(1))
    }
    
    batch.commit().await()
    
    return PromoCodeMapper.voteToDomain(voteDto)
}
```

## Real-Time Data Synchronization

### Live Data Updates
```kotlin
fun observePromoCodes(ids: List<PromoCodeId>): Flow<List<PromoCode>> =
    callbackFlow {
        if (ids.isEmpty()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        
        val listener = firestore.collection(PROMOCODES_COLLECTION)
            .whereIn("__name__", ids.map { it.value })
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val promoCodes = snapshot?.documents?.mapNotNull { document ->
                    try {
                        document.toObject<PromoCodeDto>()?.let { dto ->
                            PromoCodeMapper.toDomain(dto)
                        }
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                trySend(promoCodes)
            }
        
        awaitClose {
            listener.remove()
        }
    }
```

### Authentication State Monitoring
```kotlin
override fun getAuthStateFlow(): Flow<User?> =
    callbackFlow {
        val auth = Firebase.auth
        
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            val user = firebaseUser?.let { googleAuthService.createUserFromFirebaseUser(it) }
            trySend(user)
        }
        
        auth.addAuthStateListener(authStateListener)
        
        awaitClose {
            auth.removeAuthStateListener(authStateListener)
        }
    }
```

### Real-Time Features
- **Live Updates**: Automatic UI updates when data changes
- **CallbackFlow**: Proper Flow-based Firebase listener integration
- **Error Handling**: Graceful listener error propagation
- **Resource Cleanup**: Automatic listener removal on Flow cancellation

## Error Handling Strategy

### Standard Exception Patterns
Following NIA (Now in Android) patterns, the data layer uses standard exceptions:

```kotlin
/**
 * Repository interface for authentication operations.
 * 
 * All methods may throw standard exceptions which will be handled by use cases
 * or ViewModels using the .asResult() extension.
 * 
 * Follows NIA pattern - uses standard exceptions, no custom exception hierarchies.
 */
interface AuthRepository {
    
    /**
     * Sign in with Google authentication.
     * 
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when Google Play Services unavailable
     * @throws SecurityException when authentication is rejected or cancelled
     * @throws RuntimeException for unexpected authentication errors
     */
    fun signInWithGoogle(): Flow<User>
    
    /**
     * Sign out the current user.
     * 
     * @throws java.io.IOException when network request fails
     * @throws IllegalStateException when no user is signed in
     */
    fun signOut(): Flow<Unit>
    
    /**
     * Observe authentication state changes.
     * 
     * This method doesn't throw exceptions - authentication state changes
     * are delivered as Flow emissions.
     */
    fun getAuthStateFlow(): Flow<User?>
    
    /**
     * Check if user is currently signed in.
     * 
     * Note: This is synchronous and doesn't throw exceptions
     */
    fun isSignedIn(): Boolean
}
```

### Exception Categories
- **IOException**: Network connectivity issues
- **IllegalStateException**: Invalid application state (e.g., missing Google Play Services)
- **SecurityException**: Authentication rejected or cancelled by user
- **RuntimeException**: Unexpected errors and general failures
- **IllegalArgumentException**: Invalid input parameters

### Error Recovery Patterns
```kotlin
// Data source error handling
suspend fun createPromoCode(promoCode: PromoCode): PromoCode {
    val dto = PromoCodeMapper.toDto(promoCode)
    
    try {
        firestore.collection(PROMOCODES_COLLECTION)
            .document(dto.id)
            .set(dto)
            .await()
    } catch (e: Exception) {
        // Convert Firebase exceptions to standard exceptions
        throw IllegalStateException("PromoCode with code '${promoCode.code}' already exists for service '${promoCode.serviceName}'", e)
    }
    
    return promoCode
}

// Query error handling with graceful degradation
return querySnapshot.documents.mapNotNull { document ->
    try {
        document.toObject<PromoCodeDto>()?.let { dto ->
            PromoCodeMapper.toDomain(dto)
        }
    } catch (e: Exception) {
        // Log error and skip malformed documents
        null
    }
}
```

## Caching Strategy

### Repository-Level Caching
- **Flow Caching**: StateFlow and SharedFlow for in-memory caching
- **Real-Time Updates**: Live data synchronization reduces cache invalidation needs
- **Offline Support**: Firebase offline persistence for automatic caching

### Data Freshness
- **Pull-to-Refresh**: Manual data refresh triggers
- **Automatic Refresh**: Time-based or event-based cache invalidation
- **Optimistic Updates**: Immediate UI updates with server synchronization

## Performance Optimizations

### Query Optimization
- **Composite Indexes**: Firestore indexes for complex queries
- **Pagination**: Cursor-based pagination for large datasets
- **Field Selection**: Minimize data transfer with selective queries
- **Batch Operations**: Atomic writes for consistency and performance

### Memory Management
- **Flow Cleanup**: Proper Flow cancellation and resource cleanup
- **Listener Management**: Automatic Firebase listener removal
- **Object Pooling**: Efficient DTO object reuse where applicable

## Data Migration

### Schema Evolution
- **DTO Versioning**: Handle schema changes with backward compatibility
- **Migration Scripts**: Firestore data migration strategies
- **Graceful Degradation**: Handle missing or changed fields

### Model Updates
```kotlin
fun toDomain(dto: PromoCodeDto): PromoCode {
    // Handle optional fields that might not exist in older documents
    val createdAt = dto.createdAt?.toInstant() ?: Clock.System.now()
    val createdBy = dto.createdBy?.let { UserId(it) }
    
    // Provide defaults for new fields
    val views = dto.views ?: 0
    val comments = dto.comments ?: emptyList()
    
    return when (dto.type) {
        "percentage" -> PromoCode.PercentagePromoCode(
            // ... mapping with defaults
        )
        else -> throw IllegalArgumentException("Unknown promo code type: ${dto.type}")
    }
}
```

## Testing Strategy

### Repository Testing
```kotlin
@Test
fun authRepository_whenSignInSuccessful_returnsUser() = runTest {
    // Given
    val mockGoogleAuthService = mockk<GoogleAuthService>()
    val repository = AuthRepositoryImpl(mockGoogleAuthService)
    
    every { mockGoogleAuthService.signIn() } returns flowOf(testUser)
    
    // When
    val result = repository.signInWithGoogle().first()
    
    // Then
    assertEquals(testUser, result)
}
```

### Data Source Testing
```kotlin
@Test
fun firestoreDataSource_whenCreatePromoCode_savesToFirestore() = runTest {
    // Test Firestore operations with test database
}
```

### Mapper Testing
```kotlin
@Test
fun promoCodeMapper_toDomain_convertsCorrectly() {
    // Given
    val dto = PromoCodeDto(/* test data */)
    
    // When
    val domain = PromoCodeMapper.toDomain(dto)
    
    // Then
    assertEquals(dto.id, domain.id.value)
    assertEquals(dto.code, domain.code)
    // ... other assertions
}
```

## Development Guidelines

### Repository Implementation
1. **Delegate to Data Sources**: Repositories should delegate to data sources
2. **Minimal Wrapping**: Avoid unnecessary Flow wrapping
3. **Standard Exceptions**: Use standard exception types
4. **Interface Compliance**: Implement all repository interface methods

### Data Source Design
1. **Single Responsibility**: Each data source handles one type of data
2. **Suspend Functions**: Use suspend functions for async operations
3. **Error Propagation**: Let exceptions bubble up to repositories
4. **Resource Cleanup**: Proper cleanup of Firebase listeners

### Mapping Strategy
1. **Bidirectional**: Support both domain → DTO and DTO → domain
2. **Validation**: Validate data during mapping
3. **Type Safety**: Handle sealed classes and nullable fields properly
4. **Performance**: Efficient mapping without unnecessary allocations

### Firebase Best Practices
1. **Composite Keys**: Use composite document IDs for uniqueness
2. **Batch Operations**: Use batch writes for atomic updates
3. **Index Planning**: Design queries with Firestore index limitations in mind
4. **Offline Support**: Enable Firebase offline persistence

This data layer provides a robust, scalable foundation for the application with modern Android development patterns, Firebase integration, and enterprise-level architecture practices.
