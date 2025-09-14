# Error Handling Architecture

## Overview

Qode uses a **standardized Result-based error handling system** following **NIA (Now in Android) patterns**. This provides consistent loading states, type-safe error handling, and superior UX across all features.

## 🏗️ Architecture

### **Core Pattern: Sealed Result Interface**

```kotlin
// shared/src/commonMain/kotlin/com/qodein/shared/common/result/Result.kt
sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val exception: Throwable) : Result<Nothing>  
    data object Loading : Result<Nothing>
}
```

### **Layer Responsibilities**

| Layer | Pattern | Example |
|-------|---------|---------|
| **Repository** | `Flow<T>` + throw exceptions | `IOException`, `SecurityException` |
| **Use Case** | `.asResult()` extension | `Flow<T>` → `Flow<Result<T>>` |
| **ViewModel** | Handle Result states | `when (result)` → Update `UiState` |
| **UI** | Display based on state | Loading/Success/Error components |

## 🔧 Implementation

### **1. Repository Layer**
```kotlin
class PromoCodeRepositoryImpl : PromoCodeRepository {
    override fun getPromoCodes(): Flow<List<PromoCode>> =
        dataSource.getPromoCodes() // Throws standard exceptions
}
```

### **2. Use Case Layer**
```kotlin
class GetPromoCodesUseCase @Inject constructor(
    private val repository: PromoCodeRepository
) {
    operator fun invoke(): Flow<Result<List<PromoCode>>> =
        repository.getPromoCodes().asResult() //  Always use this
}
```

### **3. ViewModel Layer**
```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getPromoCodesUseCase: GetPromoCodesUseCase
) : ViewModel() {
    
    private fun loadData() {
        viewModelScope.launch {
            getPromoCodesUseCase().collect { result ->
                _uiState.value = when (result) {
                    is Result.Loading -> HomeUiState.Loading
                    is Result.Success -> HomeUiState.Success(data = result.data)
                    is Result.Error -> HomeUiState.Error(
                        errorType = result.exception.toErrorType(),
                        isRetryable = result.exception.isRetryable()
                    )
                }
            }
        }
    }
}
```

### **4. UI Layer**
```kotlin
@Composable
fun HomeScreen(uiState: HomeUiState, onRetry: () -> Unit) {
    when (uiState) {
        is HomeUiState.Loading -> LoadingIndicator()
        is HomeUiState.Success -> ContentList(uiState.data)
        is HomeUiState.Error -> QodeActionErrorCard(
            message = uiState.errorType.toLocalizedMessage(),
            errorAction = uiState.errorType.suggestedAction(),
            onActionClicked = onRetry,
            onDismiss = { /* dismiss logic */ }
        )
    }
}
```

## 🌍 Error Classification & Localization

### **Type-Safe Error Classification**
```kotlin
// Shared module: Business logic only (NO strings)
enum class ErrorType {
    NETWORK_TIMEOUT,
    AUTH_USER_CANCELLED, 
    PROMO_CODE_EXPIRED,
    // ... clean classification
}

// Extension functions for smart error handling
fun Throwable.toErrorType(): ErrorType = // Parse exception message
fun Throwable.isRetryable(): Boolean = // Determine retry logic
fun ErrorType.suggestedAction(): ErrorAction = // Smart action mapping
```

### **Localized UI Mapping**
```kotlin
// UI layer: Proper localization support
@Composable
fun ErrorType.toLocalizedMessage(): String = when (this) {
    ErrorType.AUTH_USER_CANCELLED -> stringResource(R.string.error_auth_user_cancelled)
    // "Sign-in was cancelled." / "Вход был отменен." / "Кіру болдырылмады."
}
```

##  Benefits

### **Developer Experience**
- **Consistent Patterns**: Same error handling everywhere
- **Type Safety**: Compile-time guarantees, no runtime surprises
- **Clean Architecture**: Business logic separated from UI concerns
- **Better Testing**: Uniform Result patterns, easy to mock

### **User Experience**  
- **No Stuck States**: Always dismissible with X button
- **Smart Actions**: Context-aware buttons (retry vs contact support)
- **Multi-Language**: Proper localization (EN/RU/KZ)
- **Accessibility**: Full screen reader support

### **Business Benefits**
- **Faster Development**: Standardized patterns reduce decision fatigue
- **Fewer Bugs**: Type safety catches errors at compile time
- **Better Metrics**: Consistent error classification for analytics
- **International Ready**: Built-in localization support

## ❌ What NOT to Do

### **DON'T: Manual Result Wrapping**
```kotlin
// ❌ Verbose and error-prone
repository.getData()
    .map { Result.Success(it) }
    .catch { emit(Result.Error(it)) }
```

### **DON'T: Mix Exception Patterns**
```kotlin
// ❌ Inconsistent error handling
try {
    val result = useCase()
    // Handle result...
} catch (e: Exception) {
    // Handle exception...
}
```

### **DON'T: Strings in Shared Module**
```kotlin
// ❌ Breaks localization
enum class ErrorType(val message: String) {
    NETWORK_ERROR("Network error occurred")
}
```

## 🚀 For New Developers

### **Quick Start Checklist**
1. **Repository**: Throw standard exceptions (`IOException`, `SecurityException`)
2. **Use Case**: Always use `.asResult()` extension
3. **ViewModel**: Handle with `when (result)` patterns
4. **UI**: Use `QodeActionErrorCard` for errors
5. **Import**: Always `import com.qodein.shared.common.result.Result`

### **Common Gotchas**
- **Smart Cast Issues**: Extract cross-module properties to local variables
- **Import Conflicts**: IDE might auto-import Kotlin Result instead of shared Result
- **Exception Context**: Add meaningful messages for extension function parsing

### **Testing Patterns**
```kotlin
@Test
fun `test error handling`() = runTest {
    // Given
    every { repository.getData() } throws IOException("Network error")
    
    // When & Then
    useCase().test {
        assertEquals(Result.Loading, awaitItem())
        assertEquals(Result.Error(IOException("Network error")), awaitItem())
    }
}
```

## 📁 File Structure

```
shared/src/commonMain/kotlin/com/qodein/shared/common/result/
├── Result.kt                 # Sealed interface + .asResult() extension
├── ErrorType.kt             # Error classification enum
└── ExceptionExt.kt          # Smart error handling extensions

core/ui/src/main/java/com/qodein/core/ui/error/
└── ErrorStringMapper.kt     # UI localization mapping

core/ui/src/main/res/
├── values/strings.xml       # English error messages
├── values-ru/strings.xml    # Russian translations  
└── values-kk/strings.xml    # Kazakh translations
```

## Future Improvements (Research Completed)

### Rich Errors (Kotlin 2.4)
Kotlin 2.4 will introduce "Rich Errors" - native union types for error handling without wrappers. This will provide type-safe error handling without Result/Either patterns.

### Current Best Practices (2025)
- **Sealed classes** for domain-specific errors
- **Arrow Kt Raise DSL** for functional error handling
- **Context receivers** (experimental) for composable error handling

### Migration Strategy
The current Result-based system is solid and will migrate easily to Rich Errors when available. No major refactoring needed now.

**Status**: Complete with 50+ files updated, ready for Rich Errors migration when Kotlin 2.4 releases.
