package com.qodein.shared.data.repository

import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.common.error.UserError
import com.qodein.shared.data.datasource.SupabaseAuthDataSource
import com.qodein.shared.domain.repository.AuthRepository
import com.qodein.shared.model.GoogleAuthResult
import com.qodein.shared.model.GoogleIdToken
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.exceptions.HttpRequestException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

class AuthRepositoryImpl(private val dataSource: SupabaseAuthDataSource) : AuthRepository {
    override suspend fun signInWithGoogle(token: GoogleIdToken): Result<GoogleAuthResult, OperationError> =
        authCall(invalidCredentials = UserError.AuthenticationFailure.InvalidCredentials) {
            dataSource.signInWithGoogle(token.idToken, token.nonce)?.toAuthResult()
        }

    override suspend fun sendEmailCode(email: String): Result<Unit, OperationError> =
        authCall(invalidCredentials = UserError.AuthenticationFailure.InvalidEmail) {
            dataSource.sendEmailCode(email)
        }

    override suspend fun signInWithEmailCode(
        email: String,
        code: String
    ): Result<GoogleAuthResult, OperationError> =
        authCall(invalidCredentials = UserError.AuthenticationFailure.InvalidCode) {
            dataSource.verifyEmailCode(email, code)?.toAuthResult()
        }

    override suspend fun signOut() {
        dataSource.signOut()
    }

    override fun observeAuthState(): Flow<GoogleAuthResult?> =
        dataSource.observeAuthState().map { it?.toAuthResult() }.distinctUntilChanged()

    private inline fun <T : Any> authCall(
        invalidCredentials: OperationError,
        block: () -> T?
    ): Result<T, OperationError> =
        try {
            block()?.let { Result.Success(it) } ?: Result.Error(UserError.AuthenticationFailure.Unknown)
        } catch (e: CancellationException) {
            throw e
        } catch (e: AuthRestException) {
            Result.Error(
                when (e.statusCode) {
                    400, 401, 403, 422 -> invalidCredentials
                    429 -> UserError.AuthenticationFailure.TooManyAttempts
                    else -> UserError.AuthenticationFailure.Unknown
                },
            )
        } catch (e: HttpRequestException) {
            Result.Error(SystemError.Offline)
        } catch (e: Exception) {
            Result.Error(SystemError.Unknown)
        }

    private fun UserInfo.toAuthResult(): GoogleAuthResult =
        GoogleAuthResult(
            uid = id,
            email = email,
            displayName = metadataText("full_name") ?: metadataText("name"),
            photoUrl = metadataText("avatar_url") ?: metadataText("picture"),
        )

    private fun UserInfo.metadataText(key: String): String? = (userMetadata?.get(key) as? JsonPrimitive)?.contentOrNull
}
