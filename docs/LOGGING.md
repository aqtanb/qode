# Logging System Documentation

This document describes the hybrid logging architecture implemented in the Qode application, which combines Kermit (for shared multiplatform code) with Timber (for Android-specific code).

## Architecture Overview

The logging system follows NIA (Now in Android) patterns with a clean separation between multiplatform and platform-specific logging:

```
┌── Shared Module ──────┐    ┌── Android Modules ────┐    ┌── App Module ─────┐
│  Uses Kermit        │    │  Uses Timber Direct │    │  Bridge Active  │
│ - Business logic      │    │ - Data sources        │    │ KermitTimberWriter │
│ - Domain entities     │    │ - UI components       │    │ ┌───────────────┐ │
│ - Multiplatform code  │    │ - Android features    │    │ │ Unified Output │ │
└─────────────────────────┘    └─────────────────────────┘    └─────────────────┘
```

## Core Components

### 1. Kermit (Shared Module)
- **Purpose**: Logging for multiplatform business logic
- **Location**: `shared/src/commonMain/kotlin`
- **Usage**: Direct Kermit API calls
- **Configuration**: Minimal severity level based on debug/release builds

### 2. Timber (Android Modules)
- **Purpose**: Android-specific logging following NIA patterns
- **Location**: All Android modules (`core/*`, `feature/*`, `androidApp`)
- **Usage**: Direct Timber API calls with tags
- **Configuration**: Debug tree with line numbers, release tree for errors only

### 3. KermitTimberWriter (Bridge)
- **Purpose**: Routes shared module logs through Timber trees
- **Location**: `androidApp/src/main/java/.../logging/KermitTimberWriter.kt`
- **Benefit**: Unified log output, easy Crashlytics integration

## Implementation Details

### Shared Module Logging

```kotlin
// Direct Kermit usage in shared module
import co.touchlab.kermit.Logger

class BusinessLogicClass {
    companion object {
        private val logger = Logger.withTag("BusinessLogic")
    }
    
    fun performOperation() {
        logger.d { "Starting operation" }
        
        try {
            // Business logic
            logger.i { "Operation completed successfully" }
        } catch (e: Exception) {
            logger.e(e) { "Operation failed: ${e.message}" }
            throw e
        }
    }
}
```

### Android Module Logging

```kotlin
// Direct Timber usage in Android modules (NIA style)
import timber.log.Timber

class FirestoreDataSource {
    companion object {
        private const val TAG = "FirestoreDataSource"
    }
    
    suspend fun fetchData() {
        Timber.tag(TAG).d("Starting data fetch")
        
        try {
            // Data fetching logic
            Timber.tag(TAG).i("Data fetched successfully")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to fetch data")
            throw e
        }
    }
}
```

### Error Integration

The logging system integrates with the shared error handling system:

```kotlin
// In Android modules - log with error classification
import com.qodein.shared.common.result.toErrorType
import com.qodein.shared.common.result.getErrorCode

try {
    // Risky operation
} catch (e: Exception) {
    val errorType = e.toErrorType()
    val errorCode = e.getErrorCode()
    Timber.tag(TAG).e(e, "Error in operation [$errorCode][${errorType.name}]")
    throw e
}
```

## Configuration

### Application Setup

The logging system is initialized in `QodeApplication.kt`:

```kotlin
private fun initializeLogging() {
    val isDebug = BuildConfig.DEBUG
    
    // Initialize Timber for Android-specific logging
    if (isDebug) {
        Timber.plant(object : Timber.DebugTree() {
            override fun createStackElementTag(element: StackTraceElement): String {
                return "${super.createStackElementTag(element)}:${element.lineNumber}"
            }
        })
    } else {
        // Release builds: Only log errors for Crashlytics
        Timber.plant(object : Timber.Tree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                if (priority >= android.util.Log.ERROR) {
                    // TODO: Send to Crashlytics when Firebase Analytics is added
                }
            }
        })
    }
    
    // Configure Kermit for shared module logging, bridged to Timber
    Logger.setMinSeverity(if (isDebug) Severity.Verbose else Severity.Error)
    Logger.setLogWriters(KermitTimberWriter())
}
```

### Debug vs Release Behavior

| Build Type | Kermit Severity | Timber Behavior | Output |
|------------|----------------|-----------------|--------|
| Debug | Verbose | All logs with line numbers | Logcat (full) |
| Release | Error | Errors only | Crashlytics (when added) |

## Best Practices

### 1. Follow NIA Patterns
- **Android modules**: Use Timber directly, no abstractions
- **Shared module**: Use Kermit directly for multiplatform compatibility
- **No over-engineering**: Keep logging simple and maintainable

### 2. Logging Levels
- **Verbose/Debug**: Development info, removed in release
- **Info**: Important application flow
- **Warn**: Recoverable issues
- **Error**: Failures requiring attention

### 3. Tag Conventions
```kotlin
// Use descriptive, consistent tags
private const val TAG = "FirestorePromoCodeDS"    // Data source
private const val TAG = "AuthViewModel"           // ViewModel
private val logger = Logger.withTag("PromoCodeUseCase") // Use case
```

### 4. Error Boundary Logging
Log at architectural boundaries following NIA patterns:

```kotlin
//  Good - Log at data layer boundary
class RepositoryImpl {
    suspend fun fetchData() {
        try {
            // Data fetching
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error in fetchData")
            throw e // Re-throw for upper layers
        }
    }
}

//  Good - Log handled errors at ViewModel
class ViewModel {
    private fun handleResult(result: Result<Data>) {
        when (result) {
            is Result.Error -> {
                Timber.tag(TAG).e(result.exception, "Handled error in UI")
                // Update UI state
            }
        }
    }
}
```

### 5. Performance Considerations
```kotlin
//  Good - Use lambda for expensive operations
logger.d { "Complex data: ${expensiveOperation()}" }

// ❌ Avoid - Always evaluates even if not logged
logger.d("Complex data: ${expensiveOperation()}")
```

## Module Dependencies

### Required Dependencies

**Shared Module** (`shared/build.gradle.kts`):
```kotlin
dependencies {
    implementation(libs.kermit)
}
```

**Android Modules** (e.g., `core/data/build.gradle.kts`):
```kotlin
dependencies {
    implementation(libs.timber)
}
```

**AndroidApp Module** (`androidApp/build.gradle.kts`):
```kotlin
dependencies {
    implementation(libs.kermit)  // For KermitTimberWriter
    implementation(libs.timber)
}

buildFeatures {
    buildConfig = true  // Required for BuildConfig.DEBUG
}
```

### Dependency Versions

Defined in `gradle/libs.versions.toml`:
```toml
[versions]
kermit = "2.0.4"
timber = "5.0.1"

[libraries]
kermit = { group = "co.touchlab", name = "kermit", version.ref = "kermit" }
timber = { group = "com.jakewharton.timber", name = "timber", version.ref = "timber" }
```

## Integration with Error Handling

The logging system works seamlessly with the error handling system documented in [ERROR_HANDLING.md](ERROR_HANDLING.md):

1. **Shared Module**: Exceptions are classified using `ErrorType` enum
2. **Android Modules**: Log errors with classification for debugging
3. **Structured Logging**: Error codes and retry information included
4. **Analytics Ready**: Foundation for Firebase Analytics integration

## Analytics Integration  **COMPLETED**

### Firebase Analytics Implementation
Firebase Analytics has been successfully integrated alongside the logging system:

1. **AnalyticsHelper Interface**: Clean abstraction following NIA patterns
2. **Multiple Implementations**: Firebase (production), Stub (debug), NoOp (tests)
3. **Screen View Tracking**: Automatic tracking across all screens using `TrackScreenViewEvent`
4. **Domain Extensions**: Type-safe analytics logging via extension functions
5. **ViewModel Integration**: Analytics injected into all ViewModels

```kotlin
// Analytics integration with logging
class HomeViewModel @Inject constructor(
    private val analyticsHelper: AnalyticsHelper,
    // other dependencies...
) {
    private fun onVote(promocodeId: String, isUpvote: Boolean) {
        val voteType = if (isUpvote) "upvote" else "downvote"
        
        // Analytics tracking
        analyticsHelper.logVote(promocodeId, voteType)
        
        // Logging for debugging
        Timber.tag(TAG).d("Vote tracked: $voteType for $promocodeId")
        
        // Business logic...
    }
}
```

### Crashlytics Integration
For production error tracking with analytics correlation:

```kotlin
// Enhanced Timber tree with Crashlytics
if (!isDebug) {
    Timber.plant(object : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority >= android.util.Log.ERROR) {
                // Send to both Crashlytics and Analytics
                Firebase.crashlytics.recordException(t ?: Exception(message))
                analyticsHelper.logEvent(
                    AnalyticsEvent(
                        type = "app_error",
                        extras = listOf(
                            AnalyticsEvent.Param("error_tag", tag ?: "unknown"),
                            AnalyticsEvent.Param("error_message", message)
                        )
                    )
                )
            }
        }
    })
}
```

### Testing

The logging system is designed to be testable:

```kotlin
// Mock Kermit in shared module tests
@Test
fun testBusinessLogic() {
    // Logger calls don't interfere with test logic
    val result = businessLogic.performOperation()
    assertThat(result).isNotNull()
}

// Timber testing in Android modules
@Test
fun testDataSource() {
    // Timber calls are side effects and don't affect test outcomes
    val data = dataSource.fetchData()
    assertThat(data).isNotEmpty()
}
```

## Troubleshooting

### Common Issues

1. **BuildConfig not found**: Ensure `buildFeatures { buildConfig = true }` in androidApp
2. **Circular dependencies**: Don't import between core modules for logging
3. **Missing logs in release**: Check Timber tree configuration
4. **Kermit not bridging**: Verify KermitTimberWriter is set in Logger

### Verification

Check that logging works correctly:

```kotlin
// Test in shared module
Logger.withTag("TestTag").d { "Kermit test message" }

// Test in Android module  
Timber.tag("TestTag").d("Timber test message")

// Both should appear in Logcat during development
```

## Migration Guide

### From Previous Logging
If migrating from other logging approaches:

1. **Replace Log.d()**: Use `Timber.tag(TAG).d()` in Android modules
2. **Replace println()**: Use appropriate Logger calls in shared module
3. **Remove abstractions**: Use direct Timber/Kermit APIs following NIA patterns
4. **Update tags**: Use consistent naming conventions

### Adding to New Modules

**For new Android modules**:
1. Add `implementation(libs.timber)` to build.gradle.kts
2. Use `Timber.tag(TAG).d()` pattern throughout
3. No additional setup required

**For new shared code**:
1. Kermit is already configured in shared module
2. Use `Logger.withTag("Tag").d { }` pattern
3. Logs automatically bridge to Timber

This documentation ensures the logging system remains maintainable, performant, and aligned with modern Android development practices.
