# Ultimate Error Handling Refactor

> **Status**: Planning Phase
> **Timeline**: TBD
> **Complexity**: High
> **Breaking Changes**: YES - Complete architectural overhaul

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [Current State Analysis](#current-state-analysis)
3. [Research Findings](#research-findings)
4. [Target Architecture](#target-architecture)
5. [Implementation Roadmap](#implementation-roadmap)
6. [Migration Strategy](#migration-strategy)
7. [Developer Guidelines](#developer-guidelines)
8. [Risk Assessment](#risk-assessment)
9. [Success Metrics](#success-metrics)

---

## Executive Summary

This document outlines a complete overhaul of Qode's error handling architecture, transitioning from a generic `ErrorType` enum-based system to a sophisticated **domain-specific sealed error interface** architecture that provides:

- **Type-safe error handling** with compiler-enforced exhaustive handling
- **Rich error context** with domain-specific data
- **Better UX** through precise error messages and recovery actions
- **Enhanced analytics** with detailed error tracking
- **Future-ready** for Kotlin 2.4 Rich Errors migration

### Why This Refactor?

1. **Current system limitations**: Generic ErrorType enum doesn't scale
2. **Industry evolution**: 2025 best practices favor sealed interfaces
3. **Competitive advantage**: Even Now in Android lacks this sophistication
4. **Future-proofing**: Preparation for Kotlin 2.4 Rich Errors

---

## Current State Analysis

### What We Have Now ✅

Our current error handling is already **more sophisticated than industry standards**:

```kotlin
// shared/src/commonMain/kotlin/com/qodein/shared/common/result/
├── Result.kt                 // Sealed interface (Success|Error|Loading)
├── ErrorType.kt             // 53 classified error types
├── ExceptionExt.kt          // Smart error classification extensions
└── QodeException.kt         // Typed exception hierarchy
```

#### Strengths of Current System

1. **Consistent Pattern**: `.asResult()` extension used throughout
2. **Error Classification**: 53 ErrorType variants with smart mapping
3. **UX Considerations**: Retry logic, snackbar behavior, error codes
4. **Clean Architecture**: Repository throws → Use case wraps → ViewModel handles
5. **Analytics Support**: Error codes and tracking
6. **Localization Ready**: No strings in shared module

#### Current Implementation Example

```kotlin
// Repository Layer
class PromoCodeRepositoryImpl : PromoCodeRepository {
    override fun getPromoCodes(): Flow<List<PromoCode>> =
        dataSource.getPromoCodes() // Throws IOException/SecurityException
}

// Use Case Layer
class GetPromoCodesUseCase @Inject constructor(
    private val repository: PromoCodeRepository
) {
    operator fun invoke(): Flow<Result<List<PromoCode>>> =
        repository.getPromoCodes().asResult() // Automatic wrapping
}

// ViewModel Layer
viewModelScope.launch {
    getPromoCodesUseCase().collect { result ->
        _uiState.value = when (result) {
            is Result.Loading -> PromoCodeState.Loading
            is Result.Success -> PromoCodeState.Success(result.data)
            is Result.Error -> PromoCodeState.Error(
                errorType = result.exception.toErrorType(),
                isRetryable = result.exception.isRetryable(),
                shouldShowSnackbar = result.exception.shouldShowSnackbar()
            )
        }
    }
}
```

### What's Limited About Current System ❌

1. **Exception-based**: Still relies on throwables underneath
2. **String parsing**: Exception message analysis for classification
3. **Limited expressiveness**: ErrorType enum can't carry rich context
4. **Scalability concerns**: Enum grows unwieldy (already 53 values)
5. **Missing domain specificity**: Can't model complex business scenarios
6. **Generic error handling**: Same logic for different error contexts

---

## Research Findings

### Now in Android Analysis

After deep analysis of the official Android architecture sample:

#### What NIA Actually Has
```kotlin
// Their complete Result.kt implementation
sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val exception: Throwable) : Result<Nothing>
    data object Loading : Result<Nothing>
}

fun <T> Flow<T>.asResult(): Flow<Result<T>> =
    map { Result.Success(it) }
    .onStart { emit(Result.Loading) }
    .catch { emit(Result.Error(it)) }
```

#### What NIA Lacks (That We Have)
- ❌ No ErrorType classification
- ❌ No retry logic extensions
- ❌ No UX behavior guidance
- ❌ No error analytics
- ❌ No recovery suggestions

#### Community Feedback on NIA
- **Issue #708**: Community wants better error handling examples
- **Discussion #932**: No consensus on where exceptions should be handled
- **Pull #1789**: Attempts to add general error handler show gaps

**Conclusion**: Our current system is already more advanced than the "gold standard"!

### 2025 Best Practices Research

#### Sealed Interfaces vs Result
- **Sealed interfaces** preferred for complex error scenarios (2025 consensus)
- **Better domain modeling** with specific error contexts
- **Compiler safety** with exhaustive when handling
- **Type safety** over generic Throwable

#### Kotlin 2.4 Rich Errors Preview
```kotlin
// Coming in Kotlin 2.4 - Union Types for Error Handling
fun login(): User | NetworkException | ValidationException
```

- Native union types eliminate Result wrappers
- Type-safe error handling without exceptions
- Direct error return without throwing

#### Arrow Either Analysis
- **Pros**: Type-safe error handling, rich context
- **Cons**: "Either hell" complexity, external dependency
- **Verdict**: Native sealed interfaces preferred

---

## Target Architecture

### Core Philosophy

**Domain-Driven Error Modeling**: Each business domain owns its error types with rich, typed context instead of generic classification.

### Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                     UI Layer                                │
│  ┌─────────────────┐    ┌─────────────────┐                │
│  │  ViewModels     │    │   UI States     │                │
│  │  - Type-safe    │    │  - Sealed error │                │
│  │    error        │    │    interfaces   │                │
│  │    handling     │    │  - Rich context │                │
│  └─────────────────┘    └─────────────────┘                │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                    Domain Layer                             │
│  ┌─────────────────┐    ┌─────────────────┐                │
│  │   Use Cases     │    │ Error Recovery  │                │
│  │  - Typed error  │    │   - Smart retry │                │
│  │    propagation  │    │   - Fallbacks   │                │
│  │  - Business     │    │   - User        │                │
│  │    logic        │    │     guidance    │                │
│  └─────────────────┘    └─────────────────┘                │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────────┐
│                     Data Layer                              │
│  ┌─────────────────┐    ┌─────────────────┐                │
│  │  Repositories   │    │ Error Mapping   │                │
│  │  - Domain-      │    │   - Exception   │                │
│  │    specific     │    │     to domain   │                │
│  │    errors       │    │   - Rich        │                │
│  │  - Typed        │    │     context     │                │
│  │    results      │    │     creation    │                │
│  └─────────────────┘    └─────────────────┘                │
└─────────────────────────────────────────────────────────────┘
```

### Core Error Hierarchy

```kotlin
// Base domain error interface
sealed interface DomainError {
    val errorId: String
    val timestamp: Instant
    val context: Map<String, Any>
}

// Network domain errors
sealed interface NetworkError : DomainError {
    data class Timeout(
        val timeoutMs: Long,
        val url: String,
        val retryCount: Int = 0,
        override val errorId: String = "NET_TIMEOUT",
        override val timestamp: Instant = Clock.System.now(),
        override val context: Map<String, Any> = mapOf(
            "timeout_ms" to timeoutMs,
            "url" to url,
            "retry_count" to retryCount
        )
    ) : NetworkError

    data class NoConnection(
        val lastConnectedAt: Instant?,
        val connectionType: ConnectionType?,
        override val errorId: String = "NET_NO_CONNECTION",
        override val timestamp: Instant = Clock.System.now(),
        override val context: Map<String, Any> = mapOf(
            "last_connected_at" to (lastConnectedAt?.toString() ?: "never"),
            "connection_type" to (connectionType?.name ?: "unknown")
        )
    ) : NetworkError

    data class ServerError(
        val httpCode: Int,
        val serverMessage: String,
        val endpoint: String,
        val requestId: String?,
        override val errorId: String = "NET_SERVER_ERROR",
        override val timestamp: Instant = Clock.System.now(),
        override val context: Map<String, Any> = mapOf(
            "http_code" to httpCode,
            "server_message" to serverMessage,
            "endpoint" to endpoint,
            "request_id" to (requestId ?: "unknown")
        )
    ) : NetworkError
}

// Authentication domain errors
sealed interface AuthError : DomainError {
    data object UserCancelled : AuthError {
        override val errorId: String = "AUTH_USER_CANCELLED"
        override val timestamp: Instant = Clock.System.now()
        override val context: Map<String, Any> = emptyMap()
    }

    data class InvalidCredentials(
        val attemptCount: Int,
        val lockoutTimeRemaining: Duration?,
        val provider: AuthProvider,
        override val errorId: String = "AUTH_INVALID_CREDENTIALS",
        override val timestamp: Instant = Clock.System.now(),
        override val context: Map<String, Any> = mapOf(
            "attempt_count" to attemptCount,
            "lockout_remaining_ms" to (lockoutTimeRemaining?.inWholeMilliseconds ?: 0),
            "provider" to provider.name
        )
    ) : AuthError

    data class Unauthorized(
        val requiredPermissions: List<String>,
        val currentPermissions: List<String>,
        val resource: String,
        override val errorId: String = "AUTH_UNAUTHORIZED",
        override val timestamp: Instant = Clock.System.now(),
        override val context: Map<String, Any> = mapOf(
            "required_permissions" to requiredPermissions,
            "current_permissions" to currentPermissions,
            "resource" to resource
        )
    ) : AuthError
}

// Business logic domain errors
sealed interface BusinessError : DomainError {
    data class PromoCodeExpired(
        val promoCode: String,
        val expiredAt: Instant,
        val service: Service,
        val userNotificationSent: Boolean = false,
        override val errorId: String = "BIZ_PROMO_EXPIRED",
        override val timestamp: Instant = Clock.System.now(),
        override val context: Map<String, Any> = mapOf(
            "promo_code" to promoCode,
            "expired_at" to expiredAt.toString(),
            "service_id" to service.id.value,
            "user_notified" to userNotificationSent
        )
    ) : BusinessError

    data class ServiceUnavailable(
        val service: Service,
        val reason: UnavailabilityReason,
        val estimatedRecovery: Duration?,
        val alternativeServices: List<Service> = emptyList(),
        override val errorId: String = "BIZ_SERVICE_UNAVAILABLE",
        override val timestamp: Instant = Clock.System.now(),
        override val context: Map<String, Any> = mapOf(
            "service_id" to service.id.value,
            "reason" to reason.name,
            "estimated_recovery_ms" to (estimatedRecovery?.inWholeMilliseconds ?: -1),
            "alternatives_count" to alternativeServices.size
        )
    ) : BusinessError
}

// Validation domain errors
sealed interface ValidationError : DomainError {
    data class RequiredField(
        val fieldName: String,
        val fieldType: FieldType,
        override val errorId: String = "VAL_REQUIRED_FIELD",
        override val timestamp: Instant = Clock.System.now(),
        override val context: Map<String, Any> = mapOf(
            "field_name" to fieldName,
            "field_type" to fieldType.name
        )
    ) : ValidationError

    data class InvalidFormat(
        val fieldName: String,
        val expectedFormat: String,
        val actualValue: String,
        val formatExamples: List<String> = emptyList(),
        override val errorId: String = "VAL_INVALID_FORMAT",
        override val timestamp: Instant = Clock.System.now(),
        override val context: Map<String, Any> = mapOf(
            "field_name" to fieldName,
            "expected_format" to expectedFormat,
            "actual_length" to actualValue.length,
            "examples_count" to formatExamples.size
        )
    ) : ValidationError
}
```

### Enhanced Result System

```kotlin
// Enhanced Result supporting typed errors
sealed interface Result<out T, out E : DomainError> {
    data class Success<T>(val data: T) : Result<T, Nothing>
    data class Error<E : DomainError>(val error: E) : Result<Nothing, E>
    data object Loading : Result<Nothing, Nothing>
}

// Specialized Result aliases for common patterns
typealias NetworkResult<T> = Result<T, NetworkError>
typealias AuthResult<T> = Result<T, AuthError>
typealias BusinessResult<T> = Result<T, BusinessError>
typealias ValidationResult<T> = Result<T, ValidationError>

// Multi-domain Result for complex operations
typealias PromoCodeResult<T> = Result<T, NetworkError | AuthError | BusinessError>
```

### Type-Safe Error Handling

```kotlin
// Compiler-enforced exhaustive error handling
suspend fun handlePromoCodeLoad(result: PromoCodeResult<List<PromoCode>>) {
    when (result) {
        is Result.Success -> displayPromoCodes(result.data)
        is Result.Error -> when (val error = result.error) {
            // Network errors
            is NetworkError.Timeout -> {
                showRetryDialog(
                    message = "Request timed out after ${error.timeoutMs}ms",
                    retryDelay = calculateRetryDelay(error.retryCount)
                )
                analyticsHelper.trackError("network_timeout", error.context)
            }
            is NetworkError.NoConnection -> {
                showOfflineMode(lastConnected = error.lastConnectedAt)
                enableOfflineQueue()
            }
            is NetworkError.ServerError -> {
                when (error.httpCode) {
                    in 500..599 -> showServerErrorDialog(error.requestId)
                    429 -> showRateLimitDialog(error.serverMessage)
                    else -> showGenericServerError(error.httpCode)
                }
            }

            // Auth errors
            is AuthError.UserCancelled -> {
                // User cancelled - no action needed
                analyticsHelper.trackUserAction("auth_cancelled")
            }
            is AuthError.InvalidCredentials -> {
                if (error.lockoutTimeRemaining != null) {
                    showLockoutDialog(error.lockoutTimeRemaining)
                } else {
                    showSignInDialog(attemptsRemaining = 3 - error.attemptCount)
                }
            }
            is AuthError.Unauthorized -> {
                showPermissionDialog(
                    required = error.requiredPermissions,
                    current = error.currentPermissions,
                    resource = error.resource
                )
            }

            // Business errors
            is BusinessError.PromoCodeExpired -> {
                showExpiredPromoDialog(
                    promoCode = error.promoCode,
                    expiredAt = error.expiredAt,
                    service = error.service
                )
                suggestAlternativePromos(error.service)
            }
            is BusinessError.ServiceUnavailable -> {
                showServiceUnavailableDialog(
                    service = error.service,
                    reason = error.reason,
                    estimatedRecovery = error.estimatedRecovery
                )
                if (error.alternativeServices.isNotEmpty()) {
                    suggestAlternatives(error.alternativeServices)
                }
            }
        }
        is Result.Loading -> showLoadingIndicator()
    }
}
```

---

## Implementation Roadmap

### Phase 1: Foundation Architecture (Weeks 1-2)

**Goal**: Create core sealed error interfaces and enhanced Result system

#### 1.1 Core Error Interfaces (3 days)
```kotlin
// New file: shared/src/commonMain/kotlin/com/qodein/shared/common/error/DomainError.kt
// - Base DomainError interface
// - NetworkError sealed hierarchy
// - AuthError sealed hierarchy
// - BusinessError sealed hierarchy
// - ValidationError sealed hierarchy
```

#### 1.2 Enhanced Result System (3 days)
```kotlin
// New file: shared/src/commonMain/kotlin/com/qodein/shared/common/result/TypedResult.kt
// - Result<T, E> with typed errors
// - Result type aliases (NetworkResult, AuthResult, etc.)
// - Extension functions for Result operations
```

#### 1.3 Error Mapping Utilities (2 days)
```kotlin
// New file: shared/src/commonMain/kotlin/com/qodein/shared/common/error/ErrorMapper.kt
// - Exception -> DomainError mapping
// - Context enrichment utilities
// - Error aggregation for multi-domain operations
```

#### 1.4 Foundation Testing (2 days)
- Unit tests for all error types
- Result system tests
- Error mapping validation

**Deliverables**:
- ✅ Core error interfaces
- ✅ Enhanced Result system
- ✅ Error mapping utilities
- ✅ Comprehensive tests

### Phase 2: Shared Domain Migration (Weeks 3-4)

**Goal**: Migrate shared domain types and create domain-specific error contexts

#### 2.1 Service Domain Errors (3 days)
```kotlin
// Update: shared/src/commonMain/kotlin/com/qodein/shared/domain/error/ServiceError.kt
sealed interface ServiceError : DomainError {
    data class ServiceNotFound(val serviceName: String, val searchCriteria: Map<String, Any>)
    data class ServiceUnavailable(val service: Service, val reason: UnavailabilityReason)
    data class ServiceConfigurationError(val service: Service, val missingConfig: List<String>)
}
```

#### 2.2 PromoCode Domain Errors (3 days)
```kotlin
// Update: shared/src/commonMain/kotlin/com/qodein/shared/domain/error/PromoCodeError.kt
sealed interface PromoCodeError : DomainError {
    data class PromoCodeNotFound(val code: String, val service: Service?)
    data class PromoCodeExpired(val promoCode: PromoCode, val expiredAt: Instant)
    data class PromoCodeAlreadyUsed(val promoCode: PromoCode, val usedAt: Instant, val user: UserId)
    data class MinimumOrderNotMet(val promoCode: PromoCode, val required: Money, val current: Money)
}
```

#### 2.3 User Domain Errors (2 days)
```kotlin
// New: shared/src/commonMain/kotlin/com/qodein/shared/domain/error/UserError.kt
sealed interface UserError : DomainError {
    data class UserNotFound(val identifier: String, val searchType: UserSearchType)
    data class UserBanned(val user: User, val banReason: String, val bannedUntil: Instant?)
    data class UserSuspended(val user: User, val suspensionReason: String, val suspendedUntil: Instant)
}
```

#### 2.4 Domain Error Testing (2 days)
- Domain-specific error creation tests
- Error context validation
- Serialization/deserialization tests

**Deliverables**:
- ✅ Service domain errors
- ✅ PromoCode domain errors
- ✅ User domain errors
- ✅ Domain error tests

### Phase 3: Data Layer Foundation (Weeks 5-6)

**Goal**: Update repository interfaces and create typed error mappers

#### 3.1 Repository Interface Migration (4 days)
```kotlin
// Update all repository interfaces to return typed Results
interface PromoCodeRepository {
    // Before: fun getPromoCodes(): Flow<List<PromoCode>>
    // After: fun getPromoCodes(): Flow<Result<List<PromoCode>, PromoCodeError | NetworkError>>

    fun getPromoCode(id: PromoCodeId): Flow<Result<PromoCode, PromoCodeError | NetworkError>>
    fun submitPromoCode(request: SubmitPromoCodeRequest): Flow<Result<PromoCode, PromoCodeError | AuthError | ValidationError>>
}

interface UserRepository {
    fun getUser(userId: UserId): Flow<Result<User, UserError | NetworkError>>
    fun updateUser(user: User): Flow<Result<User, UserError | AuthError | ValidationError>>
}

interface ServiceRepository {
    fun getServices(): Flow<Result<List<Service>, ServiceError | NetworkError>>
    fun searchServices(query: String): Flow<Result<List<Service>, ServiceError | NetworkError>>
}
```

#### 3.2 Firebase Error Mappers (4 days)
```kotlin
// New: core/data/src/main/java/com/qodein/core/data/error/FirebaseErrorMapper.kt
object FirebaseErrorMapper {
    fun mapFirebaseException(exception: Exception, context: OperationContext): DomainError {
        return when (exception) {
            is FirebaseAuthException -> when (exception.errorCode) {
                "ERROR_USER_NOT_FOUND" -> AuthError.InvalidCredentials(...)
                "ERROR_WRONG_PASSWORD" -> AuthError.InvalidCredentials(...)
                "ERROR_USER_DISABLED" -> UserError.UserBanned(...)
                else -> AuthError.Unauthorized(...)
            }
            is FirestoreException -> when (exception.code) {
                FirestoreException.Code.NOT_FOUND -> BusinessError.ResourceNotFound(...)
                FirestoreException.Code.PERMISSION_DENIED -> AuthError.Unauthorized(...)
                FirestoreException.Code.UNAVAILABLE -> NetworkError.ServerError(...)
                else -> NetworkError.ServerError(...)
            }
            is IOException -> NetworkError.NoConnection(...)
            else -> NetworkError.ServerError(...)
        }
    }
}
```

#### 3.3 Repository Implementation Updates (4 days)
- Update all repository implementations
- Integrate Firebase error mappers
- Add rich context to errors

**Deliverables**:
- ✅ Repository interfaces with typed errors
- ✅ Firebase error mappers
- ✅ Repository implementations
- ✅ Data layer tests

### Phase 4: Use Case Layer Migration (Weeks 7-8)

**Goal**: Update use cases with typed error handling and business logic

#### 4.1 Core Use Case Updates (4 days)
```kotlin
// Update use cases to handle typed errors
class GetPromoCodesUseCase @Inject constructor(
    private val repository: PromoCodeRepository
) {
    operator fun invoke(
        sortBy: SortBy = SortBy.POPULARITY,
        filterBy: FilterCriteria? = null
    ): Flow<Result<List<PromoCode>, PromoCodeError | NetworkError | AuthError>> {
        return repository.getPromoCodes()
            .map { result ->
                when (result) {
                    is Result.Success -> {
                        val filteredAndSorted = result.data
                            .let { if (filterBy != null) applyFilter(it, filterBy) else it }
                            .sortedWith(sortBy.comparator)
                        Result.Success(filteredAndSorted)
                    }
                    is Result.Error -> result
                    is Result.Loading -> result
                }
            }
            .catch { exception ->
                emit(Result.Error(ErrorMapper.mapToBusinessError(exception)))
            }
    }
}
```

#### 4.2 Multi-Domain Use Cases (4 days)
```kotlin
// Use cases that combine multiple domains
class SubmitPromoCodeUseCase @Inject constructor(
    private val promoCodeRepository: PromoCodeRepository,
    private val userRepository: UserRepository,
    private val analyticsHelper: AnalyticsHelper
) {
    suspend operator fun invoke(
        request: SubmitPromoCodeRequest
    ): Result<PromoCode, PromoCodeError | UserError | AuthError | ValidationError | NetworkError> {

        // Validate user permissions first
        val userResult = userRepository.getCurrentUser().first()
        if (userResult is Result.Error) {
            return Result.Error(userResult.error)
        }

        val user = (userResult as Result.Success).data

        // Check if user can submit promo codes
        if (!user.canSubmitPromoCodes) {
            return Result.Error(UserError.InsufficientPermissions(
                user = user,
                requiredPermission = "SUBMIT_PROMO_CODES",
                currentPermissions = user.permissions
            ))
        }

        // Submit promo code
        return promoCodeRepository.submitPromoCode(request).first()
    }
}
```

#### 4.3 Error Recovery Logic (4 days)
```kotlin
// New: shared/src/commonMain/kotlin/com/qodein/shared/domain/error/ErrorRecovery.kt
object ErrorRecovery {
    suspend fun <T, E : DomainError> retryWithBackoff(
        maxRetries: Int = 3,
        initialDelay: Duration = 1.seconds,
        maxDelay: Duration = 30.seconds,
        backoffMultiplier: Double = 2.0,
        retryCondition: (E) -> Boolean = { true },
        operation: suspend () -> Result<T, E>
    ): Result<T, E> {
        var currentDelay = initialDelay
        repeat(maxRetries) { attempt ->
            val result = operation()
            when {
                result is Result.Success -> return result
                result is Result.Error && retryCondition(result.error) -> {
                    if (attempt < maxRetries - 1) {
                        delay(currentDelay)
                        currentDelay = (currentDelay * backoffMultiplier)
                            .coerceAtMost(maxDelay)
                    }
                }
                result is Result.Error -> return result
            }
        }
        return operation() // Final attempt
    }
}
```

**Deliverables**:
- ✅ Core use case updates
- ✅ Multi-domain use cases
- ✅ Error recovery logic
- ✅ Use case tests

### Phase 5: UI State Migration (Weeks 9-10)

**Goal**: Update UI states to use typed errors with rich context

#### 5.1 State Class Updates (4 days)
```kotlin
// Before: BannerState.kt
sealed class BannerState {
    data class Success(val banners: List<Banner>) : BannerState()
    data object Loading : BannerState()
    data class Error(val errorType: ErrorType, val isRetryable: Boolean, ...) : BannerState()
}

// After: Enhanced with typed errors
sealed interface BannerState {
    data class Success(val banners: List<Banner>) : BannerState
    data object Loading : BannerState
    data class Error(
        val error: DomainError,
        val retryAction: (() -> Unit)? = null,
        val alternativeActions: List<ErrorAction> = emptyList()
    ) : BannerState {

        val isRetryable: Boolean
            get() = when (error) {
                is NetworkError.Timeout, is NetworkError.NoConnection -> true
                is NetworkError.ServerError -> error.httpCode in 500..599
                is AuthError.UserCancelled -> true
                else -> false
            }

        val primaryMessage: String
            get() = when (error) {
                is NetworkError.Timeout -> "Request timed out after ${error.timeoutMs}ms"
                is NetworkError.NoConnection -> "No internet connection"
                is BusinessError.ServiceUnavailable -> "${error.service.name} is temporarily unavailable"
                else -> "An error occurred"
            }

        val secondaryMessage: String?
            get() = when (error) {
                is NetworkError.NoConnection ->
                    error.lastConnectedAt?.let { "Last connected ${formatTimeAgo(it)}" }
                is BusinessError.ServiceUnavailable ->
                    error.estimatedRecovery?.let { "Expected back in ${formatDuration(it)}" }
                else -> null
            }
    }
}
```

#### 5.2 ViewModel State Management (4 days)
```kotlin
// Enhanced ViewModels with rich error handling
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getBannersUseCase: GetBannersUseCase,
    private val getPromoCodesUseCase: GetPromoCodesUseCase,
    private val errorRecovery: ErrorRecovery,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {

    private fun loadBanners() {
        viewModelScope.launch {
            errorRecovery.retryWithBackoff(
                maxRetries = 3,
                retryCondition = { error ->
                    when (error) {
                        is NetworkError.Timeout, is NetworkError.NoConnection -> true
                        is NetworkError.ServerError -> error.httpCode >= 500
                        else -> false
                    }
                }
            ) {
                getBannersUseCase().first()
            }.collect { result ->
                _uiState.update { state ->
                    state.copy(
                        bannerState = when (result) {
                            is Result.Loading -> BannerState.Loading
                            is Result.Success -> BannerState.Success(result.data)
                            is Result.Error -> {
                                analyticsHelper.trackError(
                                    errorId = result.error.errorId,
                                    context = result.error.context
                                )
                                BannerState.Error(
                                    error = result.error,
                                    retryAction = if (result.error.isRetryable()) {
                                        { loadBanners() }
                                    } else null,
                                    alternativeActions = getAlternativeActions(result.error)
                                )
                            }
                        }
                    )
                }
            }
        }
    }

    private fun getAlternativeActions(error: DomainError): List<ErrorAction> {
        return when (error) {
            is AuthError -> listOf(
                ErrorAction.SignIn { navigateToSignIn() },
                ErrorAction.ContactSupport { openSupportChat() }
            )
            is NetworkError.NoConnection -> listOf(
                ErrorAction.CheckSettings { openNetworkSettings() },
                ErrorAction.ViewCached { loadCachedData() }
            )
            is BusinessError.ServiceUnavailable -> listOf(
                ErrorAction.ViewAlternatives { showAlternativeServices(error.alternativeServices) },
                ErrorAction.NotifyWhenAvailable { setupAvailabilityNotification(error.service) }
            )
            else -> emptyList()
        }
    }
}
```

#### 5.3 Error Action System (4 days)
```kotlin
// New: shared/src/commonMain/kotlin/com/qodein/shared/ui/error/ErrorAction.kt
sealed interface ErrorAction {
    val id: String
    val label: String
    val action: () -> Unit

    data class Retry(
        override val label: String = "Try Again",
        override val action: () -> Unit
    ) : ErrorAction {
        override val id: String = "retry"
    }

    data class SignIn(
        override val label: String = "Sign In",
        override val action: () -> Unit
    ) : ErrorAction {
        override val id: String = "sign_in"
    }

    data class ContactSupport(
        override val label: String = "Contact Support",
        override val action: () -> Unit
    ) : ErrorAction {
        override val id: String = "contact_support"
    }

    data class ViewCached(
        override val label: String = "View Offline",
        override val action: () -> Unit
    ) : ErrorAction {
        override val id: String = "view_cached"
    }
}
```

**Deliverables**:
- ✅ Enhanced UI state classes
- ✅ Rich ViewModel error handling
- ✅ Error action system
- ✅ UI state tests

### Phase 6: UI Component Migration (Weeks 11-12)

**Goal**: Create type-safe UI error components with rich displays

#### 6.1 Enhanced Error Components (4 days)
```kotlin
// New: core/ui/src/main/java/com/qodein/core/ui/error/TypedErrorCard.kt
@Composable
fun TypedErrorCard(
    error: DomainError,
    primaryAction: ErrorAction? = null,
    secondaryActions: List<ErrorAction> = emptyList(),
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (error) {
                is NetworkError -> MaterialTheme.colorScheme.errorContainer
                is AuthError -> MaterialTheme.colorScheme.warningContainer
                is BusinessError -> MaterialTheme.colorScheme.tertiaryContainer
                is ValidationError -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Error header with icon
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = error.getIcon(),
                    contentDescription = null,
                    tint = error.getIconTint()
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = error.getPrimaryMessage(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    error.getSecondaryMessage()?.let { secondary ->
                        Text(
                            text = secondary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                onDismiss?.let { dismiss ->
                    IconButton(onClick = dismiss) {
                        Icon(
                            imageVector = QodeIcons.Close,
                            contentDescription = "Dismiss"
                        )
                    }
                }
            }

            // Rich error context (expandable)
            if (error.context.isNotEmpty()) {
                var showContext by remember { mutableStateOf(false) }

                TextButton(
                    onClick = { showContext = !showContext }
                ) {
                    Text(if (showContext) "Hide Details" else "Show Details")
                    Icon(
                        imageVector = if (showContext) QodeIcons.ExpandLess else QodeIcons.ExpandMore,
                        contentDescription = null
                    )
                }

                AnimatedVisibility(visible = showContext) {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        items(error.context.entries.toList()) { (key, value) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = key.replace("_", " ").capitalizeWords(),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = value.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Action buttons
            if (primaryAction != null || secondaryActions.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Primary action
                    primaryAction?.let { action ->
                        Button(
                            onClick = action.action,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(action.label)
                        }
                    }

                    // Secondary actions
                    secondaryActions.forEach { action ->
                        OutlinedButton(
                            onClick = action.action
                        ) {
                            Text(action.label)
                        }
                    }
                }
            }
        }
    }
}

// Extension functions for error display
@Composable
private fun DomainError.getIcon(): ImageVector = when (this) {
    is NetworkError -> QodeIcons.WifiOff
    is AuthError -> QodeIcons.Lock
    is BusinessError -> QodeIcons.Business
    is ValidationError -> QodeIcons.Warning
    else -> QodeIcons.Error
}

@Composable
private fun DomainError.getIconTint(): Color = when (this) {
    is NetworkError -> MaterialTheme.colorScheme.error
    is AuthError -> MaterialTheme.colorScheme.warning
    is BusinessError -> MaterialTheme.colorScheme.tertiary
    is ValidationError -> MaterialTheme.colorScheme.primary
    else -> MaterialTheme.colorScheme.onSurfaceVariant
}

private fun DomainError.getPrimaryMessage(): String = when (this) {
    is NetworkError.Timeout -> "Request timed out"
    is NetworkError.NoConnection -> "No internet connection"
    is NetworkError.ServerError -> "Server error (${httpCode})"
    is AuthError.UserCancelled -> "Sign in was cancelled"
    is AuthError.InvalidCredentials -> "Invalid credentials"
    is AuthError.Unauthorized -> "Access denied"
    is BusinessError.PromoCodeExpired -> "Promo code has expired"
    is BusinessError.ServiceUnavailable -> "Service temporarily unavailable"
    is ValidationError.RequiredField -> "Required field missing"
    is ValidationError.InvalidFormat -> "Invalid format"
    else -> "An error occurred"
}

private fun DomainError.getSecondaryMessage(): String? = when (this) {
    is NetworkError.Timeout -> "Request took longer than ${timeoutMs}ms"
    is NetworkError.NoConnection -> lastConnectedAt?.let { "Last connected ${formatTimeAgo(it)}" }
    is NetworkError.ServerError -> serverMessage.takeIf { it.isNotBlank() }
    is AuthError.InvalidCredentials -> lockoutTimeRemaining?.let {
        "Try again in ${formatDuration(it)}"
    }
    is AuthError.Unauthorized -> "Missing permissions: ${requiredPermissions.joinToString()}"
    is BusinessError.PromoCodeExpired -> "Expired on ${formatDate(expiredAt)}"
    is BusinessError.ServiceUnavailable -> estimatedRecovery?.let {
        "Expected back in ${formatDuration(it)}"
    }
    is ValidationError.RequiredField -> "Please fill in the ${fieldName} field"
    is ValidationError.InvalidFormat -> "Expected format: $expectedFormat"
    else -> null
}
```

#### 6.2 Screen-Specific Error Displays (4 days)
```kotlin
// Enhanced screen composables with typed error handling
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Banner section
        when (val bannerState = uiState.bannerState) {
            is BannerState.Loading -> BannerLoadingPlaceholder()
            is BannerState.Success -> BannerCarousel(
                banners = bannerState.banners,
                onBannerClick = { onAction(HomeAction.BannerClicked(it)) }
            )
            is BannerState.Error -> TypedErrorCard(
                error = bannerState.error,
                primaryAction = bannerState.retryAction?.let { retry ->
                    ErrorAction.Retry { retry() }
                },
                secondaryActions = bannerState.alternativeActions,
                onDismiss = { onAction(HomeAction.DismissBannerError) }
            )
        }

        // Promo codes section
        when (val promoState = uiState.promoCodeState) {
            is PromoCodeState.Loading -> PromoCodeLoadingGrid()
            is PromoCodeState.Success -> PromoCodeGrid(
                promoCodes = promoState.promoCodes,
                onPromoClick = { onAction(HomeAction.PromoCodeClicked(it)) },
                onLoadMore = { onAction(HomeAction.LoadMore) }
            )
            is PromoCodeState.Empty -> EmptyPromoCodeState()
            is PromoCodeState.Error -> TypedErrorCard(
                error = promoState.error,
                primaryAction = promoState.retryAction?.let { retry ->
                    ErrorAction.Retry { retry() }
                },
                secondaryActions = promoState.alternativeActions
            )
        }
    }
}
```

**Deliverables**:
- ✅ Type-safe error components
- ✅ Screen-specific error displays
- ✅ Rich error context UI
- ✅ Component tests

### Phase 7: Analytics & Monitoring (Weeks 13-14)

**Goal**: Implement comprehensive error analytics and monitoring

#### 7.1 Error Analytics System (4 days)
```kotlin
// New: core/analytics/src/main/java/com/qodein/core/analytics/error/ErrorAnalytics.kt
interface ErrorAnalytics {
    fun trackError(error: DomainError, additionalContext: Map<String, Any> = emptyMap())
    fun trackErrorRecovery(error: DomainError, recoveryMethod: String, success: Boolean)
    fun trackUserErrorAction(error: DomainError, action: ErrorAction)
}

@Singleton
class ErrorAnalyticsImpl @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val crashlytics: FirebaseCrashlytics
) : ErrorAnalytics {

    override fun trackError(error: DomainError, additionalContext: Map<String, Any>) {
        val bundle = Bundle().apply {
            putString("error_id", error.errorId)
            putString("error_type", error::class.simpleName)
            putLong("timestamp", error.timestamp.toEpochMilliseconds())

            // Add error-specific context
            error.context.forEach { (key, value) ->
                when (value) {
                    is String -> putString("ctx_$key", value)
                    is Number -> putLong("ctx_$key", value.toLong())
                    is Boolean -> putBoolean("ctx_$key", value)
                    else -> putString("ctx_$key", value.toString())
                }
            }

            // Add additional context
            additionalContext.forEach { (key, value) ->
                when (value) {
                    is String -> putString("add_$key", value)
                    is Number -> putLong("add_$key", value.toLong())
                    is Boolean -> putBoolean("add_$key", value)
                    else -> putString("add_$key", value.toString())
                }
            }
        }

        firebaseAnalytics.logEvent("qode_error", bundle)

        // Log to Crashlytics for non-fatal tracking
        crashlytics.recordException(DomainErrorException(error))

        // Add custom keys for filtering
        crashlytics.setCustomKey("last_error_id", error.errorId)
        crashlytics.setCustomKey("last_error_timestamp", error.timestamp.toString())
    }

    override fun trackErrorRecovery(error: DomainError, recoveryMethod: String, success: Boolean) {
        val bundle = Bundle().apply {
            putString("original_error_id", error.errorId)
            putString("recovery_method", recoveryMethod)
            putBoolean("recovery_success", success)
            putLong("recovery_timestamp", Clock.System.now().toEpochMilliseconds())
        }

        firebaseAnalytics.logEvent("qode_error_recovery", bundle)
    }

    override fun trackUserErrorAction(error: DomainError, action: ErrorAction) {
        val bundle = Bundle().apply {
            putString("error_id", error.errorId)
            putString("action_id", action.id)
            putString("action_label", action.label)
            putLong("action_timestamp", Clock.System.now().toEpochMilliseconds())
        }

        firebaseAnalytics.logEvent("qode_error_action", bundle)
    }
}

// Exception wrapper for Crashlytics
class DomainErrorException(val domainError: DomainError) : Exception(
    "Domain Error: ${domainError.errorId} - ${domainError::class.simpleName}"
) {
    override val message: String
        get() = buildString {
            appendLine("Error ID: ${domainError.errorId}")
            appendLine("Type: ${domainError::class.simpleName}")
            appendLine("Timestamp: ${domainError.timestamp}")
            appendLine("Context:")
            domainError.context.forEach { (key, value) ->
                appendLine("  $key: $value")
            }
        }
}
```

#### 7.2 Error Monitoring Dashboard (4 days)
```kotlin
// New: core/analytics/src/main/java/com/qodein/core/analytics/error/ErrorMonitor.kt
@Singleton
class ErrorMonitor @Inject constructor(
    private val errorAnalytics: ErrorAnalytics,
    private val networkMonitor: NetworkMonitor,
    private val userManager: UserManager
) {

    private val errorFrequency = mutableMapOf<String, Int>()
    private val errorTrends = mutableMapOf<String, List<Long>>()

    fun reportError(error: DomainError, source: String) {
        // Track error frequency
        errorFrequency[error.errorId] = errorFrequency.getOrDefault(error.errorId, 0) + 1

        // Track error trends (last 24 hours)
        val currentTime = Clock.System.now().toEpochMilliseconds()
        val trend = errorTrends.getOrDefault(error.errorId, emptyList()).toMutableList()
        trend.add(currentTime)

        // Keep only last 24 hours
        val dayAgo = currentTime - 24 * 60 * 60 * 1000
        errorTrends[error.errorId] = trend.filter { it > dayAgo }

        // Enhanced context
        val enhancedContext = buildMap {
            putAll(error.context)
            put("source", source)
            put("network_available", networkMonitor.isOnline.value)
            put("user_authenticated", userManager.currentUser.value != null)
            put("error_frequency_24h", errorTrends[error.errorId]?.size ?: 0)

            // Add device context
            put("device_type", if (isTablet()) "tablet" else "phone")
            put("app_version", BuildConfig.VERSION_NAME)
            put("api_level", Build.VERSION.SDK_INT)
        }

        errorAnalytics.trackError(error, enhancedContext)

        // Check for error patterns that need immediate attention
        checkErrorPatterns(error)
    }

    private fun checkErrorPatterns(error: DomainError) {
        val recent = errorTrends[error.errorId] ?: return
        val currentTime = Clock.System.now().toEpochMilliseconds()

        // Check for error spikes (>5 errors in 5 minutes)
        val fiveMinutesAgo = currentTime - 5 * 60 * 1000
        val recentErrors = recent.count { it > fiveMinutesAgo }

        if (recentErrors > 5) {
            // Log critical error pattern
            crashlytics.log("ERROR SPIKE: ${error.errorId} occurred $recentErrors times in 5 minutes")

            // Could trigger alerts, circuit breakers, etc.
            when (error) {
                is NetworkError.ServerError -> {
                    // Consider enabling circuit breaker for this endpoint
                    notifyCircuitBreaker(error.endpoint)
                }
                is AuthError -> {
                    // Consider rate limiting auth attempts
                    notifyAuthRateLimit()
                }
            }
        }
    }
}
```

**Deliverables**:
- ✅ Error analytics system
- ✅ Error monitoring dashboard
- ✅ Pattern detection
- ✅ Analytics tests

### Phase 8: Testing Infrastructure (Weeks 15-16)

**Goal**: Create comprehensive testing framework for typed error handling

#### 8.1 Error Testing Utilities (4 days)
```kotlin
// New: shared/src/testFixtures/kotlin/com/qodein/shared/testing/error/ErrorTestFixtures.kt
object ErrorTestFixtures {

    fun createNetworkTimeout(
        timeoutMs: Long = 5000,
        url: String = "https://api.example.com/test",
        retryCount: Int = 0
    ) = NetworkError.Timeout(
        timeoutMs = timeoutMs,
        url = url,
        retryCount = retryCount
    )

    fun createPromoCodeExpired(
        promoCode: String = "TEST2024",
        service: Service = ServiceTestFixtures.createTestService(),
        daysAgo: Long = 1
    ) = BusinessError.PromoCodeExpired(
        promoCode = promoCode,
        expiredAt = Clock.System.now().minus(daysAgo.days),
        service = service
    )

    fun createAuthInvalidCredentials(
        attemptCount: Int = 1,
        lockoutMinutes: Long? = null,
        provider: AuthProvider = AuthProvider.EMAIL
    ) = AuthError.InvalidCredentials(
        attemptCount = attemptCount,
        lockoutTimeRemaining = lockoutMinutes?.minutes,
        provider = provider
    )
}

// Test extensions for Result types
fun <T, E : DomainError> Result<T, E>.assertIsSuccess(): T {
    return when (this) {
        is Result.Success -> data
        is Result.Error -> throw AssertionError("Expected Success but was Error: ${error}")
        is Result.Loading -> throw AssertionError("Expected Success but was Loading")
    }
}

fun <T, E : DomainError> Result<T, E>.assertIsError(): E {
    return when (this) {
        is Result.Success -> throw AssertionError("Expected Error but was Success: $data")
        is Result.Error -> error
        is Result.Loading -> throw AssertionError("Expected Error but was Loading")
    }
}

inline fun <T, E : DomainError, reified ExpectedError : E> Result<T, E>.assertIsError(): ExpectedError {
    val error = assertIsError()
    if (error !is ExpectedError) {
        throw AssertionError("Expected ${ExpectedError::class.simpleName} but was ${error::class.simpleName}")
    }
    return error
}
```

#### 8.2 Repository Testing (4 days)
```kotlin
// Example: PromoCodeRepositoryTest with typed error testing
class PromoCodeRepositoryTest {

    @Mock
    private lateinit var dataSource: PromoCodeDataSource

    @Mock
    private lateinit var errorMapper: FirebaseErrorMapper

    private lateinit var repository: PromoCodeRepositoryImpl

    @Test
    fun `getPromoCode returns success when data source succeeds`() = runTest {
        // Given
        val promoCodeId = PromoCodeId("test123")
        val expectedPromoCode = PromoCodeTestFixtures.createTestPromoCode(id = promoCodeId)
        whenever(dataSource.getPromoCode(promoCodeId)).thenReturn(expectedPromoCode)

        // When
        val result = repository.getPromoCode(promoCodeId).first()

        // Then
        val promoCode = result.assertIsSuccess()
        assertEquals(expectedPromoCode, promoCode)
    }

    @Test
    fun `getPromoCode returns PromoCodeNotFound when code doesn't exist`() = runTest {
        // Given
        val promoCodeId = PromoCodeId("nonexistent")
        val firebaseException = FirestoreException("Not found", FirestoreException.Code.NOT_FOUND)
        val expectedError = PromoCodeError.PromoCodeNotFound(promoCodeId.value, null)

        whenever(dataSource.getPromoCode(promoCodeId)).thenThrow(firebaseException)
        whenever(errorMapper.mapFirebaseException(firebaseException, any())).thenReturn(expectedError)

        // When
        val result = repository.getPromoCode(promoCodeId).first()

        // Then
        val error = result.assertIsError<PromoCodeError.PromoCodeNotFound>()
        assertEquals(promoCodeId.value, error.code)
    }

    @Test
    fun `getPromoCode returns NetworkError when network fails`() = runTest {
        // Given
        val promoCodeId = PromoCodeId("test123")
        val ioException = IOException("Network unreachable")
        val expectedError = NetworkError.NoConnection(
            lastConnectedAt = Clock.System.now().minus(5.minutes),
            connectionType = null
        )

        whenever(dataSource.getPromoCode(promoCodeId)).thenThrow(ioException)
        whenever(errorMapper.mapFirebaseException(ioException, any())).thenReturn(expectedError)

        // When
        val result = repository.getPromoCode(promoCodeId).first()

        // Then
        val error = result.assertIsError<NetworkError.NoConnection>()
        assertNotNull(error.lastConnectedAt)
    }
}
```

#### 8.3 UI Testing with Typed Errors (4 days)
```kotlin
// Example: HomeScreenTest with typed error scenarios
@HiltAndroidTest
class HomeScreenTest {

    @Test
    fun showsNetworkTimeoutErrorWithRetryButton() {
        // Given
        val timeoutError = ErrorTestFixtures.createNetworkTimeout(
            timeoutMs = 10000,
            url = "https://api.qode.com/banners"
        )
        val errorState = BannerState.Error(
            error = timeoutError,
            retryAction = { /* retry logic */ }
        )

        composeTestRule.setContent {
            HomeScreen(
                uiState = HomeUiState(bannerState = errorState),
                onAction = { }
            )
        }

        // Then
        composeTestRule
            .onNodeWithText("Request timed out")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Request took longer than 10000ms")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Try Again")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun showsPromoCodeExpiredErrorWithAlternatives() {
        // Given
        val expiredError = ErrorTestFixtures.createPromoCodeExpired(
            promoCode = "SUMMER2024",
            daysAgo = 3
        )
        val errorState = PromoCodeState.Error(
            error = expiredError,
            alternativeActions = listOf(
                ErrorAction.ViewAlternatives { /* show alternatives */ },
                ErrorAction.ContactSupport { /* contact support */ }
            )
        )

        composeTestRule.setContent {
            HomeScreen(
                uiState = HomeUiState(promoCodeState = errorState),
                onAction = { }
            )
        }

        // Then
        composeTestRule
            .onNodeWithText("Promo code has expired")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("View Alternatives")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Contact Support")
            .assertIsDisplayed()
    }
}
```

**Deliverables**:
- ✅ Error testing utilities
- ✅ Repository error tests
- ✅ UI error testing
- ✅ Test documentation

### Phase 9: Migration Validation (Week 17)

**Goal**: Comprehensive validation and performance testing

#### 9.1 Migration Validation Scripts (2 days)
```kotlin
// New: tools/src/main/kotlin/ValidationScript.kt
object MigrationValidator {

    fun validateErrorCoverage() {
        println("🔍 Validating error coverage...")

        val allDomainErrors = findAllDomainErrorTypes()
        val handledErrors = findAllErrorHandlingSites()

        val unhandledErrors = allDomainErrors - handledErrors
        if (unhandledErrors.isNotEmpty()) {
            println("❌ Unhandled error types:")
            unhandledErrors.forEach { println("   - $it") }
        } else {
            println("✅ All error types are handled")
        }
    }

    fun validateAnalyticsCoverage() {
        println("📊 Validating analytics coverage...")

        val allErrorTypes = findAllDomainErrorTypes()
        val trackedErrors = findAllAnalyticsCallSites()

        val untrackedErrors = allErrorTypes - trackedErrors
        if (untrackedErrors.isNotEmpty()) {
            println("❌ Untracked error types:")
            untrackedErrors.forEach { println("   - $it") }
        } else {
            println("✅ All errors are being tracked")
        }
    }

    fun validateTestCoverage() {
        println("🧪 Validating test coverage...")

        // Use code coverage tools to ensure error handling paths are tested
        val errorHandlingMethods = findErrorHandlingMethods()
        val testedMethods = findTestedMethods()

        val coveragePercentage = (testedMethods.intersect(errorHandlingMethods).size.toFloat() /
                                 errorHandlingMethods.size * 100).roundToInt()

        println("📈 Error handling test coverage: $coveragePercentage%")

        if (coveragePercentage < 90) {
            println("❌ Test coverage below 90%")
            val untestedMethods = errorHandlingMethods - testedMethods
            untestedMethods.forEach { println("   Missing tests for: $it") }
        } else {
            println("✅ Excellent test coverage")
        }
    }
}
```

#### 9.2 Performance Impact Assessment (3 days)
- Memory usage comparison (old vs new system)
- Error handling latency measurements
- App startup time impact
- Network request overhead analysis

**Deliverables**:
- ✅ Migration validation scripts
- ✅ Performance benchmarks
- ✅ Coverage reports
- ✅ Migration sign-off

### Phase 10: Documentation & Training (Week 18)

**Goal**: Complete developer documentation and training materials

#### 10.1 Developer Guidelines (3 days)
```kotlin
// Update: docs/ERROR_HANDLING_GUIDE.md
# Error Handling Developer Guide

## Quick Start

### 1. Creating Domain Errors
```kotlin
// Define domain-specific errors with rich context
sealed interface UserError : DomainError {
    data class UserNotFound(
        val identifier: String,
        val searchType: UserSearchType
    ) : UserError
}
```

### 2. Repository Implementation
```kotlin
// Return typed Results from repositories
class UserRepository {
    fun getUser(id: UserId): Flow<Result<User, UserError | NetworkError>> {
        return flow {
            try {
                val user = dataSource.getUser(id)
                emit(Result.Success(user))
            } catch (e: Exception) {
                val domainError = errorMapper.mapException(e)
                emit(Result.Error(domainError))
            }
        }
    }
}
```

### 3. ViewModel Error Handling
```kotlin
// Handle typed errors in ViewModels with rich context
when (result) {
    is Result.Success -> handleSuccess(result.data)
    is Result.Error -> when (val error = result.error) {
        is UserError.UserNotFound -> showUserNotFoundDialog(error.identifier)
        is NetworkError.Timeout -> showRetryOption(error.timeoutMs)
        // Compiler ensures exhaustive handling
    }
}
```

## Best Practices
- Always use sealed interfaces for error hierarchies
- Include rich context in error data classes
- Implement exhaustive when expressions for error handling
- Track all errors with analytics
- Test both success and error scenarios
```

#### 10.2 Migration Checklist (2 days)
```markdown
# Error Handling Migration Checklist

## Pre-Migration ✅
- [ ] All team members trained on new error handling patterns
- [ ] Migration validation scripts passing
- [ ] Performance benchmarks baseline established
- [ ] Rollback plan documented and tested

## During Migration
- [ ] Phase 1: Foundation completed and validated
- [ ] Phase 2: Shared domain migrated
- [ ] Phase 3: Data layer updated
- [ ] Phase 4: Use cases migrated
- [ ] Phase 5: UI states enhanced
- [ ] Phase 6: UI components updated
- [ ] Phase 7: Analytics integrated
- [ ] Phase 8: Testing framework complete
- [ ] Phase 9: Migration validated

## Post-Migration ✅
- [ ] All legacy error handling code removed
- [ ] Performance metrics within acceptable range
- [ ] Error analytics dashboard operational
- [ ] Team satisfied with new developer experience
- [ ] Documentation updated and published
```

**Deliverables**:
- ✅ Complete developer guide
- ✅ Migration checklist
- ✅ Training materials
- ✅ Best practices documentation

---

## Migration Strategy

### Approach: Big Bang Migration
Given the requirement for "no half solutions" and "no backward compatibility," we'll perform a comprehensive migration across all modules simultaneously.

### Pre-Migration Requirements
1. **Complete feature freeze** during migration weeks
2. **Comprehensive backup** of current codebase
3. **Dedicated migration branch** for all changes
4. **Team training** on new error handling patterns

### Migration Execution
1. **Week 1-2**: Foundation (Phases 1-3)
2. **Week 3-6**: Core layers (Phases 4-6)
3. **Week 7-10**: UI and components (Phases 7-9)
4. **Week 11-12**: Testing and validation (Phases 10-12)
5. **Week 13-15**: Advanced features (Phases 13-15)
6. **Week 16-18**: Documentation and finalization

### Validation Checkpoints
- **After Phase 3**: Foundation architecture validated
- **After Phase 6**: Data/domain layers functional
- **After Phase 9**: UI fully migrated
- **After Phase 12**: Testing complete
- **After Phase 15**: Ready for production

### Rollback Strategy
If critical issues are discovered:
1. **Immediate**: Revert to backup branch
2. **Analysis**: Identify specific failure points
3. **Targeted fix**: Address issues in isolation
4. **Gradual rollout**: Re-deploy with fixes

---

## Risk Assessment

### High Risk ⚠️
- **Compilation errors**: Massive API changes across all modules
- **Runtime regressions**: New error paths not properly tested
- **Performance degradation**: Additional memory/CPU overhead
- **Team productivity**: Learning curve for new patterns

### Mitigation Strategies
1. **Comprehensive testing**: 90%+ coverage requirement
2. **Performance monitoring**: Before/after benchmarks
3. **Gradual training**: Team workshops before migration
4. **Pair programming**: Knowledge sharing during implementation

### Medium Risk ⚠️
- **Analytics gaps**: Missing error tracking
- **UX changes**: Different error display patterns
- **Third-party integrations**: Firebase error mapping

### Low Risk ✅
- **Code maintainability**: New system is more maintainable
- **Type safety**: Compiler catches most issues
- **Future readiness**: Easy migration to Kotlin 2.4

---

## Success Metrics

### Technical Metrics
- **Compile-time safety**: 100% exhaustive error handling
- **Test coverage**: 90%+ for error scenarios
- **Performance**: <10% overhead vs current system
- **Code quality**: Reduced complexity metrics

### Developer Experience
- **Error handling clarity**: Survey feedback >4.5/5
- **Implementation speed**: Faster error handling implementation
- **Bug reduction**: 50% fewer error-related bugs
- **Debugging efficiency**: Faster error diagnosis

### User Experience
- **Error clarity**: More helpful error messages
- **Recovery options**: Better error recovery flows
- **App stability**: Fewer crashes and unexpected states
- **User satisfaction**: Improved app store ratings

### Business Metrics
- **Error analytics**: 10x more detailed error data
- **Issue resolution**: Faster support ticket resolution
- **Feature velocity**: Faster feature development
- **Technical debt**: Significant reduction in error-handling debt

---

## Future Roadmap

### Kotlin 2.4 Rich Errors Migration
When Kotlin 2.4 Rich Errors become stable:
1. **Direct mapping**: Current sealed interfaces → union types
2. **Syntax update**: `Result<T, E>` → `T | E`
3. **Simplified patterns**: Native language support
4. **Performance boost**: No wrapper types needed

### Advanced Error Handling Features
- **Circuit breakers**: Automatic service degradation
- **Error prediction**: ML-based error forecasting
- **Smart retries**: Context-aware retry strategies
- **Error clustering**: Pattern detection and alerting

---

## Conclusion

This migration represents a fundamental improvement to Qode's error handling architecture. By adopting domain-specific sealed error interfaces, we achieve:

✅ **Type Safety**: Compiler-enforced error handling
✅ **Rich Context**: Detailed error information for better UX
✅ **Better Analytics**: Comprehensive error tracking
✅ **Developer Experience**: Clearer, more maintainable code
✅ **Future Ready**: Easy migration to Kotlin 2.4 Rich Errors

The investment in this migration will pay dividends in:
- Reduced bugs and crashes
- Faster feature development
- Better user experience
- More effective support and debugging
- Stronger competitive position

**This is not just an error handling upgrade—it's a foundation for building more reliable, maintainable, and user-friendly applications.**

---

*Last Updated: ${Clock.System.now().toString()}*
*Document Version: 1.0*
*Status: Ready for Implementation*