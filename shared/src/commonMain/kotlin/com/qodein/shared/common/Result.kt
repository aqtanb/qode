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

/**
 * Unwrap a Result, throwing if it contains an error.
 * Useful in previews/tests where failure should crash fast.
 */
inline fun <reified D, reified E : RootError> Result<D, E>.requireSuccess(message: String? = null): D =
    when (this) {
        is Result.Success -> data
        is Result.Error -> throw IllegalStateException(message ?: "Expected Success but got Error: ${error::class.simpleName}")
    }
