package com.qodein.shared.common.result

import com.qodein.shared.common.error.DomainError
import com.qodein.shared.common.error.AuthError
import com.qodein.shared.common.error.BusinessError
import com.qodein.shared.common.error.NetworkError
import com.qodein.shared.common.error.ValidationError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * Enhanced Result type that supports typed domain errors.
 *
 * This is the foundation of the new error handling system, providing:
 * - Type-safe error handling with compiler-enforced exhaustive handling
 * - Rich error context through domain-specific errors
 * - Better UX through precise error messages and recovery actions
 * - Enhanced analytics with detailed error tracking
 *
 * @param T The type of successful data
 * @param E The type of domain error (must extend DomainError)
 */
sealed interface Result<out T, out E : DomainError> {
    /**
     * Represents a successful result with data.
     */
    data class Success<T>(val data: T) : Result<T, Nothing>

    /**
     * Represents an error result with typed domain error.
     */
    data class Error<E : DomainError>(val error: E) : Result<Nothing, E>

    /**
     * Represents a loading state.
     */
    data object Loading : Result<Nothing, Nothing>
}

// Type aliases for common Result patterns

/**
 * Result type for network operations that can fail with network-related errors.
 */
typealias NetworkResult<T> = Result<T, NetworkError>

/**
 * Result type for authentication operations that can fail with auth-related errors.
 */
typealias AuthResult<T> = Result<T, AuthError>

/**
 * Result type for business operations that can fail with business logic errors.
 */
typealias BusinessResult<T> = Result<T, BusinessError>

/**
 * Result type for validation operations that can fail with validation errors.
 */
typealias ValidationResult<T> = Result<T, ValidationError>

/**
 * Result type that can fail with any domain error.
 * Useful for operations that might encounter multiple types of errors.
 */
typealias DomainResult<T> = Result<T, DomainError>

// Multi-domain Result types for complex operations

/**
 * Result type for operations that involve authentication and network calls.
 * Common for API operations that require authentication.
 */
typealias AuthNetworkResult<T> = Result<T, out DomainError> // Union would be AuthError | NetworkError

/**
 * Result type for promo code operations that can fail with multiple error types.
 * Covers network, auth, business, and validation errors.
 */
typealias PromoCodeResult<T> = Result<T, out DomainError> // Union would be NetworkError | AuthError | BusinessError | ValidationError

/**
 * Result type for service operations that can encounter various error types.
 */
typealias ServiceResult<T> = Result<T, out DomainError> // Union would be NetworkError | BusinessError

// Extension functions for Result operations

/**
 * Maps the success value of a Result to a new type.
 * Preserves error and loading states.
 */
inline fun <T, R, E : DomainError> Result<T, E>.map(transform: (T) -> R): Result<R, E> {
    return when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Error -> this
        is Result.Loading -> this
    }
}

/**
 * Maps the error value of a Result to a new error type.
 * Preserves success and loading states.
 */
inline fun <T, E : DomainError, R : DomainError> Result<T, E>.mapError(transform: (E) -> R): Result<T, R> {
    return when (this) {
        is Result.Success -> this
        is Result.Error -> Result.Error(transform(error))
        is Result.Loading -> this
    }
}

/**
 * Flat maps the success value of a Result, allowing for nested Result operations.
 * This is useful for chaining operations that each return a Result.
 */
inline fun <T, R, E : DomainError> Result<T, E>.flatMap(transform: (T) -> Result<R, E>): Result<R, E> {
    return when (this) {
        is Result.Success -> transform(data)
        is Result.Error -> this
        is Result.Loading -> this
    }
}

/**
 * Returns the success data or null if the result is not successful.
 */
fun <T, E : DomainError> Result<T, E>.getOrNull(): T? {
    return when (this) {
        is Result.Success -> data
        is Result.Error -> null
        is Result.Loading -> null
    }
}

/**
 * Returns the success data or the result of the default function if not successful.
 */
inline fun <T, E : DomainError> Result<T, E>.getOrElse(default: (E?) -> T): T {
    return when (this) {
        is Result.Success -> data
        is Result.Error -> default(error)
        is Result.Loading -> default(null)
    }
}

/**
 * Returns the error or null if the result is not an error.
 */
fun <T, E : DomainError> Result<T, E>.errorOrNull(): E? {
    return when (this) {
        is Result.Success -> null
        is Result.Error -> error
        is Result.Loading -> null
    }
}

/**
 * Returns true if the result is a success.
 */
fun <T, E : DomainError> Result<T, E>.isSuccess(): Boolean = this is Result.Success

/**
 * Returns true if the result is an error.
 */
fun <T, E : DomainError> Result<T, E>.isError(): Boolean = this is Result.Error

/**
 * Returns true if the result is loading.
 */
fun <T, E : DomainError> Result<T, E>.isLoading(): Boolean = this is Result.Loading

/**
 * Executes the given action if the result is a success.
 */
inline fun <T, E : DomainError> Result<T, E>.onSuccess(action: (T) -> Unit): Result<T, E> {
    if (this is Result.Success) action(data)
    return this
}

/**
 * Executes the given action if the result is an error.
 */
inline fun <T, E : DomainError> Result<T, E>.onError(action: (E) -> Unit): Result<T, E> {
    if (this is Result.Error) action(error)
    return this
}

/**
 * Executes the given action if the result is loading.
 */
inline fun <T, E : DomainError> Result<T, E>.onLoading(action: () -> Unit): Result<T, E> {
    if (this is Result.Loading) action()
    return this
}

/**
 * Combines two Results, applying the given transform function if both are successful.
 * If either Result is an error or loading, returns the first non-success state encountered.
 */
inline fun <T1, T2, R, E : DomainError> Result<T1, E>.combine(
    other: Result<T2, E>,
    transform: (T1, T2) -> R
): Result<R, E> {
    return when {
        this is Result.Loading || other is Result.Loading -> Result.Loading
        this is Result.Error -> this
        other is Result.Error -> other
        this is Result.Success && other is Result.Success -> Result.Success(transform(data, other.data))
        else -> Result.Loading // Should not happen
    }
}

// Flow extensions for typed Results

/**
 * Converts a Flow<T> to Flow<Result<T, E>> with typed error handling.
 * This is the enhanced version of the original asResult() function.
 *
 * @param errorMapper Function to map exceptions to domain errors
 */
inline fun <T, E : DomainError> Flow<T>.asTypedResult(
    crossinline errorMapper: (Throwable) -> E
): Flow<Result<T, E>> =
    map<T, Result<T, E>> { Result.Success(it) }
        .onStart { emit(Result.Loading) }
        .catch { throwable -> emit(Result.Error(errorMapper(throwable))) }

/**
 * Maps a Flow<Result<T, E1>> to Flow<Result<R, E2>> with both data and error transformation.
 */
inline fun <T, R, E1 : DomainError, E2 : DomainError> Flow<Result<T, E1>>.mapResult(
    crossinline dataTransform: (T) -> R,
    crossinline errorTransform: (E1) -> E2
): Flow<Result<R, E2>> = map { result ->
    when (result) {
        is Result.Success -> Result.Success(dataTransform(result.data))
        is Result.Error -> Result.Error(errorTransform(result.error))
        is Result.Loading -> Result.Loading
    }
}

/**
 * Filters success results based on a predicate.
 * If predicate fails, converts to error using errorProvider.
 */
inline fun <T, E : DomainError> Flow<Result<T, E>>.filterSuccess(
    crossinline predicate: (T) -> Boolean,
    crossinline errorProvider: (T) -> E
): Flow<Result<T, E>> = map { result ->
    when (result) {
        is Result.Success -> {
            if (predicate(result.data)) {
                result
            } else {
                Result.Error(errorProvider(result.data))
            }
        }
        is Result.Error -> result
        is Result.Loading -> result
    }
}

// Suspend function helpers

/**
 * Wraps a suspend function call in a Result, mapping exceptions to domain errors.
 */
suspend inline fun <T, E : DomainError> resultOf(
    errorMapper: (Throwable) -> E,
    block: () -> T
): Result<T, E> = try {
    Result.Success(block())
} catch (e: Exception) {
    Result.Error(errorMapper(e))
}

/**
 * Wraps a nullable suspend function call in a Result.
 * Returns error if the result is null.
 */
suspend inline fun <T, E : DomainError> resultOfNotNull(
    nullError: E,
    errorMapper: (Throwable) -> E,
    block: () -> T?
): Result<T, E> = try {
    val result = block()
    if (result != null) {
        Result.Success(result)
    } else {
        Result.Error(nullError)
    }
} catch (e: Exception) {
    Result.Error(errorMapper(e))
}