# Testing Framework

## Overview
Comprehensive testing framework following enterprise patterns with shared testing infrastructure, MockK for mocking, Turbine for Flow testing, and comprehensive UI testing with Compose Test. Features test utilities, data providers, and consistent testing patterns across all modules.

## Architecture
- **Shared Testing Module**: `core/testing` with common utilities and test data
- **Unit Testing**: ViewModel and business logic testing with MockK and Turbine
- **UI Testing**: Compose UI testing with semantic testing and accessibility
- **Integration Testing**: End-to-end flow testing and repository testing
- **Test Utilities**: Custom extensions and helpers for common test patterns
- **Consistent Patterns**: Standardized testing approaches across features

## Key Files

### Core Testing Infrastructure
- `core/testing/rule/MainDispatcherRule.kt` - Coroutine testing setup following NIA patterns
- `core/testing/util/TestExtensions.kt` - Common test utilities and extensions
- `core/testing/data/TestUsers.kt` - Shared test data for consistent testing

### Unit Tests
- `feature/auth/src/test/java/com/qodein/feature/auth/AuthViewModelTest.kt` - Comprehensive ViewModel testing
- `feature/profile/src/test/java/com/qodein/feature/profile/ProfileViewModelTest.kt` - Profile state management testing
- `core/domain/src/test/java/com/qodein/core/domain/usecase/auth/GetAuthStateUseCaseTest.kt` - Use case testing
- `core/domain/src/test/java/com/qodein/core/domain/auth/AuthStateManagerTest.kt` - Business logic testing

### UI Tests
- `feature/auth/src/androidTest/java/com/qodein/feature/auth/AuthScreenTest.kt` - Auth UI testing
- `feature/profile/src/androidTest/java/com/qodein/feature/profile/ProfileScreenTest.kt` - Profile UI testing

### Navigation Tests
- `app/src/test/java/com/qodein/qode/navigation/NavigationHandlerTest.kt` - Navigation logic testing

## Core Testing Infrastructure

### MainDispatcherRule
```kotlin
/**
 * JUnit TestRule that swaps the background executor used by the Architecture Components
 * with a different one which executes each task synchronously.
 * 
 * Based on Now in Android (nowinandroid) testing patterns
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()) : TestWatcher() {
    
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }
    
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
```

### Usage Pattern
```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class MyViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    @Test
    fun myTest() = runTest {
        // Test implementation
    }
}
```

### Test Extensions
```kotlin
/**
 * Test a Flow emission with a more concise syntax
 */
suspend fun <T> Flow<T>.testFlow(validate: suspend TurbineTestContext<T>.() -> Unit) {
    test {
        validate()
    }
}

/**
 * Assert that a state is of a specific type with better error messages
 */
inline fun <reified T> Any?.assertIsType(): T =
    when (this) {
        is T -> this
        null -> throw AssertionError("Expected ${T::class.simpleName} but was null")
        else -> throw AssertionError("Expected ${T::class.simpleName} but was ${this::class.simpleName}")
    }

/**
 * Run test with better naming for readability
 */
fun runViewModelTest(testBody: suspend () -> Unit) =
    runTest {
        testBody()
    }
```

## Test Data Management

### Shared Test Users
```kotlin
object TestUsers {
    /**
     * Standard test user - use this for most tests
     */
    val sampleUser = User(
        id = UserId("test_user_id"),
        email = Email("john.doe@example.com"),
        profile = UserProfile(
            username = "johndoe",
            firstName = "John",
            lastName = "Doe",
            bio = "Test user for unit testing",
            photoUrl = "https://example.com/profile.jpg",
            // ... other fields
        ),
        stats = UserStats.initial(UserId("test_user_id")),
        preferences = UserPreferences.default(UserId("test_user_id")),
    )
    
    /**
     * Power user with high stats - use for stats-related tests
     */
    val powerUser = User(
        id = UserId("power_user_id"),
        // ... with high stats
        stats = UserStats(
            userId = UserId("power_user_id"),
            submittedCodes = 50,
            upvotes = 125,
            downvotes = 15,
            // ... other stats
        ),
        // ... other fields
    )
    
    /**
     * New user with minimal data - use for edge case testing
     */
    val newUser = User(
        // ... minimal profile data with nulls
        profile = UserProfile(
            bio = null, // No bio set yet
            photoUrl = null, // No profile photo
            isGenerated = true, // Generated profile
            // ... other fields
        ),
        // ... other fields
    )
    
    /**
     * Create a user with custom ID - useful for parameterized tests
     */
    fun createUser(
        id: String = "test_user_${System.currentTimeMillis()}",
        email: String = "test@example.com",
        username: String = "testuser",
        firstName: String = "Test",
        lastName: String = "User"
    ): User
}
```

### Test Data Features
- **Standard Test User**: Default user for most test scenarios
- **Power User**: High-activity user for statistics testing
- **New User**: Minimal data user for edge case testing
- **Dynamic Creation**: Factory method for parameterized tests
- **Consistent Data**: Same test data across all modules

## ViewModel Testing Patterns

### AuthViewModel Testing
```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    private lateinit var signInWithGoogleUseCase: SignInWithGoogleUseCase
    private lateinit var viewModel: AuthViewModel
    private lateinit var testDispatcher: TestDispatcher
    
    private val testUser = TestUsers.sampleUser
    
    @Before
    fun setUp() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        signInWithGoogleUseCase = mockk()
        viewModel = AuthViewModel(signInWithGoogleUseCase)
    }
    
    @Test
    fun signInWithGoogle_whenSuccessful_emitsLoadingThenSuccessState() =
        runTest(testDispatcher) {
            // Given
            every { signInWithGoogleUseCase() } returns flowOf(Result.success(testUser))
            
            // When & Then
            viewModel.state.test {
                // Initial state
                val initialState = awaitItem()
                assertTrue(initialState is AuthUiState.Idle)
                
                // Trigger sign in
                viewModel.handleAction(AuthAction.SignInWithGoogleClicked)
                advanceUntilIdle()
                
                // Loading state
                val loadingState = awaitItem()
                assertTrue(loadingState is AuthUiState.Loading)
                
                // Success state
                val successState = awaitItem()
                assertTrue(successState is AuthUiState.Success)
                assertEquals(testUser, (successState as AuthUiState.Success).user)
            }
        }
}
```

### Testing Patterns
- **State Transitions**: Test complete state flows with Turbine
- **Error Handling**: Test different exception types and retry behavior
- **Event Emissions**: Verify navigation and side effect events
- **Job Management**: Test coroutine cancellation and cleanup
- **Edge Cases**: Test rapid actions and error recovery

### Error Categorization Testing
```kotlin
@Test
fun signInWithGoogle_whenSecurityException_emitsNonRetryableError() =
    runTest(testDispatcher) {
        // Given
        val securityException = SecurityException("Sign-in was cancelled or rejected")
        every { signInWithGoogleUseCase() } returns flowOf(Result.failure(securityException))
        
        // When & Then
        viewModel.state.test {
            awaitItem() // Skip initial state
            
            viewModel.handleAction(AuthAction.SignInWithGoogleClicked)
            advanceUntilIdle()
            
            awaitItem() // Skip loading state
            
            // Error state
            val errorState = awaitItem()
            assertTrue(errorState is AuthUiState.Error)
            assertEquals(securityException, (errorState as AuthUiState.Error).exception)
            assertEquals(false, errorState.isRetryable) // SecurityException is not retryable
        }
    }

@Test
fun signInWithGoogle_whenIOException_emitsRetryableError() =
    runTest(testDispatcher) {
        // Given
        val ioException = IOException("Network error")
        every { signInWithGoogleUseCase() } returns flowOf(Result.failure(ioException))
        
        // When & Then
        viewModel.state.test {
            // Test implementation - IOException is retryable
            assertEquals(true, errorState.isRetryable)
        }
    }
```

## Flow Testing with Turbine

### Turbine Integration
```kotlin
// Test Flow emissions with Turbine
viewModel.state.test {
    val initialState = awaitItem()
    assertTrue(initialState is LoadingState)
    
    // Trigger action
    viewModel.performAction()
    
    val successState = awaitItem()
    assertTrue(successState is SuccessState)
}

// Test multiple flows in parallel
@Test
fun stateTransition_idleToLoadingToSuccessWithEvent() =
    runTest(testDispatcher) {
        // Given
        every { signInWithGoogleUseCase() } returns flowOf(Result.success(testUser))
        
        // Test both state and events in parallel
        viewModel.state.test {
            val initialState = awaitItem()
            assertTrue(initialState is AuthUiState.Idle)
            
            // Start testing events in parallel
            val eventTurbine = viewModel.events.testIn(this@runTest)
            
            viewModel.handleAction(AuthAction.SignInWithGoogleClicked)
            advanceUntilIdle()
            
            // State transitions
            val loadingState = awaitItem()
            assertTrue(loadingState is AuthUiState.Loading)
            
            val successState = awaitItem()
            assertTrue(successState is AuthUiState.Success)
            
            // Verify event was emitted
            val event = eventTurbine.awaitItem()
            assertTrue(event is AuthEvent.SignedIn)
            
            eventTurbine.cancel()
        }
    }
```

### Turbine Features
- **Flow Testing**: Test StateFlow and SharedFlow emissions
- **Parallel Testing**: Test multiple flows simultaneously
- **Timeout Handling**: Built-in timeout for flow testing
- **Cancellation**: Proper cleanup of test flows

## UI Testing with Compose Test

### AuthScreen UI Testing
```kotlin
@Test
fun authScreen_whenSignInClicked_callsOnSignInWithGoogle() {
    composeTestRule.setContent {
        QodeTheme {
            AuthScreen(
                onSignInWithGoogle = { onSignInWithGoogleCalled = true },
                onNavigateToTermsOfService = { },
                onNavigateToPrivacyPolicy = { }
            )
        }
    }
    
    composeTestRule
        .onNodeWithText("Sign in with Google")
        .performClick()
        
    assertTrue(onSignInWithGoogleCalled)
}

@Test
fun authScreen_whenErrorState_showsRetryButton() {
    composeTestRule.setContent {
        QodeTheme {
            AuthScreen(
                // ... callbacks
            )
        }
    }
    
    // Set error state through ViewModel or state parameter
    
    composeTestRule
        .onNodeWithText("Retry")
        .assertIsDisplayed()
}
```

### Semantic Testing
```kotlin
@Test
fun authScreen_hasCorrectSemantics() {
    composeTestRule.setContent {
        QodeTheme {
            AuthScreen(/* ... */)
        }
    }
    
    // Test accessibility
    composeTestRule
        .onNodeWithContentDescription("Sign in with Google")
        .assertIsDisplayed()
        
    // Test test tags
    composeTestRule
        .onNodeWithTag("sign_in_button")
        .assertIsDisplayed()
        .assertIsEnabled()
}
```

### UI Testing Features
- **Semantic Testing**: Test content descriptions and accessibility
- **Interaction Testing**: Test user interactions and callbacks
- **State Testing**: Test UI state changes and visibility
- **Layout Testing**: Test responsive layout and positioning

## MockK Testing Patterns

### Mocking Use Cases
```kotlin
private lateinit var getAuthStateUseCase: GetAuthStateUseCase
private lateinit var signOutUseCase: SignOutUseCase

@Before
fun setUp() {
    getAuthStateUseCase = mockk()
    signOutUseCase = mockk()
}

// Success scenario
every { getAuthStateUseCase() } returns flowOf(Result.success(AuthState.Authenticated(testUser)))

// Error scenario
every { getAuthStateUseCase() } returns flowOf(Result.failure(IOException("Network error")))

// Multiple return values
every { signInWithGoogleUseCase() } returnsMany listOf(
    flowOf(Result.failure(exception)),
    flowOf(Result.success(testUser)),
)

// Verification
verify { signInWithGoogleUseCase() }
verify(exactly = 2) { signInWithGoogleUseCase() }
verify(atLeast = 1) { signInWithGoogleUseCase() }
```

### MockK Features
- **Flow Mocking**: Mock Flow-based use cases and repositories
- **Result Mocking**: Mock Result patterns for error handling
- **Verification**: Verify method calls and invocation counts
- **Flexible Returns**: Multiple return values for different scenarios

## Integration Testing

### Repository Testing
```kotlin
@Test
fun authRepository_whenSignInSuccessful_returnsUser() = runTest {
    // Given
    val mockGoogleAuthService = mockk<GoogleAuthService>()
    val repository = AuthRepositoryImpl(mockGoogleAuthService)
    
    every { mockGoogleAuthService.signIn() } returns flowOf(Result.success(authCredentials))
    
    // When
    val result = repository.signInWithGoogle().first()
    
    // Then
    assertTrue(result.isSuccess)
    assertEquals(testUser, result.getOrNull())
}
```

### End-to-End Testing
```kotlin
@Test
fun authFlow_completeSignInFlow_navigatesToHome() = runTest {
    // Test complete authentication flow from UI to navigation
    composeTestRule.setContent {
        QodeApp() // Full app composition
    }
    
    // Navigate to auth
    composeTestRule
        .onNodeWithText("Sign In")
        .performClick()
    
    // Perform sign in
    composeTestRule
        .onNodeWithText("Sign in with Google")
        .performClick()
    
    // Verify navigation to home
    composeTestRule
        .onNodeWithTag("home_screen")
        .assertIsDisplayed()
}
```

## Preview Testing

### Preview Parameter Providers
```kotlin
class UserPreviewParameterProvider : PreviewParameterProvider<User> {
    override val values: Sequence<User> = sequenceOf(
        TestUsers.sampleUser,
        TestUsers.powerUser,
        TestUsers.newUser
    )
}

@Preview
@Composable
fun ProfileHeaderPreview(@PreviewParameter(UserPreviewParameterProvider::class) user: User) {
    QodeTheme {
        ProfileHeader(user = user, onAction = {})
    }
}
```

### Preview Testing Features
- **Parameter Providers**: Test multiple data scenarios in previews
- **State Testing**: Test different UI states in previews
- **Theme Testing**: Test light/dark theme variations
- **Device Testing**: Test different screen sizes and orientations

## Performance Testing

### Memory Testing
```kotlin
@Test
fun viewModel_doesNotLeakMemory() {
    // Test ViewModel cleanup and memory leaks
    val viewModel = AuthViewModel(signInWithGoogleUseCase)
    
    // Perform operations
    viewModel.handleAction(AuthAction.SignInWithGoogleClicked)
    
    // Verify cleanup
    viewModel.onCleared()
    
    // Memory leak detection would go here
}
```

### Animation Testing
```kotlin
@Test
fun profileScreen_animationsPerformSmoothly() {
    composeTestRule.setContent {
        QodeTheme {
            ProfileScreen(/* ... */)
        }
    }
    
    // Test animation performance and smoothness
    composeTestRule
        .onNodeWithTag("profile_header")
        .assertIsDisplayed()
    
    // Advance time to test animations
    composeTestRule.mainClock.advanceTimeBy(1000)
    
    // Verify final animation state
}
```

## Test Organization

### Test Categories
```kotlin
// Unit Tests
class AuthViewModelTest // ViewModel logic testing
class GetAuthStateUseCaseTest // Use case testing
class AuthRepositoryImplTest // Repository testing

// Integration Tests
class AuthIntegrationTest // Multi-layer integration testing
class NavigationIntegrationTest // Navigation flow testing

// UI Tests
class AuthScreenTest // Compose UI testing
class ProfileScreenTest // Screen-level UI testing

// End-to-End Tests
class AuthFlowTest // Complete user flow testing
```

### Test Naming Convention
```kotlin
// Pattern: methodName_whenCondition_expectedResult
@Test
fun signInWithGoogle_whenSuccessful_emitsLoadingThenSuccessState()

@Test
fun handleAction_whenRetryClicked_startsSignInFlow()

@Test
fun authScreen_whenErrorState_showsRetryButton()
```

## Coverage and Quality

### Test Coverage Goals
- **Unit Tests**: 90%+ coverage for ViewModels and business logic
- **Integration Tests**: Critical user flows and data layer
- **UI Tests**: Key user interactions and accessibility
- **E2E Tests**: Core app functionality and navigation

### Quality Metrics
- **State Testing**: All UI states covered (loading, success, error, empty)
- **Error Handling**: All exception types and recovery scenarios
- **Edge Cases**: Boundary conditions and error states
- **Accessibility**: Screen readers and semantic markup

## Development Guidelines

### Testing Best Practices
1. **Arrange-Act-Assert**: Clear test structure with given-when-then
2. **Single Responsibility**: One assertion per test method
3. **Descriptive Names**: Test names describe the scenario and expectation
4. **Consistent Data**: Use shared test data from TestUsers
5. **Mock Externals**: Mock external dependencies and services
6. **Test Behavior**: Test behavior, not implementation details
7. **Fast Tests**: Unit tests should run quickly and independently
8. **Reliable Tests**: Tests should be deterministic and not flaky

### Testing Anti-Patterns to Avoid
- **Over-Mocking**: Don't mock everything, test real collaborations when possible
- **Implementation Testing**: Don't test private methods or implementation details
- **Brittle Tests**: Don't rely on specific timing or order unless necessary
- **Shared State**: Avoid shared mutable state between tests
- **Complex Setup**: Keep test setup simple and focused

### Test-Driven Development
1. **Red**: Write a failing test first
2. **Green**: Write minimal code to make test pass
3. **Refactor**: Improve code while keeping tests green
4. **Repeat**: Continue cycle for new functionality

This testing framework provides comprehensive coverage with modern Android testing patterns, ensuring code quality and reliability across all features of the application.