# Authentication System

## Overview
Google Sign-In authentication system with Firebase integration, following MVI architecture pattern with Events for user interactions.

## Architecture
- **MVI Pattern**: AuthViewModel exposes AuthUiState via StateFlow, handles AuthAction sealed classes
- **Events System**: AuthEvent for side effects (navigation, external actions)
- **State Management**: AuthState managed in domain layer with AuthStateManager
- **Firebase Integration**: Google Auth Service with Firebase Authentication

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
- `core/data/datasource/GoogleAuthService.kt` - Google Sign-In implementation
- `core/data/repository/AuthRepositoryImpl.kt` - Repository implementation
- `core/model/AuthCredentials.kt` - Authentication credential models
- `core/model/User.kt` - User model with profile information

## Authentication Flow

### 1. AuthState Management
```kotlin
sealed interface AuthState {
    data object Loading : AuthState
    data object Unauthenticated : AuthState
    data class Authenticated(val user: User) : AuthState
    data class Error(val exception: Throwable) : AuthState
}
```

### 2. MVI Pattern
```kotlin
// Actions
sealed interface AuthAction {
    data object SignInWithGoogle : AuthAction
    data object SignOut : AuthAction
    data object DismissError : AuthAction
}

// UI State  
sealed interface AuthUiState {
    data object Loading : AuthUiState
    data object SignedOut : AuthUiState
    data class SignedIn(val user: User) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

// Events
sealed interface AuthEvent {
    data object SignedIn : AuthEvent
    data object TermsOfServiceRequested : AuthEvent
    data object PrivacyPolicyRequested : AuthEvent
}
```

### 3. ViewModel Implementation
```kotlin
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val getAuthStateUseCase: GetAuthStateUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    private val _events = MutableSharedFlow<AuthEvent>()
    val events = _events.asSharedFlow()
    
    fun onAction(action: AuthAction) { /* Handle actions */ }
}
```

## Google Sign-In Integration

### Configuration
- Firebase project configuration with google-services.json
- Google Sign-In client setup in GoogleAuthService
- Proper OAuth 2.0 scopes configuration

### Implementation
```kotlin
class GoogleAuthService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun signIn(): Result<AuthCredentials> { 
        // Google Sign-In implementation
    }
}
```

## Navigation Integration

### Type-Safe Navigation
```kotlin
@Serializable object AuthBaseRoute
@Serializable object AuthRoute

fun NavController.navigateToAuth(navOptions: NavOptions? = null) {
    navigate(route = AuthRoute, navOptions = navOptions)
}

fun NavGraphBuilder.authSection(
    onAuthSuccess: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    navigation<AuthBaseRoute>(startDestination = AuthRoute) {
        composable<AuthRoute> {
            AuthScreen(onNavigateToHome = onAuthSuccess)
        }
    }
}
```

### Event Handling
```kotlin
// In AuthScreen
LaunchedEffect(events) {
    when (events) {
        AuthEvent.SignedIn -> onNavigateToHome()
        AuthEvent.TermsOfServiceRequested -> { /* Open terms */ }
        AuthEvent.PrivacyPolicyRequested -> { /* Open privacy */ }
    }
}
```

## UI Implementation

### AuthScreen Features
- **Transparent top bar** for gradient integration
- **Google Sign-In button** with loading states
- **Error handling** with retry mechanisms
- **Terms and Privacy links** with proper events
- **Responsive design** with design system components

### Screen Structure
```kotlin
@Composable
fun AuthScreen(
    onNavigateToHome: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle(initialValue = null)
    
    // Handle events for navigation
    LaunchedEffect(events) { /* Event handling */ }
    
    // UI implementation with QodeTheme and design system components
}
```

## Testing

### Unit Tests
- `AuthViewModelTest.kt` - ViewModel behavior testing with MockK
- `AuthStateManagerTest.kt` - State management testing
- `GetAuthStateUseCaseTest.kt` - Use case testing

### Integration Tests
- `AuthScreenTest.kt` - UI testing with Compose Test
- Firebase Auth integration testing

## Error Handling

### Repository Layer
```kotlin
class AuthRepositoryImpl : AuthRepository {
    override suspend fun signInWithGoogle(): Flow<Result<User>> = flow {
        try {
            val result = googleAuthService.signIn()
            emit(Result.success(result))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
```

### UI Layer
- Error states displayed with QodeCard components
- Retry mechanisms for failed sign-in attempts
- Graceful handling of user cancellation

## Security Considerations

- **No credentials stored locally** - Firebase handles token management
- **Proper OAuth 2.0 flow** - Server-side token validation
- **User data protection** - Minimal user data collection
- **Secure communication** - HTTPS only for auth endpoints

## Development Guidelines

1. **Use Events for side effects** - Navigation and external actions
2. **Follow MVI pattern** - Unidirectional data flow
3. **Handle all error states** - Proper error UI and recovery
4. **Use design system** - Consistent UI with QodeButton, QodeCard
5. **Test thoroughly** - Unit and integration tests for auth flow
6. **Transparent top bars** - Consistent with app design

This authentication system provides secure, user-friendly Google Sign-In with proper state management and error handling.