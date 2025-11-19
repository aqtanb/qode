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
 * Returns the success data or null if the result is an error.
 */
fun <D, E : RootError> Result<D, E>.getOrNull(): D? =
    when (this) {
        is Result.Success -> data
        is Result.Error -> null
    }

/**
 * Returns the success data or the result of fallback function if error.
 * Note: The fallback lambda cannot use `return` to exit the outer function.
 * For early returns, use a when expression instead.
 */
fun <D, E : RootError> Result<D, E>.getOrElse(fallback: (E) -> D): D =
    when (this) {
        is Result.Success -> data
        is Result.Error -> fallback(error)
    }
