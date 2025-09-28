package com.qodein.shared.common

import com.qodein.shared.common.error.Error

typealias RootError = Error

/**
 * Type-safe Result wrapper for operations that can succeed or fail.
 *
 * @param D The type of data returned on success
 * @param E The type of error returned on failure (must extend Error)
 */
sealed interface Result<out D, out E : RootError> {

    /**
     * Represents a successful operation with data.
     */
    data class Success<out D>(val data: D) : Result<D, Nothing>

    /**
     * Represents a failed operation with a typed error.
     */
    data class Error<out E : RootError>(val error: E) : Result<Nothing, E>
}

// Core transformation functions

/**
 * Maps the success value to a new type while preserving error type.
 * Enables transforming data in the success path of the railway.
 */
fun <D, E : RootError, R> Result<D, E>.map(transform: (D) -> R): Result<R, E> =
    when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Error -> this
    }

/**
 * Chains operations that can fail. If current result is Success, applies transform.
 * If current result is Error, propagates the error without calling transform.
 * This is the core of railway-oriented programming.
 */
fun <D, E : RootError, R> Result<D, E>.andThen(transform: (D) -> Result<R, E>): Result<R, E> =
    when (this) {
        is Result.Success -> transform(data)
        is Result.Error -> this
    }

// Error handling functions

/**
 * Maps the error to a new error type while preserving success type.
 */
fun <D, E : RootError, R : RootError> Result<D, E>.mapError(transform: (E) -> R): Result<D, R> =
    when (this) {
        is Result.Success -> this
        is Result.Error -> Result.Error(transform(error))
    }

/**
 * Recovers from an error by providing a fallback value.
 * Converts Error to Success using the recovery function.
 */
fun <D, E : RootError> Result<D, E>.recover(transform: (E) -> D): Result<D, E> =
    when (this) {
        is Result.Success -> this
        is Result.Error -> Result.Success(transform(error))
    }

// Side effect functions (don't break the chain)

/**
 * Executes a side effect if the result is Success.
 * Returns the original result unchanged, allowing chaining to continue.
 */
fun <D, E : RootError> Result<D, E>.onSuccess(action: (D) -> Unit): Result<D, E> {
    if (this is Result.Success) action(data)
    return this
}

/**
 * Executes a side effect if the result is Error.
 * Returns the original result unchanged, allowing chaining to continue.
 */
fun <D, E : RootError> Result<D, E>.onError(action: (E) -> Unit): Result<D, E> {
    if (this is Result.Error) action(error)
    return this
}

// Terminal operations (end the railway)

/**
 * Returns the success data or null if the result is an error.
 * This is a terminal operation that ends the railway chain.
 */
fun <D, E : RootError> Result<D, E>.getOrNull(): D? =
    when (this) {
        is Result.Success -> data
        is Result.Error -> null
    }

/**
 * Returns the success data or the result of fallback function if error.
 * This is a terminal operation that ends the railway chain.
 */
fun <D, E : RootError> Result<D, E>.getOrElse(fallback: (E) -> D): D =
    when (this) {
        is Result.Success -> data
        is Result.Error -> fallback(error)
    }
