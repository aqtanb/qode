# Authentication System

## Overview
Google Sign-In authentication with contextual bottom sheet prompts. The authentication feature serves as a **core service** that other features depend on following proper unidirectional dependency flow.

## Architecture & Dependencies

### ✅ Correct Approach: Unidirectional Dependencies
```
feature:home → feature:auth     (✅ Logical - home needs auth state)
feature:profile → feature:auth  (✅ Logical - profile needs auth state)
feature:promocode → feature:auth (✅ Logical - actions need auth)
```

### ❌ Wrong Approach: Circular Dependencies  
```
feature:auth → feature:home      (❌ Auth shouldn't know about home)
core:ui → feature:auth           (❌ Core can't depend on features)
```

**Key Rule**: Dependencies flow from specific features to generic services, never the reverse.

## Core Components

### 1. Authentication State (MVI Pattern)
```kotlin
// Centralized auth state
sealed interface AuthState {
    data object Loading : AuthState
    data object Unauthenticated : AuthState
    data class Authenticated(val user: User) : AuthState
}

// Feature-level UI state
sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState  
    data class Error(val errorType: ErrorType) : AuthUiState
}
```

## Key Files

### Feature Layer
- `feature/auth/AuthScreen.kt` - Main authentication UI with Google Sign-In button
- `feature/auth/AuthViewModel.kt` - MVI ViewModel handling auth state and actions
- `feature/auth/AuthUiState.kt` - Sealed interface for auth screen state
- `feature/auth/AuthAction.kt` - Sealed interface for user actions
- `feature/auth/AuthEvent.kt` - Events for side effects (SignedIn, Terms, Privacy)
- `feature/auth/navigation/AuthNavigation.kt` - Type-safe navigation with Kotlinx Serialization

### Domain Layer  
- `core/domain/AuthState.kt` - Authentication state sealed interface
- `core/domain/auth/AuthStateManager.kt` - Centralized auth state management
- `core/domain/repository/AuthRepository.kt` - Repository interface
- `core/domain/usecase/auth/SignInWithGoogleUseCase.kt` - Google sign-in business logic
- `core/domain/usecase/auth/SignOutUseCase.kt` - Sign-out business logic
- `core/domain/usecase/auth/GetAuthStateUseCase.kt` - Auth state retrieval

### Data Layer
- `core/data/datasource/FirebaseGoogleAuthService.kt` - Google Sign-In implementation
- `core/data/repository/AuthRepositoryImpl.kt` - Repository implementation
- `core/model/AuthCredentials.kt` - Authentication credential models
- `core/model/User.kt` - User model with profile information

## Contextual Authentication System

### 2. Component Architecture
```
core/ui/component/
├── AuthenticationBottomSheet.kt    # Pure UI + AuthPromptAction enum
└── AuthRequiredContent.kt          # Pure conditional rendering

feature/auth/component/
└── AuthenticationGate.kt           # Business logic orchestration
```

### 3. Usage Pattern
```kotlin
// In any feature module (home, promocode, profile, etc.)
val requireUpvote = requireAuthentication(
    action = AuthPromptAction.UpvotePromoCode,  // Contextual messaging
    onAuthenticated = { upvotePromoCode() }     // Your business logic
)

QodeButton(onClick = requireUpvote) {
    Text("Upvote")  // User sees contextual auth prompt only when needed
}
```

## Key Features

### ✅ Non-Intrusive UX
- **Only shows when needed**: Auth prompt appears when user tries protected action
- **Contextual messaging**: "Sign in to upvote" vs "Sign in to comment"
- **Modern bottom sheet**: Not blocking dialog
- **Easy dismissal**: Clear close button + swipe down

### ✅ Proper Architecture
- **Core UI components**: No feature dependencies  
- **Feature orchestration**: Business logic in feature modules
- **One-tap Google Sign-In**: Uses existing auth flow
- **Unidirectional dependencies**: Features → auth, never reverse

## AuthPromptAction Types

```kotlin
enum class AuthPromptAction {
    SubmitPromoCode,    // "Sign in to submit promo code" 
    UpvotePromoCode,    // "Sign in to upvote"
    DownvotePromoCode,  // "Sign in to downvote"
    WriteComment,       // "Sign in to comment"
    BookmarkPromoCode,  // "Sign in to bookmark"
    FollowStore        // "Sign in to follow store"
}
```

## Quick Integration

### 1. Add dependency
```kotlin
// In feature/yourfeature/build.gradle.kts
dependencies {
    implementation(project(":core:ui"))
    implementation(project(":feature:auth"))
}
```

### 2. Protect your action
```kotlin
import com.qodein.core.ui.component.AuthPromptAction
import com.qodein.feature.auth.component.requireAuthentication

val requireAuth = requireAuthentication(
    action = AuthPromptAction.YourAction,
    onAuthenticated = { /* your logic */ }
)
```

### 3. Use in onClick
```kotlin
QodeButton(onClick = requireAuth) {
    Text("Protected Action")
}
```

## Development Rules

1. **Keep dependencies unidirectional**: Features depend on auth, not reverse
2. **Use contextual actions**: Specific AuthPromptAction for each use case  
3. **No core → feature deps**: Core components stay pure
4. **Follow MVI + Events**: Consistent with app architecture
5. **Test feature integration**: Unit test your requireAuthentication usage
