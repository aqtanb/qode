package com.qodein.core.ui.error

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.qodein.core.ui.R
import com.qodein.shared.common.error.FirestoreError
import com.qodein.shared.common.error.InteractionError
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.PostError
import com.qodein.shared.common.error.PromoCodeError
import com.qodein.shared.common.error.ServiceError
import com.qodein.shared.common.error.StorageError
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
        UserError.AuthenticationFailure.Cancelled -> stringResource(R.string.error_auth_user_cancelled)
        UserError.AuthenticationFailure.InvalidCredentials -> stringResource(R.string.error_auth_invalid_credentials)
        UserError.AuthenticationFailure.ServiceUnavailable -> stringResource(R.string.error_auth_service_unavailable)
        UserError.AuthenticationFailure.TooManyAttempts -> stringResource(R.string.error_auth_too_many_attempts)

        UserError.ProfileFailure.NotFound -> stringResource(R.string.error_user_not_found)
        UserError.ProfileFailure.AccessDenied -> stringResource(R.string.error_user_profile_access_denied)
        UserError.ProfileFailure.DataCorrupted -> stringResource(R.string.error_user_data_corrupted)
        UserError.ProfileFailure.UpdateFailed -> stringResource(R.string.error_user_update_failed)

        PromoCodeError.SubmissionFailure.DuplicateCode -> stringResource(R.string.error_promo_code_already_exists)
        PromoCodeError.SubmissionFailure.NotAuthorized -> stringResource(R.string.error_promo_code_submission_not_authorized)
        PromoCodeError.SubmissionFailure.InvalidData -> stringResource(R.string.error_validation_invalid_format)

        PromoCodeError.RetrievalFailure.NotFound -> stringResource(R.string.error_promo_code_not_found)
        PromoCodeError.RetrievalFailure.NoResults -> stringResource(R.string.error_no_results)
        PromoCodeError.RetrievalFailure.TooManyResults -> stringResource(R.string.error_too_many_results)

        ServiceError.SearchFailure.NoResults -> stringResource(R.string.error_service_no_results)
        ServiceError.SearchFailure.QueryTooShort -> stringResource(R.string.error_service_query_too_short)
        ServiceError.SearchFailure.TooManyResults -> stringResource(R.string.error_service_too_many_results)
        ServiceError.SearchFailure.InvalidQuery -> stringResource(R.string.error_service_invalid_query)

        ServiceError.RetrievalFailure.NotFound -> stringResource(R.string.error_service_not_found)
        ServiceError.RetrievalFailure.DataCorrupted -> stringResource(R.string.error_service_data_corrupted)
        ServiceError.RetrievalFailure.CacheExpired -> stringResource(R.string.error_service_cache_expired)

        InteractionError.VotingFailure.NotAuthorized -> stringResource(R.string.error_interaction_vote_not_authorized)
        InteractionError.VotingFailure.AlreadyVoted -> stringResource(R.string.error_interaction_already_voted)
        InteractionError.VotingFailure.ContentNotFound -> stringResource(R.string.error_interaction_vote_content_not_found)
        InteractionError.VotingFailure.SaveFailed -> stringResource(R.string.error_interaction_vote_save_failed)

        InteractionError.BookmarkFailure.NotAuthorized -> stringResource(R.string.error_interaction_bookmark_not_authorized)
        InteractionError.BookmarkFailure.ContentNotFound -> stringResource(R.string.error_interaction_bookmark_content_not_found)
        InteractionError.BookmarkFailure.SaveFailed -> stringResource(R.string.error_interaction_bookmark_save_failed)
        InteractionError.BookmarkFailure.RemoveFailed -> stringResource(R.string.error_interaction_remove_failed)

        SystemError.Offline -> stringResource(R.string.error_network_no_connection)
        SystemError.ServiceDown -> stringResource(R.string.error_system_service_down)
        SystemError.Unknown -> stringResource(R.string.error_unknown)
        PostError.CreationFailure.ContentTooLong -> stringResource(R.string.error_post_content_too_long)
        PostError.CreationFailure.EmptyAuthorName -> stringResource(R.string.error_post_author_missing)
        PostError.CreationFailure.EmptyContent -> stringResource(R.string.error_post_content_empty)
        PostError.CreationFailure.EmptyTitle -> stringResource(R.string.error_post_title_empty)
        PostError.CreationFailure.InvalidTagData -> stringResource(R.string.error_post_invalid_tag)
        PostError.CreationFailure.TitleTooLong -> stringResource(R.string.error_post_title_too_long)
        PostError.CreationFailure.TooManyImages -> stringResource(R.string.error_post_too_many_images)
        PostError.CreationFailure.TooManyTags -> stringResource(R.string.error_post_too_many_tags)
        PostError.RetrievalFailure.AccessDenied -> stringResource(R.string.error_post_access_denied)
        PostError.RetrievalFailure.NoResults -> stringResource(R.string.error_post_no_results)
        PostError.RetrievalFailure.NotFound -> stringResource(R.string.error_post_not_found)
        PostError.SubmissionFailure.InvalidData -> stringResource(R.string.error_post_submission_invalid_data)
        PostError.SubmissionFailure.NotAuthorized -> stringResource(R.string.error_post_submission_not_authorized)
        SystemError.PermissionDenied -> stringResource(R.string.error_system_permission_denied)
        StorageError.DeletionFailure.FileNotFound -> stringResource(R.string.error_storage_delete_file_not_found)
        StorageError.RetrievalFailure.FileNotFound -> stringResource(R.string.error_storage_retrieve_file_not_found)
        StorageError.UploadFailure.CorruptedFile -> stringResource(R.string.error_storage_upload_corrupted_file)
        StorageError.UploadFailure.FileTooLarge -> stringResource(R.string.error_storage_upload_file_too_large)
        StorageError.UploadFailure.InvalidFileType -> stringResource(R.string.error_storage_upload_invalid_file_type)
        StorageError.UploadFailure.NotAuthenticated -> stringResource(R.string.error_storage_upload_not_authenticated)
        StorageError.UploadFailure.QuotaExceeded -> stringResource(R.string.error_storage_upload_quota_exceeded)
        StorageError.UploadFailure.UploadCancelled -> stringResource(R.string.error_storage_upload_cancelled)
        StorageError.CompressionFailure.CannotReadImage -> stringResource(R.string.error_storage_compression_cannot_read)
        StorageError.CompressionFailure.InvalidImageFormat -> stringResource(R.string.error_storage_compression_invalid_format)
        StorageError.CompressionFailure.OutOfMemory -> stringResource(R.string.error_storage_compression_out_of_memory)
        StorageError.CompressionFailure.CompressionFailed -> stringResource(R.string.error_storage_compression_failed)
        SystemError.Unauthorized -> stringResource(R.string.error_auth_unauthorized)
        FirestoreError.Cancelled -> stringResource(R.string.error_operation_cancelled)
        FirestoreError.InvalidArgument -> stringResource(R.string.error_firestore_invalid_argument)
        FirestoreError.DeadlineExceeded -> stringResource(R.string.error_request_timeout)
        FirestoreError.NotFound -> stringResource(R.string.error_not_found)
        FirestoreError.AlreadyExists -> stringResource(R.string.error_already_exists)
        FirestoreError.PermissionDenied -> stringResource(R.string.error_firestore_permission_denied)
        FirestoreError.ResourceExhausted -> stringResource(R.string.error_quota_exceeded)
        FirestoreError.FailedPrecondition -> stringResource(R.string.error_operation_failed)
        FirestoreError.Aborted -> stringResource(R.string.error_operation_aborted)
        FirestoreError.OutOfRange -> stringResource(R.string.error_firestore_out_of_range)
        FirestoreError.Unimplemented -> stringResource(R.string.error_not_supported)
        FirestoreError.Internal -> stringResource(R.string.error_firestore_internal)
        FirestoreError.Unavailable -> stringResource(R.string.error_firestore_unavailable)
        FirestoreError.DataLoss -> stringResource(R.string.error_data_corrupted)
        FirestoreError.Unauthenticated -> stringResource(R.string.error_auth_required)
    }
