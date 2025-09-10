# PromoCode Management System

## Overview
Comprehensive promo code management system with type-safe sealed classes, Firebase integration, and community voting features. Supports percentage and fixed-amount discounts with sophisticated validation and factory methods.

## Architecture
- **Sealed Class Hierarchy**: Type-safe PromoCode model with PercentagePromoCode and FixedAmountPromoCode
- **Repository Pattern**: Clean architecture with domain interfaces and data implementations
- **Community Features**: Voting system, comments, view tracking, and user-generated content
- **Validation**: Comprehensive input validation with Result patterns for safe creation
- **Firebase Integration**: Firestore for persistence with real-time updates

## Key Files

### Core Model Layer
- `core/model/PromoCode.kt` - Sealed class hierarchy with factory methods and validation
- `core/model/User.kt` - User model with PromoCode relationships

### Domain Layer
- `core/domain/repository/PromoCodeRepository.kt` - Repository interface with comprehensive operations
- `core/domain/usecase/promocode/CreatePromoCodeUseCase.kt` - Business logic for creating promo codes
- `core/domain/usecase/promocode/GetPromoCodesUseCase.kt` - Retrieval with filtering and sorting
- `core/domain/usecase/promocode/SearchPromoCodesUseCase.kt` - Full-text search capabilities
- `core/domain/usecase/promocode/VoteOnPromoCodeUseCase.kt` - Community voting logic
- `core/domain/usecase/promocode/ValidatePromoCodeUseCase.kt` - Validation business rules
- `core/domain/usecase/promocode/GetUserVoteUseCase.kt` - User vote retrieval
- `core/domain/usecase/promocode/IncrementViewCountUseCase.kt` - Analytics tracking
- `core/domain/usecase/promocode/AddCommentUseCase.kt` - Community interaction

### Data Layer
- `core/data/repository/PromocodeRepositoryImpl.kt` - Repository implementation with Firestore
- `core/data/datasource/FirestorePromocodeDataSource.kt` - Firebase data access layer
- `core/data/mapper/PromoCodeMapper.kt` - DTO to domain model mapping
- `core/data/model/PromoCodeDto.kt` - Data transfer objects for Firebase

### UI Components
- `core/ui/component/CouponPromoCodeCard.kt` - Coupon-style promo code card with perforated design
- `core/ui/component/EnhancedPromoCodeCard.kt` - Advanced card with voting and community features
- `core/ui/component/PromoCodeCard.kt` - Basic promo code display component
- `core/ui/component/PromoCodeList.kt` - List component with pagination and filtering

### Feature Integration
- `feature/home/HomeEvent.kt` - PromoCode-related events (detail view, copy actions)
- `feature/promocode/submission/` - Multi-step submission wizard for creating promo codes

## PromoCode Model Architecture

### Sealed Class Hierarchy
```kotlin
sealed class PromoCode {
    abstract val id: PromoCodeId
    abstract val code: String
    abstract val serviceName: String
    abstract val category: String?
    abstract val title: String
    abstract val description: String?
    abstract val startDate: Instant
    abstract val endDate: Instant
    abstract val isFirstUserOnly: Boolean
    abstract val upvotes: Int
    abstract val downvotes: Int
    abstract val views: Int
    abstract val screenshotUrl: String?
    abstract val comments: List<String>?
    abstract val createdAt: Instant
    abstract val createdBy: UserId?

    // Computed properties
    val isExpired: Boolean
    val isValidNow: Boolean
    val totalVotes: Int
    val voteScore: Int
    val popularityScore: Double

    abstract fun calculateDiscount(orderAmount: Double): Double
}
```

### PromoCode Types
```kotlin
// Percentage-based discount
data class PercentagePromoCode(
    val discountPercentage: Double,
    val minimumOrderAmount: Double,
    // ... other properties
) : PromoCode()

// Fixed amount discount  
data class FixedAmountPromoCode(
    val discountAmount: Double,
    val minimumOrderAmount: Double,
    // ... other properties
) : PromoCode()
```

### Factory Methods with Validation
```kotlin
companion object {
    fun createPercentage(
        code: String,
        serviceName: String,
        discountPercentage: Double,
        // ... other parameters
    ): Result<PercentagePromoCode>

    fun createFixedAmount(
        code: String,
        serviceName: String,
        discountAmount: Double,
        // ... other parameters
    ): Result<FixedAmountPromoCode>

    fun generateCompositeId(code: String, serviceName: String): String
}
```

## Repository Operations

### CRUD Operations
```kotlin
interface PromoCodeRepository {
    fun createPromoCode(promoCode: PromoCode): Flow<PromoCode>
    fun getPromoCodes(
        query: String? = null,
        sortBy: ContentSortBy = ContentSortBy.POPULARITY,
        filterByType: String? = null,
        filterByService: String? = null,
        filterByCategory: String? = null,
        isFirstUserOnly: Boolean? = null,
        limit: Int = 20,
        offset: Int = 0
    ): Flow<List<PromoCode>>
    fun getPromoCodeById(id: PromoCodeId): Flow<PromoCode?>
    fun updatePromoCode(promoCode: PromoCode): Flow<PromoCode>
    fun deletePromoCode(id: PromoCodeId): Flow<Unit>
}
```

### Community Features
```kotlin
// Voting system
fun voteOnPromoCode(
    promoCodeId: PromoCodeId,
    userId: UserId,
    isUpvote: Boolean
): Flow<PromoCodeVote>

// Analytics
fun incrementViewCount(id: PromoCodeId): Flow<Unit>

// Comments
fun addComment(
    promoCodeId: PromoCodeId,
    userId: UserId,
    comment: String
): Flow<PromoCode>
```

### Advanced Queries
```kotlin
// User-specific queries
fun getPromoCodesByUser(userId: UserId): Flow<List<PromoCode>>

// Service-specific queries  
fun getPromoCodesByService(serviceName: String): Flow<List<PromoCode>>

// Real-time updates
fun observePromoCodes(ids: List<PromoCodeId>): Flow<List<PromoCode>>
```

## Sorting and Filtering

### Sort Options
```kotlin
enum class ContentSortBy {
    POPULARITY,     // Sort by vote score
    NEWEST,         // Creation date (newest first)
    OLDEST,         // Creation date (oldest first)
    EXPIRING_SOON,  // End date (expiring first)
    MOST_VIEWED,    // View count
    MOST_USED,      // Usage count
    ALPHABETICAL    // Code alphabetically
}
```

### Filter Capabilities
- **By Type**: Percentage vs Fixed Amount
- **By Service**: Specific service/company
- **By Category**: Food, Shopping, Travel, etc.
- **By User Restriction**: First-user-only codes
- **By Status**: Active, expired, not started
- **Full-text Search**: Code, title, description, service name

## Validation System

### Input Validation
```kotlin
// PromoCode creation with validation
init {
    require(code.isNotBlank()) { "PromoCode code cannot be blank" }
    require(serviceName.isNotBlank()) { "Service name cannot be blank" }
    require(discountPercentage > 0 && discountPercentage <= 100) 
    require(minimumOrderAmount?.let { it > 0 } ?: true)
    require(endDate.isAfter(startDate)) { "End date must be after start date" }
    require(upvotes >= 0) { "Upvotes cannot be negative" }
    require(downvotes >= 0) { "Downvotes cannot be negative" }
    require(views >= 0) { "Views cannot be negative" }
}
```

### Business Rule Validation
- Date range validation (start < end)
- Discount percentage limits (0-100%)
- Minimum order amount constraints
- Code uniqueness per service
- User permission checks

## UI Components

### CouponPromoCodeCard Features
- **Coupon Design**: Realistic coupon appearance with perforated divider
- **Visual Hierarchy**: Clear discount display, service branding, promo code prominence
- **Interactive Elements**: Copy code button, card press animations
- **Color Coding**: Different colors for percentage vs fixed amount discounts
- **Community Data**: Rating display, creation date, view metrics

### Component Structure
```kotlin
@Composable
fun CouponPromoCodeCard(
    promoCode: PromoCode,
    onCardClick: () -> Unit,
    onCopyCodeClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

### Design Features
- **Custom Shape**: CouponShape with actual cutouts at perforation
- **Transparent Top Bars**: Seamless gradient integration
- **Design Tokens**: Consistent spacing, colors, animations using Tokens.kt
- **Material 3**: Modern design system implementation
- **Responsive Layout**: Adapts to different screen sizes

## Community Features

### Voting System
```kotlin
@Serializable
data class PromoCodeVote(
    val id: String,
    val promoCodeId: PromoCodeId,
    val userId: UserId,
    val isUpvote: Boolean,
    val votedAt: Instant
)
```

### Analytics Tracking
- **View Count**: Track promo code views for popularity metrics
- **Usage Analytics**: Monitor code copy actions and redemptions
- **User Engagement**: Vote patterns and comment activity
- **Service Popularity**: Track which services have most popular codes

### Community Moderation
- **User-Generated Content**: Community-submitted promo codes
- **Voting-Based Quality**: Community voting determines code quality
- **Comment System**: User feedback and experiences
- **Reputation System**: User contribution tracking

## Firebase Integration

### Firestore Schema
```kotlin
// Collection: promoCodes
{
    id: "SAVE20_UBER",
    code: "SAVE20", 
    serviceName: "Uber",
    type: "percentage", // or "fixedAmount"
    discountPercentage: 20.0, // for percentage type
    discountAmount: 100.0,    // for fixed amount type
    minimumOrderAmount: 500.0,
    category: "Transportation",
    title: "20% Off Uber Rides",
    description: "Save on your next ride",
    startDate: "2024-01-01T00:00:00Z",
    endDate: "2024-12-31T23:59:59Z",
    isFirstUserOnly: false,
    upvotes: 45,
    downvotes: 3,
    views: 1250,
    screenshotUrl: "https://...",
    comments: ["Works great!", "Expired for me"],
    createdAt: "2024-01-15T10:30:00Z",
    createdBy: "user123"
}
```

### Real-Time Updates
- **Live Vote Counts**: Real-time voting updates across all clients
- **Community Activity**: Live comment additions and interactions
- **Expiration Tracking**: Automatic status updates for expired codes
- **Service Monitoring**: Real-time service availability updates

## Error Handling

### Repository Error Patterns
Following NIA (Now in Android) patterns:
```kotlin
// Repository throws standard exceptions
fun createPromoCode(promoCode: PromoCode): Flow<PromoCode>
// Throws: IOException, IllegalStateException, IllegalArgumentException

// Use cases apply .asResult() extension
fun execute(promoCode: PromoCode): Flow<Result<PromoCode>> = 
    repository.createPromoCode(promoCode).asResult()
```

### Exception Types
- **IOException**: Network/connectivity issues
- **IllegalStateException**: Firestore unavailable
- **IllegalArgumentException**: Validation failures
- **SecurityException**: Permission/authorization errors

## Testing Strategy

### Unit Tests
- **Model Validation**: PromoCode factory method testing
- **Business Logic**: Use case behavior with MockK
- **Repository**: Data transformation and error handling
- **Utilities**: Discount calculation and validation logic

### Integration Tests
- **Firebase Integration**: Firestore operations with test data
- **UI Components**: Compose testing with preview data
- **End-to-End**: Complete promo code lifecycle testing

## Development Guidelines

1. **Use Factory Methods**: Always create PromoCode instances via companion object methods
2. **Handle Results**: Use Result patterns for validation failures
3. **Follow MVI**: Events for navigation, Actions for user interactions
4. **Community First**: Design with community features in mind
5. **Validate Early**: Client-side validation with server-side verification
6. **Cache Wisely**: Balance real-time updates with performance
7. **Design Tokens**: Use SpacingTokens, SizeTokens for consistent UI
8. **Type Safety**: Leverage sealed classes for compile-time safety

This promo code system provides a robust foundation for community-driven discount sharing with enterprise-level architecture and modern Android development patterns.
