# Submission Architecture Analysis & Improvement Plan

> **Status**: Planning Phase
> **Priority**: High - Foundation for scalable submission system

## 📋 Overview

This document outlines the comprehensive analysis and improvement plan for the promo code submission architecture. The current implementation follows good MVI patterns but suffers from state composition issues that limit scalability and maintainability.

## 🏗️ Current Architecture Assessment

### ✅ Strengths

#### **Clean MVI Foundation**
- **Action System**: Well-structured `SubmissionWizardAction` sealed interface
- **State Management**: Centralized state with proper flows
- **Event Handling**: Clean event system for navigation and side effects

#### **Authentication Integration**
- Successfully moved authentication logic from UI to ViewModel
- Proper `Result<AuthState>` handling with exhaustive when expressions
- Enterprise-ready authentication flow

#### **Progressive Step Logic**
- Well-designed `ProgressiveStep` enum with validation rules
- Clear step progression with `next()` and `previous()` methods
- Declarative validation via `canProceed(data: SubmissionWizardData)`

#### **Type Safety**
- Extensive use of sealed classes and interfaces
- Compile-time safety for state transitions
- Proper error type modeling

### 🔴 Critical Architecture Issues

#### 1. **Monolithic State Violation**

**Current State Structure**:
```kotlin
data class Success(
    val wizardData: SubmissionWizardData,           // Business data
    val isSubmitting: Boolean,                      // Submission state
    val validationErrors: Map<String, String>,      // Form validation
    val currentProgressiveStep: ProgressiveStep,    // Wizard flow
    val serviceSelectionUiState: ServiceSelectionUiState, // Service UI
    val showServiceSelector: Boolean,               // UI visibility
    val isAuthenticated: Boolean,                   // Auth state
    val authenticatedUser: User?,                   // Auth data
    val showAuthSheet: Boolean,                     // Auth UI
    val isSigningIn: Boolean,                       // Auth operation
    val authError: String?                          // Auth errors
) // 11 properties covering 5 different concerns!
```

**Problems**:
- **Single Responsibility Violation**: One class handles wizard flow, auth, service selection, validation, and submission
- **Testing Complexity**: Must mock entire state for testing individual concerns
- **Maintenance Overhead**: Changes to auth logic require understanding unrelated submission logic

#### 2. **Mixed Abstraction Levels**

State mixes high-level business concepts with low-level UI implementation details:

- **Business Logic**: `wizardData`, `currentProgressiveStep`
- **UI State**: `showServiceSelector`, `showAuthSheet`
- **Infrastructure**: `isSigningIn`, `authError`

This makes the state difficult to reason about and test.

#### 3. **Authentication State Duplication**

```kotlin
// State contains cached auth data
val isAuthenticated: Boolean
val authenticatedUser: User?

// PLUS live subscription to auth changes
getAuthStateUseCase().collect { authResult -> ... }
```

**Risk**: Cached state can become stale, leading to inconsistencies.

#### 4. **Service Selection Feature Leak**

`ServiceSelectionUiState` manages:
- Search queries and results
- Manual entry mode
- Loading states
- Popular services

This is complex enough to be its own feature module.

#### 5. **Inconsistent Error Handling**

Three different error patterns:
- **Form errors**: `Map<String, String>`
- **Auth errors**: `String?`
- **System errors**: `ErrorType`

No unified error handling strategy.

## 🚀 Improvement Plan

### Phase 1: State Composition Refactor

**Goal**: Break monolithic state into focused, composable sub-states

#### 1.1 State Decomposition

**New Architecture**:
```kotlin
data class Success(
    val wizardFlow: WizardFlowState,
    val authentication: AuthenticationState,
    val serviceSelection: ServiceSelectionState,
    val formValidation: ValidationState,
    val submission: SubmissionState
) : SubmissionWizardUiState
```

#### 1.2 Individual State Definitions

**WizardFlowState**:
```kotlin
data class WizardFlowState(
    val wizardData: SubmissionWizardData,
    val currentStep: ProgressiveStep,
    val canGoNext: Boolean,
    val canGoPrevious: Boolean,
    val canSubmit: Boolean
)
```

**AuthenticationState**:
```kotlin
sealed interface AuthenticationState {
    data object Loading : AuthenticationState
    data object Unauthenticated : AuthenticationState
    data class Authenticated(val user: User) : AuthenticationState
    data class Error(val message: String) : AuthenticationState
    data class SigningIn(val user: User? = null) : AuthenticationState
}
```

**ValidationState**:
```kotlin
data class ValidationState(
    val fieldErrors: Map<String, FieldError>,
    val isValid: Boolean
) {
    data class FieldError(
        val field: String,
        val message: String,
        val errorType: ValidationErrorType
    )
}
```

**SubmissionState**:
```kotlin
sealed interface SubmissionState {
    data object Idle : SubmissionState
    data object Submitting : SubmissionState
    data class Success(val promoCodeId: String) : SubmissionState
    data class Error(val error: SubmissionError) : SubmissionState
}
```

#### 1.3 Service Selection Extraction

Create separate feature module:
```
feature/
├── service-selection/
│   ├── ServiceSelectionViewModel.kt
│   ├── ServiceSelectionState.kt
│   ├── ServiceSelectionAction.kt
│   └── ServiceSelectionScreen.kt
```

### Phase 2: Use Case Composition

**Goal**: Replace fat ViewModel with composed, focused use cases

#### 2.1 Domain Use Cases

**ValidateSubmissionFormUseCase**:
```kotlin
class ValidateSubmissionFormUseCase {
    operator fun invoke(data: SubmissionWizardData): ValidationState {
        val errors = mutableMapOf<String, FieldError>()

        // Service validation
        if (data.selectedService == null) {
            errors["service"] = FieldError("service", "Service is required", ValidationErrorType.REQUIRED)
        }

        // Promo code validation
        if (data.promoCode.isBlank()) {
            errors["promoCode"] = FieldError("promoCode", "Promo code is required", ValidationErrorType.REQUIRED)
        }

        // Discount validation
        when (data.promoCodeType) {
            PromoCodeType.PERCENTAGE -> {
                if (data.discountPercentage.isBlank()) {
                    errors["discountPercentage"] = FieldError("discountPercentage", "Percentage is required", ValidationErrorType.REQUIRED)
                }
            }
            PromoCodeType.FIXED_AMOUNT -> {
                if (data.discountAmount.isBlank()) {
                    errors["discountAmount"] = FieldError("discountAmount", "Amount is required", ValidationErrorType.REQUIRED)
                }
            }
            null -> {
                errors["promoCodeType"] = FieldError("promoCodeType", "Discount type is required", ValidationErrorType.REQUIRED)
            }
        }

        return ValidationState(
            fieldErrors = errors,
            isValid = errors.isEmpty()
        )
    }
}
```

**SubmitAuthenticatedPromoCodeUseCase**:
```kotlin
class SubmitAuthenticatedPromoCodeUseCase(
    private val createPromoCodeUseCase: CreatePromoCodeUseCase,
    private val analyticsHelper: AnalyticsHelper
) {
    operator fun invoke(
        data: SubmissionWizardData,
        user: User
    ): Flow<Result<PromoCode>> = flow {
        emit(Result.Loading)

        val promoCodeResult = when (data.promoCodeType) {
            PromoCodeType.PERCENTAGE -> PromoCode.createPercentage(
                code = data.promoCode,
                serviceName = data.serviceName,
                serviceId = data.selectedService?.id,
                discountPercentage = data.discountPercentage.toDoubleOrNull() ?: 0.0,
                description = data.description.takeIf { it.isNotBlank() },
                minimumOrderAmount = data.minimumOrderAmount.toDoubleOrNull() ?: 0.0,
                startDate = data.startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toKotlinInstant(),
                endDate = data.endDate!!.atStartOfDay(ZoneId.systemDefault()).toInstant().toKotlinInstant(),
                isFirstUserOnly = data.isFirstUserOnly,
                createdBy = user.id,
                createdByUsername = user.profile.displayName,
                createdByAvatarUrl = user.profile.photoUrl,
                serviceLogoUrl = data.selectedService?.logoUrl,
                targetCountries = listOf("KZ"),
                category = data.selectedService?.category ?: "Unspecified"
            )
            // ... similar for FIXED_AMOUNT
            null -> throw IllegalStateException("Promo code type is required")
        }

        promoCodeResult.fold(
            onSuccess = { promoCode ->
                createPromoCodeUseCase(promoCode).collect { result ->
                    when (result) {
                        is Result.Success -> {
                            analyticsHelper.logPromoCodeSubmission(
                                promocodeId = result.data.id.value,
                                promocodeType = when (promoCode) {
                                    is PromoCode.PercentagePromoCode -> "percentage"
                                    is PromoCode.FixedAmountPromoCode -> "fixed_amount"
                                },
                                success = true
                            )
                            emit(Result.Success(result.data))
                        }
                        is Result.Error -> {
                            analyticsHelper.logPromoCodeSubmission(
                                promocodeId = "unknown",
                                promocodeType = when (promoCode) {
                                    is PromoCode.PercentagePromoCode -> "percentage"
                                    is PromoCode.FixedAmountPromoCode -> "fixed_amount"
                                },
                                success = false
                            )
                            emit(Result.Error(result.exception))
                        }
                        is Result.Loading -> emit(Result.Loading)
                    }
                }
            },
            onFailure = { exception ->
                emit(Result.Error(exception))
            }
        )
    }.asResult()
}
```

**ProgressWizardStepsUseCase**:
```kotlin
class ProgressWizardStepsUseCase(
    private val validateFormUseCase: ValidateSubmissionFormUseCase
) {
    fun canGoNext(step: ProgressiveStep, data: SubmissionWizardData): Boolean {
        return step.canProceed(data)
    }

    fun canGoPrevious(step: ProgressiveStep): Boolean {
        return !step.isFirst
    }

    fun canSubmit(step: ProgressiveStep, data: SubmissionWizardData): Boolean {
        return step.isLast && validateFormUseCase(data).isValid
    }

    fun nextStep(currentStep: ProgressiveStep): ProgressiveStep? {
        return currentStep.next()
    }

    fun previousStep(currentStep: ProgressiveStep): ProgressiveStep? {
        return currentStep.previous()
    }
}
```

### Phase 3: Error Handling Unification

**Goal**: Consistent, type-safe error handling across all operations

#### 3.1 Unified Error Hierarchy

```kotlin
sealed interface SubmissionError {
    data class ValidationError(
        val field: String,
        val message: String,
        val errorType: ValidationErrorType
    ) : SubmissionError

    data class AuthenticationError(
        val type: AuthErrorType,
        val message: String
    ) : SubmissionError

    data class BusinessRuleError(
        val rule: String,
        val context: String,
        val suggestion: String? = null
    ) : SubmissionError

    data class NetworkError(
        val exception: Throwable,
        val isRetryable: Boolean
    ) : SubmissionError

    data class SystemError(
        val errorCode: String,
        val message: String
    ) : SubmissionError
}

enum class ValidationErrorType {
    REQUIRED,
    INVALID_FORMAT,
    OUT_OF_RANGE,
    DUPLICATE,
    BUSINESS_RULE
}

enum class AuthErrorType {
    SIGN_IN_FAILED,
    TOKEN_EXPIRED,
    INSUFFICIENT_PERMISSIONS,
    NETWORK_ERROR
}
```

#### 3.2 Error Mapping Extensions

```kotlin
fun Throwable.toSubmissionError(): SubmissionError {
    return when (this) {
        is SecurityException -> SubmissionError.AuthenticationError(
            type = AuthErrorType.INSUFFICIENT_PERMISSIONS,
            message = this.message ?: "Authentication failed"
        )
        is IOException -> SubmissionError.NetworkError(
            exception = this,
            isRetryable = true
        )
        else -> SubmissionError.SystemError(
            errorCode = this.javaClass.simpleName,
            message = this.message ?: "Unknown error occurred"
        )
    }
}

fun ValidationState.FieldError.toSubmissionError(): SubmissionError.ValidationError {
    return SubmissionError.ValidationError(
        field = this.field,
        message = this.message,
        errorType = this.errorType
    )
}
```

### Phase 4: State Machine Implementation

**Goal**: Explicit, declarative state transitions

#### 4.1 Wizard Flow State Machine

```kotlin
class WizardFlowStateMachine(
    private val progressStepsUseCase: ProgressWizardStepsUseCase
) {
    fun transition(
        currentState: WizardFlowState,
        action: WizardFlowAction
    ): WizardFlowState {
        return when (action) {
            WizardFlowAction.NextStep -> {
                val nextStep = progressStepsUseCase.nextStep(currentState.currentStep)
                if (nextStep != null && currentState.canGoNext) {
                    currentState.copy(
                        currentStep = nextStep,
                        canGoNext = progressStepsUseCase.canGoNext(nextStep, currentState.wizardData),
                        canGoPrevious = progressStepsUseCase.canGoPrevious(nextStep),
                        canSubmit = progressStepsUseCase.canSubmit(nextStep, currentState.wizardData)
                    )
                } else currentState
            }

            WizardFlowAction.PreviousStep -> {
                val previousStep = progressStepsUseCase.previousStep(currentState.currentStep)
                if (previousStep != null && currentState.canGoPrevious) {
                    currentState.copy(
                        currentStep = previousStep,
                        canGoNext = progressStepsUseCase.canGoNext(previousStep, currentState.wizardData),
                        canGoPrevious = progressStepsUseCase.canGoPrevious(previousStep),
                        canSubmit = progressStepsUseCase.canSubmit(previousStep, currentState.wizardData)
                    )
                } else currentState
            }

            is WizardFlowAction.UpdateData -> {
                val newData = action.update(currentState.wizardData)
                currentState.copy(
                    wizardData = newData,
                    canGoNext = progressStepsUseCase.canGoNext(currentState.currentStep, newData),
                    canSubmit = progressStepsUseCase.canSubmit(currentState.currentStep, newData)
                )
            }
        }
    }
}

sealed interface WizardFlowAction {
    data object NextStep : WizardFlowAction
    data object PreviousStep : WizardFlowAction
    data class UpdateData(val update: (SubmissionWizardData) -> SubmissionWizardData) : WizardFlowAction
}
```

## 📊 Expected Benefits

### **Maintainability**
- **70% smaller** individual state objects
- **Single responsibility** principle adherence
- **Independent evolution** of each concern
- **Clearer testing** with focused state objects

### **Testability**
```kotlin
// Before: Testing auth requires full submission state setup
@Test
fun `test auth flow`() {
    val fullState = SubmissionWizardUiState.Success(
        wizardData = SubmissionWizardData(/* complex setup */),
        isSubmitting = false,
        validationErrors = emptyMap(),
        currentProgressiveStep = ProgressiveStep.SERVICE,
        serviceSelectionUiState = ServiceSelectionUiState.Default,
        showServiceSelector = false,
        isAuthenticated = false, // Only this matters for auth test!
        authenticatedUser = null,
        showAuthSheet = true,
        isSigningIn = false,
        authError = null
    )
    // Test auth logic...
}

// After: Direct auth state testing
@Test
fun `test auth flow`() {
    val authState = AuthenticationState.Unauthenticated
    // Test auth logic directly!
}
```

### **Scalability**
- **Reusable** service selection across multiple features
- **Composable** use cases for different submission workflows
- **Extensible** error handling for new error types
- **Independent** feature development

### **Developer Experience**
- **Focused** debugging with isolated concerns
- **Faster** development with single-responsibility components
- **Clearer** code organization and navigation
- **Reduced** cognitive load when working on specific features

## 🎖️ Architecture Maturity Assessment

| Aspect | Before | After Phase 1-2 | After Phase 3-4 |
|--------|--------|------------------|------------------|
| **State Management** | Monolithic | Composed | State Machine |
| **Error Handling** | Inconsistent | Unified | Type-Safe |
| **Testability** | Complex Setup | Focused Tests | Unit + Integration |
| **Maintainability** | Medium | High | Excellent |
| **Scalability** | Limited | Good | Enterprise-Ready |

## 🛠️ Implementation Guidelines

### **Migration Strategy**
1. **Phase 1**: Implement new state structures alongside existing (feature flags)
2. **Phase 2**: Migrate ViewModel to use new use cases
3. **Phase 3**: Update UI components to new state structure
4. **Phase 4**: Remove old state implementation

### **Testing Strategy**
- **Unit Tests**: Individual use cases and state transitions
- **Integration Tests**: Full wizard flow with composed states
- **UI Tests**: Screen behavior with new state structure

### **Performance Considerations**
- **State Updates**: Composed states may create more intermediate objects
- **Memory**: Multiple state objects vs single large object
- **Observation**: Selective state observation for UI optimization

## 📚 Related Documentation

- `docs/AUTHENTICATION.md` - Authentication system overview
- `docs/DEVELOPMENT_ROADMAP.md` - Implementation timeline
- `feature/promocode/README.md` - Feature-specific architecture
- `docs/TESTING.md` - Testing strategy and guidelines

---

**Next Steps**: Begin Phase 1 implementation with WizardFlowState creation and state decomposition.