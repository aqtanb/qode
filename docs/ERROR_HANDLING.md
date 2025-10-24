# Error Handling

## Overview

Qode uses `Result<Data, OperationError>` for type-safe error handling across all layers.

## Architecture

**Repositories** catch exceptions and map to domain errors:
- Domain-specific: `PostError`, `StorageError`, `PromoCodeError`
- Infrastructure: `SystemError` (network, auth, permissions)

**Use Cases** transform Results using `.map()`, `.andThen()`, `.mapError()`

**ViewModels** handle Results with `when (result)` expressions

**UI** displays errors using error mapping extensions

## Error Types

### SystemError (Infrastructure)
- `Offline` - Network failures (IOException)
- `Unauthorized` - Not authenticated (HTTP 401)
- `PermissionDenied` - Lacks permissions (HTTP 403)
- `ServiceDown` - Backend unavailable (HTTP 503)
- `Unknown` - Unexpected errors

### Domain Errors
- `PostError` - Post validation/submission failures
- `StorageError` - Upload/quota/file issues
- `PromoCodeError` - Promo code validation

## Patterns

**Repository:**
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

See existing code for complete examples.
