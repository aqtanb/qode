# Error Handling

## Overview

Qode uses `Result<Data, OperationError>` for type-safe error handling across all layers.

## Architecture

**Data Sources** catch exceptions and map to domain errors:
- Domain-specific: `PostError`, `StorageError`, `PromoCodeError`
- Infrastructure: `SystemError` (network, auth, permissions)

**Use Cases** transform Results using `.map()`, `.andThen()`, `.mapError()`

**ViewModels** handle Results with `when (result)` expressions

**UI** displays errors using error mapping extensions

## Patterns

**Data Source:**
```kotlin
catch (e: IOException) -> Result.Error(SystemError.Offline)
catch (e: StorageException) -> Result.Error(StorageError.UploadFailure.QuotaExceeded)
```

**ViewModel:**
```kotlin
when (result) {
    is Result.Success -> update UI
    is Result.Error -> handle error
}
```
