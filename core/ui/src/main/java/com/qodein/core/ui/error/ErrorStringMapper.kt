package com.qodein.core.ui.error

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.qodein.core.ui.R
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.InteractionError
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.PromoCodeError
import com.qodein.shared.common.error.ServiceError
import com.qodein.shared.common.error.SystemError
import com.qodein.shared.common.error.UserError

/**
 * Maps OperationError types to localized string resources.
 *
 * This is the ONLY place where error types are converted to user-facing strings.
 * Supports proper localization through Android string resources.
 */

/**
 * Converts an OperationError to a localized error message string.
 */
@Composable
fun OperationError.asUiText(): String =
    when (this) {
        // User/Auth errors
        is UserError.AuthenticationFailure.Cancelled -> stringResource(R.string.error_auth_user_cancelled)
        is UserError.AuthenticationFailure.InvalidCredentials -> stringResource(R.string.error_auth_invalid_credentials)
        is UserError.AuthenticationFailure.ServiceUnavailable -> stringResource(R.string.error_auth_service_unavailable)
        is UserError.AuthenticationFailure.TooManyAttempts -> stringResource(R.string.error_auth_too_many_attempts)

        is UserError.ProfileFailure.NotFound -> stringResource(R.string.error_user_not_found)
        is UserError.ProfileFailure.AccessDenied -> stringResource(R.string.error_auth_permission_denied)
        is UserError.ProfileFailure.DataCorrupted -> stringResource(R.string.error_user_data_corrupted)
        is UserError.ProfileFailure.UpdateFailed -> stringResource(R.string.error_user_update_failed)

        // PromoCode errors
        is PromoCodeError.SubmissionFailure.DuplicateCode -> stringResource(R.string.error_promo_code_already_exists)
        is PromoCodeError.SubmissionFailure.NotAuthorized -> stringResource(R.string.error_auth_permission_denied)
        is PromoCodeError.SubmissionFailure.InvalidData -> stringResource(R.string.error_validation_invalid_format)

        is PromoCodeError.RetrievalFailure.NotFound -> stringResource(R.string.error_promo_code_not_found)
        is PromoCodeError.RetrievalFailure.NoResults -> stringResource(R.string.error_no_results)
        is PromoCodeError.RetrievalFailure.TooManyResults -> stringResource(R.string.error_too_many_results)

        // Service errors
        is ServiceError.SearchFailure.NoResults -> stringResource(R.string.error_service_no_results)
        is ServiceError.SearchFailure.QueryTooShort -> stringResource(R.string.error_service_query_too_short)
        is ServiceError.SearchFailure.TooManyResults -> stringResource(R.string.error_service_too_many_results)
        is ServiceError.SearchFailure.InvalidQuery -> stringResource(R.string.error_service_invalid_query)

        is ServiceError.RetrievalFailure.NotFound -> stringResource(R.string.error_service_not_found)
        is ServiceError.RetrievalFailure.DataCorrupted -> stringResource(R.string.error_service_data_corrupted)
        is ServiceError.RetrievalFailure.CacheExpired -> stringResource(R.string.error_service_cache_expired)

        // Interaction errors
        is InteractionError.VotingFailure.NotAuthorized -> stringResource(R.string.error_auth_permission_denied)
        is InteractionError.VotingFailure.AlreadyVoted -> stringResource(R.string.error_interaction_already_voted)
        is InteractionError.VotingFailure.ContentNotFound -> stringResource(R.string.error_interaction_content_not_found)
        is InteractionError.VotingFailure.SaveFailed -> stringResource(R.string.error_interaction_save_failed)

        is InteractionError.BookmarkFailure.NotAuthorized -> stringResource(R.string.error_auth_permission_denied)
        is InteractionError.BookmarkFailure.ContentNotFound -> stringResource(R.string.error_interaction_content_not_found)
        is InteractionError.BookmarkFailure.SaveFailed -> stringResource(R.string.error_interaction_save_failed)
        is InteractionError.BookmarkFailure.RemoveFailed -> stringResource(R.string.error_interaction_remove_failed)

        is InteractionError.SystemFailure.Offline -> stringResource(R.string.error_network_no_connection)
        is InteractionError.SystemFailure.ServiceDown -> stringResource(R.string.error_service_unavailable)
        is InteractionError.SystemFailure.Unknown -> stringResource(R.string.error_unknown)

        // System errors
        is SystemError.Offline -> stringResource(R.string.error_network_no_connection)
        is SystemError.ServiceDown -> stringResource(R.string.error_service_unavailable)
        is SystemError.Unknown -> stringResource(R.string.error_unknown)
    }

/**
 * Convenience extension for Result.Error to get UI text directly.
 */
@Composable
fun <T> Result.Error<OperationError>.asErrorUiText(): String = error.asUiText()
