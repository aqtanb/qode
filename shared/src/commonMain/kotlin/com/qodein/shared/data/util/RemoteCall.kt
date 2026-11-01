package com.qodein.shared.data.util

import co.touchlab.kermit.Logger
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.DatabaseError
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.SystemError
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.CancellationException

/** Runs a Supabase call and turns its failures into domain errors. */
suspend inline fun <T> remoteCall(
    operation: String,
    block: () -> T
): Result<T, OperationError> =
    try {
        Result.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: RestException) {
        Logger.w(e) { "$operation failed: ${e.statusCode}" }
        Result.Error(mapRestException(e))
    } catch (e: HttpRequestException) {
        Logger.w(e) { "$operation failed: network" }
        Result.Error(SystemError.Offline)
    } catch (e: HttpRequestTimeoutException) {
        Logger.w(e) { "$operation failed: timeout" }
        Result.Error(SystemError.Offline)
    } catch (e: Exception) {
        Logger.e(e) { "$operation failed" }
        Result.Error(SystemError.Unknown)
    }

fun mapRestException(e: RestException): DatabaseError {
    when ((e as? PostgrestRestException)?.code) {
        "42501" -> return DatabaseError.PermissionDenied
        "23505" -> return DatabaseError.AlreadyExists
        "23503", "23514", "22P02" -> return DatabaseError.InvalidArgument
    }
    return when (e.statusCode) {
        400, 422 -> DatabaseError.InvalidArgument
        401 -> DatabaseError.Unauthenticated
        403 -> DatabaseError.PermissionDenied
        404 -> DatabaseError.NotFound
        409 -> DatabaseError.AlreadyExists
        429 -> DatabaseError.ResourceExhausted
        503 -> DatabaseError.Unavailable
        504 -> DatabaseError.DeadlineExceeded
        else -> DatabaseError.Internal
    }
}
