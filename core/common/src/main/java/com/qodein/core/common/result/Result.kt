package com.qodein.core.common.result

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

/**
 * A generic sealed interface that represents the result of an operation.
 */
sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val exception: Throwable) : Result<Nothing>
    data object Loading : Result<Nothing>
}

/**
 * Extension function to convert Flow<T> to Flow<Result<T>>
 * Catches all exceptions and converts them to Result.Error following NIA patterns.
 */
fun <T> Flow<T>.asResult(): Flow<Result<T>> =
    map<T, Result<T>> {
        Result.Success(it)
    }.onStart {
        emit(Result.Loading)
    }.catch {
        emit(Result.Error(it))
    }

/**
 * Extension function for suspend functions to return Result<T>
 */
suspend inline fun <T> resultOf(block: () -> T): Result<T> =
    try {
        Result.Success(block())
    } catch (e: Exception) {
        Result.Error(e)
    }
