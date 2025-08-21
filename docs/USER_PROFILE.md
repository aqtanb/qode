# User Profile System

## Overview
Sophisticated user profile system with animated UI components, comprehensive user statistics tracking, and seamless authentication integration. Features gradient-based design, auto-hiding navigation, and comprehensive state management following MVI patterns with Events.

## Architecture
- **MVI Pattern**: ProfileViewModel with ProfileAction sealed classes and ProfileEvent for navigation
- **Authentication Integration**: Real-time auth state monitoring with smart error handling
- **Animated Components**: Spring-based animations with staggered entrance effects
- **Statistics Tracking**: User stats with animated counters and visual feedback
- **Transparent Design**: QodeHeroGradient with glassmorphism effects
- **Responsive Layout**: Auto-hiding top app bar and scroll-aware design

## Key Files

### Feature Layer
- `feature/profile/ProfileScreen.kt` - Main profile UI with animated components
- `feature/profile/ProfileViewModel.kt` - Auth state monitoring and profile logic
- `feature/profile/ProfileUiState.kt` - Profile-specific UI state management
- `feature/profile/ProfileAction.kt` - User interaction actions
- `feature/profile/ProfileEvent.kt` - Navigation and side effect events
- `feature/profile/navigation/ProfileNavigation.kt` - Type-safe navigation integration

### Core Models
- `core/model/User.kt` - User model with profile, stats, and preferences
- `core/model/UserProfile.kt` - User profile information and bio
- `core/model/UserStats.kt` - User statistics and achievements
- `core/model/UserPreferences.kt` - User preferences and settings

### Business Logic
- `core/domain/usecase/auth/GetAuthStateUseCase.kt` - Authentication state monitoring
- `core/domain/usecase/auth/SignOutUseCase.kt` - Sign-out business logic

## Profile Architecture

### UI State Management
```kotlin
sealed interface ProfileUiState {
    data class Success(val user: User) : ProfileUiState
    data object Loading : ProfileUiState
    data class Error(val exception: Throwable, val isRetryable: Boolean = true) : ProfileUiState
}
```

### Action System
```kotlin
sealed interface ProfileAction {
    data object SignOutClicked : ProfileAction
    data object RetryClicked : ProfileAction
    data object EditProfileClicked : ProfileAction
    data object AchievementsClicked : ProfileAction
    data object UserJourneyClicked : ProfileAction
}
```

### Event System
```kotlin
sealed interface ProfileEvent {
    data object EditProfileRequested : ProfileEvent
    data object SignedOut : ProfileEvent
    data object AchievementsRequested : ProfileEvent
    data object UserJourneyRequested : ProfileEvent
}
```

## Authentication Integration

### Real-Time Auth State Monitoring
```kotlin
private fun checkAuthState() {
    authJob?.cancel()
    _state.value = ProfileUiState.Loading
    
    authJob = getAuthStateUseCase()
        .onEach { result ->
            _state.value = result.fold(
                onSuccess = { authState ->
                    when (authState) {
                        is AuthState.Loading -> ProfileUiState.Loading
                        is AuthState.Authenticated -> ProfileUiState.Success(user = authState.user)
                        is AuthState.Unauthenticated -> {
                            // Smart routing should prevent this state
                            ProfileUiState.Error(
                                exception = IllegalStateException("User not authenticated"),
                                isRetryable = true,
                            )
                        }
                    }
                },
                onFailure = { exception ->
                    ProfileUiState.Error(exception = exception, isRetryable = true)
                }
            )
        }
        .launchIn(viewModelScope)
}
```

### Smart Sign-Out Logic
```kotlin
private fun signOut() {
    authJob?.cancel()
    _state.value = ProfileUiState.Loading
    
    signOutUseCase()
        .onEach { result ->
            result.fold(
                onSuccess = {
                    // Only navigate after successful sign out
                    emitEvent(ProfileEvent.SignedOut)
                },
                onFailure = { exception ->
                    // Restart auth monitoring if sign out fails
                    checkAuthState()
                    _state.value = ProfileUiState.Error(exception = exception, isRetryable = true)
                }
            )
        }
        .launchIn(viewModelScope)
}
```

### Authentication Features
- **Real-Time Monitoring**: Continuous auth state observation
- **Smart Error Recovery**: Automatic retry on auth failures
- **Graceful Sign-Out**: Proper cleanup and navigation
- **State Consistency**: Auth state synchronized with profile state

## User Model System

### User Data Structure
```kotlin
data class User(
    val id: UserId, 
    val email: Email, 
    val profile: UserProfile, 
    val stats: UserStats, 
    val preferences: UserPreferences
) {
    val displayName: String get() = profile.displayName
    val username: String get() = profile.username
    val isActive: Boolean get() = stats.isActive
    val level: UserLevel get() = stats.level
    val reputation: Int get() = stats.reputation
    
    fun isGuest(): Boolean = false
}
```

### User Profile Components
- **UserProfile**: Name, username, bio, photo URL, social links
- **UserStats**: Submitted codes, upvotes, downvotes, reputation, level
- **UserPreferences**: Settings, notifications, theme preferences
- **Validation**: Comprehensive input validation with Result patterns

### Factory Methods
```kotlin
companion object {
    fun create(id: String, email: String, profile: UserProfile): Result<User>
    
    fun createWithStats(
        id: UserId,
        email: Email,
        profile: UserProfile,
        stats: UserStats,
        preferences: UserPreferences = UserPreferences.default(id)
    ): Result<User>
}
```

## Animated UI Components

### Staggered Animation System
```kotlin
@Composable
private fun AnimatedProfileHeader(
    user: User,
    onAction: (ProfileAction) -> Unit,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { -it / 2 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        ) + fadeIn(animationSpec = tween(600)),
    ) {
        ProfileHeader(user = user, onAction = onAction, modifier = modifier.fillMaxWidth())
    }
}
```

### Animation Sequence
1. **Profile Header**: Slides in from top with bounce effect (600ms)
2. **Stats Section**: Slides in from bottom with delay (800ms + 200ms delay)
3. **Activity Feed**: Slides in from bottom with longer delay (1000ms + 400ms delay)
4. **Sign Out Button**: Fades in last (800ms fade)

### Animated Statistics Cards
```kotlin
@Composable
private fun StatCard(
    value: Int,
    label: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    val animatedValue by animateIntAsState(
        targetValue = if (isVisible) value else 0,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "stat_counter",
    )
    
    LaunchedEffect(Unit) {
        delay(500)
        isVisible = true
    }
    
    // Card UI with animated counter
}
```

## Visual Design System

### Gradient Integration
```kotlin
Box(modifier = modifier.fillMaxSize()) {
    QodeHeroGradient()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(SpacingTokens.lg),
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        // Profile content
    }
}
```

### Auto-Hiding Navigation
```kotlin
AutoHidingTopAppBar(
    scrollState = scrollState,
    navigationIcon = QodeActionIcons.Back,
    onNavigationClick = onBackClick,
    navigationIconTint = MaterialTheme.colorScheme.onPrimaryContainer,
)
```

### Glassmorphism Effects
- **Transparent Containers**: Cards with alpha transparency
- **Gradient Shadows**: Context-aware shadow colors
- **Theme-Adaptive**: Different effects for light/dark themes
- **Blur Effects**: Backdrop filters for glassmorphism

## Profile Components

### Profile Header
```kotlin
@Composable
internal fun ProfileHeader(
    user: User,
    onAction: (ProfileAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        QodeAvatar(
            photoUrl = user.profile.photoUrl,
            size = SizeTokens.Avatar.sizeXLarge,
            modifier = Modifier.testTag("profile_avatar"),
        )
        UserInfo(user = user)
        EditProfileButton(onAction = onAction)
    }
}
```

### Header Features
- **Large Avatar**: Extra-large user avatar with photo loading
- **User Information**: Name, username, bio display
- **Edit Profile Button**: Primary action with shadow effects
- **Responsive Layout**: Adapts to content length

### Statistics Section
```kotlin
@Composable
internal fun StatsSection(
    userStats: UserStats,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg),
    ) {
        SectionTitle(title = stringResource(R.string.profile_stats_title))
        StatsCards(userStats = userStats)
    }
}
```

### Statistics Cards
- **Three-Card Layout**: Submitted codes, upvotes, downvotes
- **Gradient Backgrounds**: Unique gradient per card type
- **Animated Counters**: Spring-animated value counting
- **Icon Integration**: Category-specific icons from QodeIcons
- **Theme-Adaptive Shadows**: Different shadow effects per theme

### Activity Feed
```kotlin
@Composable
internal fun ActivityFeed(
    onAction: (ProfileAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(SpacingTokens.lg)) {
        SectionTitle(title = stringResource(R.string.profile_activity_title))
        
        ActivityCard(
            title = stringResource(R.string.profile_achievements_title),
            content = stringResource(R.string.profile_achievements_empty),
            icon = QodeStatusIcons.Gold,
            iconTint = MaterialTheme.colorScheme.primary,
            onClick = { onAction(ProfileAction.AchievementsClicked) },
        )
        
        ActivityCard(
            title = stringResource(R.string.profile_user_journey_title),
            content = stringResource(R.string.profile_user_journey_empty),
            icon = QodeNavigationIcons.History,
            iconTint = MaterialTheme.colorScheme.secondary,
            onClick = { onAction(ProfileAction.UserJourneyClicked) },
        )
    }
}
```

### Activity Features
- **Achievements Section**: User achievements and badges
- **User Journey**: Activity history and timeline
- **Clickable Cards**: Navigation to detailed views
- **Empty States**: Friendly messaging for new users
- **Color-Coded**: Different colors per activity type

## Error Handling & States

### Comprehensive State Management
```kotlin
when (currentState) {
    is ProfileUiState.Success -> {
        ProfileSuccessContent(
            user = currentState.user,
            onAction = viewModel::handleAction,
            onBackClick = onBackClick,
            modifier = Modifier.fillMaxSize(),
        )
    }
    
    is ProfileUiState.Loading -> {
        CircularProgressIndicator(
            modifier = Modifier
                .padding(SpacingTokens.lg)
                .semantics { contentDescription = loadingDescription },
        )
    }
    
    is ProfileUiState.Error -> {
        QodeRetryableErrorCard(
            message = currentState.exception.message ?: stringResource(R.string.profile_error_unknown),
            onRetry = { viewModel.handleAction(ProfileAction.RetryClicked) },
            modifier = Modifier.padding(SpacingTokens.lg),
        )
    }
}
```

### Error Recovery
- **Retry Mechanisms**: User-initiated retry for failed operations
- **Auth Recovery**: Automatic auth state restoration
- **State Preservation**: Maintain profile context during errors
- **User Feedback**: Clear error messages with actionable solutions

### Loading States
- **Semantic Loading**: Accessible loading indicators
- **Gradient Background**: Consistent visual experience during loading
- **Progressive Enhancement**: Content appears as it becomes available

## Event-Driven Navigation

### Navigation Events
```kotlin
LaunchedEffect(viewModel) {
    viewModel.events.collect { event ->
        when (event) {
            ProfileEvent.EditProfileRequested -> onEditProfile()
            ProfileEvent.SignedOut -> onSignOut()
            ProfileEvent.AchievementsRequested -> onAchievementsClick()
            ProfileEvent.UserJourneyRequested -> onUserJourneyClick()
        }
    }
}
```

### Event Types
- **Edit Profile**: Navigate to profile editing screen
- **Sign Out**: Navigate to authentication after sign-out
- **Achievements**: Navigate to achievements detail screen
- **User Journey**: Navigate to activity timeline

### Navigation Features
- **Type-Safe**: All navigation through events and callbacks
- **Context-Aware**: Maintains navigation context during auth flows
- **Error Handling**: Proper navigation error recovery
- **State Restoration**: Maintains profile state across navigation

## Preview & Testing System

### Comprehensive Preview Coverage
```kotlin
// Device variations
@DevicePreviews
@Composable
fun ProfileScreenDevicePreviews()

// Theme variations
@ThemePreviews
@Composable
fun ProfileScreenThemePreviews()

// Font scale accessibility
@FontScalePreviews
@Composable
fun ProfileScreenFontScalePreviews()

// Component previews
@ComponentPreviews
@Composable
fun ProfileHeaderComponentPreview(@PreviewParameter(UserPreviewParameterProvider::class) user: User)
```

### Preview Optimizations
- **Animation Bypass**: Static content for preview performance
- **State Management**: Preview-specific state handling
- **Parameter Providers**: Comprehensive test data scenarios
- **Accessibility Testing**: Font scale and contrast testing

### Testing Features
- **State Testing**: All UI states covered (loading, success, error)
- **Component Testing**: Individual component isolation
- **User Scenarios**: Different user types and data states
- **Accessibility**: Screen reader and scaling support

## Performance Optimizations

### Animation Performance
- **Spring Animations**: Natural, performant spring animations
- **Staggered Timing**: Prevents animation overload
- **Lazy Evaluation**: Animations only when needed
- **Memory Efficient**: Proper animation cleanup

### Scroll Performance
- **Auto-Hiding Navigation**: Smooth scroll-responsive navigation
- **Efficient Layout**: Optimized column layouts
- **State Management**: Minimal recomposition triggers
- **Memory Management**: Proper cleanup of scroll state

### Image Loading
- **QodeAvatar**: Efficient avatar loading and caching
- **Placeholder Handling**: Graceful image loading states
- **Error Fallbacks**: Default avatars for failed loads

## Development Guidelines

1. **Use Events for Navigation**: All navigation through ProfileEvent system
2. **Animate Thoughtfully**: Stagger animations for professional feel
3. **Monitor Auth State**: Continuous auth state observation
4. **Handle Errors Gracefully**: Comprehensive error recovery mechanisms
5. **Design System First**: Use QodeTheme and design tokens consistently
6. **Accessibility**: Proper semantic markup and screen reader support
7. **Test Comprehensively**: Cover all states and user scenarios
8. **Performance**: Optimize animations and scroll behavior

## Future Enhancements

### Planned Features
- **User Statistics Use Cases**: GetUserStatsUseCase for dynamic data
- **Achievement System**: GetUserAchievementsUseCase for badges and progress
- **Activity Timeline**: GetUserActivityUseCase for user journey tracking
- **Edit Profile**: Profile editing functionality with validation
- **Social Features**: Following, reputation system, and community metrics

### Architecture Extensions
- **Offline Support**: Profile data caching and offline functionality
- **Real-Time Updates**: Live statistics updates and notifications
- **Analytics Integration**: User behavior tracking and insights
- **Push Notifications**: Achievement notifications and profile updates

This user profile system provides a sophisticated, visually appealing, and highly functional profile experience with modern Android development patterns and enterprise-level architecture.