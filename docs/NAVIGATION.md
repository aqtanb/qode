# Navigation System

## Overview
Type-safe navigation system using Kotlinx Serialization with sophisticated state management, nested navigation graphs, and transparent top app bars. Features bottom navigation, deep linking, and context-aware navigation state tracking.

## Architecture
- **Type-Safe Navigation**: Kotlinx Serialization for compile-time route safety
- **Nested Graph Structure**: Feature-based navigation graphs with base routes
- **State Management**: QodeAppState for complex navigation state tracking
- **Bottom Navigation**: Tab-based navigation with proper state restoration
- **Transparent Design**: Seamless gradient integration across all screens
- **Deep Linking**: Support for external navigation and state restoration

## Key Files

### Core Navigation Framework
- `app/navigation/QodeNavHost.kt` - Main navigation coordinator with feature sections
- `app/ui/QodeAppState.kt` - Complex navigation state management and tab tracking
- `app/navigation/TopLevelDestination.kt` - Top-level tab destination definitions
- `app/navigation/NavigationHandler.kt` - Navigation event handling
- `app/navigation/NavigationActions.kt` - Navigation action definitions

### Feature Navigation
- `feature/home/navigation/HomeNavigation.kt` - Home section with nested routes
- `feature/auth/navigation/AuthNavigation.kt` - Authentication flow navigation
- `feature/search/navigation/SearchNavigation.kt` - Search functionality navigation
- `feature/profile/navigation/ProfileNavigation.kt` - Profile section navigation
- `feature/inbox/navigation/InboxNavigation.kt` - Inbox/notifications navigation
- `feature/promocode/navigation/SubmissionNavigation.kt` - PromoCode submission wizard

### Testing
- `app/navigation/NavigationHandlerTest.kt` - Navigation logic unit tests

## Type-Safe Route System

### Route Definitions with Kotlinx Serialization
```kotlin
// Feature base routes (for navigation graphs)
@Serializable object HomeBaseRoute
@Serializable object AuthBaseRoute
@Serializable object SearchBaseRoute

// Screen routes (for individual screens)
@Serializable object HomeRoute
@Serializable object AuthRoute
@Serializable object SearchRoute
@Serializable object ProfileRoute
@Serializable object SubmissionRoute
```

### Navigation Graph Structure
```kotlin
// Nested navigation with base routes
navigation<HomeBaseRoute>(startDestination = HomeRoute) {
    composable<HomeRoute> {
        HomeScreen()
    }
    // Additional home section routes can be added here
}

navigation<AuthBaseRoute>(startDestination = AuthRoute) {
    composable<AuthRoute> {
        AuthScreen(onNavigateToHome = onAuthSuccess)
    }
}
```

### Navigation Extension Functions
```kotlin
// Type-safe navigation extensions
fun NavController.navigateToAuth(navOptions: NavOptions? = null) {
    navigate(route = AuthRoute, navOptions = navOptions)
}

fun NavController.navigateToHome(navOptions: NavOptions) = 
    navigate(route = HomeRoute, navOptions)

fun NavController.navigateToSubmission(navOptions: NavOptions? = null) {
    navigate(route = SubmissionRoute, navOptions = navOptions)
}
```

## Navigation State Management

### QodeAppState Architecture
```kotlin
@Stable
class QodeAppState(val navController: NavHostController) {
    // Current destination tracking
    val currentDestination: NavDestination?
    val currentTopLevelDestination: TopLevelDestination?
    val selectedTabDestination: TopLevelDestination?
    
    // State checking
    val isNestedScreen: Boolean
    val isProfileScreen: Boolean
    
    // Navigation actions
    fun navigateToTopLevelDestination(destination: TopLevelDestination)
}
```

### Sophisticated State Tracking
```kotlin
val selectedTabDestination: TopLevelDestination?
    @Composable get() {
        val actualDestination = currentTopLevelDestination
        return actualDestination ?: (
            // We're on a nested screen, show the last known destination
            lastTopLevelDestination.value ?: HOME
        )
    }
```

### Nested Screen Detection
```kotlin
val isNestedScreen: Boolean
    @Composable get() {
        val topLevelDestination = TopLevelDestination.entries.firstOrNull { destination ->
            currentDestination?.hasRoute(route = destination.route) == true ||
            currentDestination?.parent?.hasRoute(route = destination.route) == true
        }
        return topLevelDestination == null
    }
```

## Top-Level Destinations

### Tab Configuration
```kotlin
enum class TopLevelDestination(
    val selectedIcon: ImageVector,
    val unSelectedIcon: ImageVector,
    @StringRes val iconTextId: Int,
    @StringRes val titleTextId: Int,
    val route: KClass<*>,
    val baseRoute: KClass<*> = route
) {
    HOME(
        selectedIcon = Icons.Filled.Home,
        unSelectedIcon = Icons.Outlined.Home,
        iconTextId = homeR.string.feature_home_title,
        titleTextId = R.string.app_name,
        route = HomeBaseRoute::class,
        baseRoute = HomeBaseRoute::class,
    ),
    SEARCH(
        selectedIcon = Icons.Filled.Search,
        unSelectedIcon = Icons.Outlined.Search,
        iconTextId = R.string.search_title,
        titleTextId = R.string.search_title,
        route = SearchRoute::class,
        baseRoute = SearchBaseRoute::class,
    ),
    INBOX(
        selectedIcon = Icons.Filled.Inbox,
        unSelectedIcon = Icons.Outlined.Inbox,
        iconTextId = R.string.inbox_title,
        titleTextId = R.string.inbox_title,
        route = InboxRoute::class,
        baseRoute = InboxBaseRoute::class,
    )
}
```

### Navigation Options with State Management
```kotlin
fun navigateToTopLevelDestination(topLevelDestination: TopLevelDestination) {
    trace("Navigation: ${topLevelDestination.name}") {
        val topLevelNavOptions = navOptions {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = false // Always go to base route
        }
        
        when (topLevelDestination) {
            HOME -> navController.navigate(
                route = HomeBaseRoute,
                navOptions = topLevelNavOptions,
            )
            // ... other destinations
        }
    }
}
```

## Feature Navigation Sections

### QodeNavHost Coordination
```kotlin
@Composable
fun QodeNavHost(
    appState: QodeAppState,
    modifier: Modifier = Modifier
) {
    val navController = appState.navController
    val selectedTabDestination = appState.selectedTabDestination ?: TopLevelDestination.HOME

    NavHost(
        navController = navController,
        startDestination = HomeBaseRoute,
        modifier = modifier,
    ) {
        homeSection(
            onPromoCodeClick = {},
            promoCodeDetail = {},
        )
        
        searchSection()
        inboxSection()
        
        profileSection(
            onBackClick = {
                appState.navigateToTopLevelDestination(selectedTabDestination)
            },
            onSignOut = {
                appState.navigateToTopLevelDestination(TopLevelDestination.HOME)
            },
        )
        
        authSection(
            onAuthSuccess = {
                appState.navigateToTopLevelDestination(selectedTabDestination)
            },
            onBackClick = {
                appState.navigateToTopLevelDestination(selectedTabDestination)
            },
        )
        
        submissionSection(
            onNavigateBack = {
                navController.popBackStack()
            },
        )
    }
}
```

### Feature Navigation Implementation
```kotlin
// Home section with nested navigation support
fun NavGraphBuilder.homeSection(
    onPromoCodeClick: (String) -> Unit,
    promoCodeDetail: NavGraphBuilder.() -> Unit
) {
    navigation<HomeBaseRoute>(startDestination = HomeRoute) {
        composable<HomeRoute> {
            HomeScreen()
        }
        // Additional nested routes can be added here
        promoCodeDetail()
    }
}

// Auth section with success callbacks
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

## Context-Aware Navigation

### Smart Back Navigation
The navigation system provides intelligent back navigation that considers user context:

```kotlin
// Profile back navigation
onBackClick = {
    // Navigate back to the last top-level destination instead of empty profile
    appState.navigateToTopLevelDestination(selectedTabDestination)
}

// Auth success navigation
onAuthSuccess = {
    // Navigate back to the original tab after successful auth
    appState.navigateToTopLevelDestination(selectedTabDestination)
}
```

### Tab State Preservation
- **Selected Tab Tracking**: Maintains which tab user came from when on nested screens
- **State Restoration**: Intelligently restores appropriate tab state
- **Context Preservation**: Remembers user's navigation context across authentication flows

## Transparent Top App Bars

### Seamless Gradient Integration
All screens implement transparent top app bars for seamless gradient integration:

```kotlin
// Example transparent top app bar implementation
TopAppBar(
    colors = TopAppBarDefaults.topAppBarColors(
        containerColor = Color.Transparent
    ),
    title = { Text("Screen Title") }
)
```

### Background Design
- **QodeHeroGradient**: All screens use gradient backgrounds
- **Floating Decorations**: Dynamic decorations that adapt to screen size
- **Glass Effects**: Transparent components with glassmorphism design
- **Consistent Branding**: Unified visual experience across all navigation levels

## Deep Linking Support

### External Navigation
The type-safe navigation system supports external deep links:

```kotlin
// Deep link handling with type-safe routes
@Serializable
data class PromoCodeDetailRoute(val promoCodeId: String)

// Navigation from external sources
fun handleDeepLink(uri: Uri) {
    when {
        uri.pathSegments.contains("promocode") -> {
            val promoCodeId = uri.getQueryParameter("id")
            navController.navigate(PromoCodeDetailRoute(promoCodeId))
        }
    }
}
```

### State Restoration
- **Navigation State**: Full navigation state preservation
- **Tab Memory**: Remembers last active tab across app restarts
- **Form State**: Maintains wizard progress during navigation
- **Authentication Flow**: Preserves navigation context through auth

## Navigation Testing

### Unit Testing Navigation Logic
```kotlin
class NavigationHandlerTest {
    @Test
    fun `test top level destination navigation`() {
        // Test navigation state changes
    }
    
    @Test
    fun `test nested screen detection`() {
        // Test nested screen identification
    }
    
    @Test
    fun `test context-aware back navigation`() {
        // Test intelligent back button behavior
    }
}
```

### UI Testing Navigation Flows
- **Complete User Journeys**: Test full navigation flows
- **Tab Switching**: Verify tab state preservation
- **Authentication Flow**: Test auth navigation context
- **Deep Link Handling**: Verify external navigation

## Development Guidelines

### Route Definition
1. **Use Kotlinx Serialization**: All routes must be `@Serializable` objects or data classes
2. **Separate Base Routes**: Use base routes for navigation graphs, screen routes for composables
3. **Type Safety**: Leverage compile-time route checking
4. **Consistent Naming**: Follow `FeatureBaseRoute` and `FeatureRoute` patterns

### Navigation Implementation
1. **Extension Functions**: Create type-safe navigation extensions for each route
2. **State Management**: Use QodeAppState for complex navigation state
3. **Context Awareness**: Consider user context in navigation decisions
4. **Transparent Design**: Maintain transparent top bars across all screens

### Feature Integration
1. **Section Pattern**: Implement `featureSection()` functions for NavGraphBuilder
2. **Callback Parameters**: Provide navigation callbacks for feature coordination
3. **Event Handling**: Use Events system for navigation side effects
4. **State Preservation**: Maintain appropriate state across navigation

### Testing Strategy
1. **Unit Tests**: Test navigation logic and state management
2. **Integration Tests**: Test feature navigation integration
3. **UI Tests**: Test complete user navigation flows
4. **Deep Link Tests**: Verify external navigation handling

This navigation system provides a robust, type-safe, and user-friendly navigation experience with sophisticated state management and seamless visual integration.