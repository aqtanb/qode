package com.qodein.core.ui.error

import androidx.compose.runtime.Composable
import com.qodein.core.ui.R
import com.qodein.core.ui.text.UiText
import com.qodein.core.ui.text.asString
import com.qodein.shared.common.error.AlgoliaError
import com.qodein.shared.common.error.FirestoreError
import com.qodein.shared.common.error.InteractionError
import com.qodein.shared.common.error.LegalDocumentError
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.common.error.PostError
import com.qodein.shared.common.error.PromocodeError
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
 * Converts an OperationError to a localized message representation.
 *
 * Use this from ViewModels/events when you need localized text without a Context.
 */
fun OperationError.toUiText(): UiText =
    when (this) {
        UserError.AuthenticationFailure.Cancelled -> UiText.StringResource(R.string.error_auth_user_cancelled)
        UserError.AuthenticationFailure.InvalidCredentials -> UiText.StringResource(R.string.error_auth_invalid_credentials)
        UserError.AuthenticationFailure.ServiceUnavailable -> UiText.StringResource(R.string.error_auth_service_unavailable)
        UserError.AuthenticationFailure.TooManyAttempts -> UiText.StringResource(R.string.error_auth_too_many_attempts)
        UserError.ProfileFailure.NotFound -> UiText.StringResource(R.string.error_user_not_found)
        UserError.ProfileFailure.AccessDenied -> UiText.StringResource(R.string.error_user_profile_access_denied)
        UserError.ProfileFailure.DataCorrupted -> UiText.StringResource(R.string.error_user_data_corrupted)
        UserError.ProfileFailure.UpdateFailed -> UiText.StringResource(R.string.error_user_update_failed)
        UserError.DeletionFailure.NotAuthenticated -> UiText.StringResource(R.string.error_user_deletion_not_authenticated)
        UserError.DeletionFailure.DeleteFailed -> UiText.StringResource(R.string.error_user_deletion_failed)
        UserError.DeletionFailure.PartialDeletion -> UiText.StringResource(R.string.error_user_deletion_partial)

        ServiceError.SearchFailure.NoResults -> UiText.StringResource(R.string.error_service_no_results)
        ServiceError.SearchFailure.QueryTooShort -> UiText.StringResource(R.string.error_service_query_too_short)
        ServiceError.SearchFailure.TooManyResults -> UiText.StringResource(R.string.error_service_too_many_results)
        ServiceError.SearchFailure.InvalidQuery -> UiText.StringResource(R.string.error_service_invalid_query)
        ServiceError.RetrievalFailure.NotFound -> UiText.StringResource(R.string.error_service_not_found)
        ServiceError.RetrievalFailure.DataCorrupted -> UiText.StringResource(R.string.error_service_data_corrupted)
        ServiceError.RetrievalFailure.CacheExpired -> UiText.StringResource(R.string.error_service_cache_expired)

        LegalDocumentError.NotFound -> UiText.StringResource(R.string.error_legal_document_not_found)
        LegalDocumentError.Unavailable -> UiText.StringResource(R.string.error_legal_document_unavailable)

        InteractionError.VotingFailure.NotAuthorized -> UiText.StringResource(R.string.error_interaction_vote_not_authorized)
        InteractionError.VotingFailure.AlreadyVoted -> UiText.StringResource(R.string.error_interaction_already_voted)
        InteractionError.VotingFailure.ContentNotFound -> UiText.StringResource(R.string.error_interaction_vote_content_not_found)
        InteractionError.VotingFailure.SaveFailed -> UiText.StringResource(R.string.error_interaction_vote_save_failed)
        InteractionError.BookmarkFailure.NotAuthorized -> UiText.StringResource(R.string.error_interaction_bookmark_not_authorized)
        InteractionError.BookmarkFailure.ContentNotFound -> UiText.StringResource(R.string.error_interaction_bookmark_content_not_found)
        InteractionError.BookmarkFailure.SaveFailed -> UiText.StringResource(R.string.error_interaction_bookmark_save_failed)
        InteractionError.BookmarkFailure.RemoveFailed -> UiText.StringResource(R.string.error_interaction_remove_failed)

        SystemError.Offline -> UiText.StringResource(R.string.error_network_no_connection)
        SystemError.Unknown -> UiText.StringResource(R.string.error_unknown)

        PostError.CreationFailure.ContentTooLong -> UiText.StringResource(R.string.error_post_content_too_long)
        PostError.CreationFailure.EmptyAuthorName -> UiText.StringResource(R.string.error_post_author_missing)
        PostError.CreationFailure.EmptyContent -> UiText.StringResource(R.string.error_post_content_empty)
        PostError.CreationFailure.EmptyTitle -> UiText.StringResource(R.string.error_post_title_empty)
        PostError.CreationFailure.InvalidTagData -> UiText.StringResource(R.string.error_post_invalid_tag)
        PostError.CreationFailure.TitleTooLong -> UiText.StringResource(R.string.error_post_title_too_long)
        PostError.CreationFailure.TooManyImages -> UiText.StringResource(R.string.error_post_too_many_images)
        PostError.CreationFailure.TooManyTags -> UiText.StringResource(R.string.error_post_too_many_tags)
        PostError.RetrievalFailure.AccessDenied -> UiText.StringResource(R.string.error_post_access_denied)
        PostError.RetrievalFailure.NoResults -> UiText.StringResource(R.string.error_post_no_results)
        PostError.RetrievalFailure.NotFound -> UiText.StringResource(R.string.error_post_not_found)
        PostError.SubmissionFailure.InvalidData -> UiText.StringResource(R.string.error_post_submission_invalid_data)
        PostError.SubmissionFailure.NotAuthorized -> UiText.StringResource(R.string.error_post_submission_not_authorized)

        StorageError.DeletionFailure.FileNotFound -> UiText.StringResource(R.string.error_storage_delete_file_not_found)
        StorageError.RetrievalFailure.FileNotFound -> UiText.StringResource(R.string.error_storage_retrieve_file_not_found)
        StorageError.UploadFailure.CorruptedFile -> UiText.StringResource(R.string.error_storage_upload_corrupted_file)
        StorageError.UploadFailure.FileTooLarge -> UiText.StringResource(R.string.error_storage_upload_file_too_large)
        StorageError.UploadFailure.InvalidFileType -> UiText.StringResource(R.string.error_storage_upload_invalid_file_type)
        StorageError.UploadFailure.NotAuthenticated -> UiText.StringResource(R.string.error_storage_upload_not_authenticated)
        StorageError.UploadFailure.QuotaExceeded -> UiText.StringResource(R.string.error_storage_upload_quota_exceeded)
        StorageError.UploadFailure.UploadCancelled -> UiText.StringResource(R.string.error_storage_upload_cancelled)
        StorageError.CompressionFailure.CannotReadImage -> UiText.StringResource(R.string.error_storage_compression_cannot_read)
        StorageError.CompressionFailure.InvalidImageFormat -> UiText.StringResource(R.string.error_storage_compression_invalid_format)
        StorageError.CompressionFailure.OutOfMemory -> UiText.StringResource(R.string.error_storage_compression_out_of_memory)
        StorageError.CompressionFailure.CompressionFailed -> UiText.StringResource(R.string.error_storage_compression_failed)

        FirestoreError.Cancelled -> UiText.StringResource(R.string.error_operation_cancelled)
        FirestoreError.InvalidArgument -> UiText.StringResource(R.string.error_firestore_invalid_argument)
        FirestoreError.DeadlineExceeded -> UiText.StringResource(R.string.error_request_timeout)
        FirestoreError.NotFound -> UiText.StringResource(R.string.error_not_found)
        FirestoreError.AlreadyExists -> UiText.StringResource(R.string.error_already_exists)
        FirestoreError.PermissionDenied -> UiText.StringResource(R.string.error_firestore_permission_denied)
        FirestoreError.ResourceExhausted -> UiText.StringResource(R.string.error_quota_exceeded)
        FirestoreError.FailedPrecondition -> UiText.StringResource(R.string.error_operation_failed)
        FirestoreError.Aborted -> UiText.StringResource(R.string.error_operation_aborted)
        FirestoreError.OutOfRange -> UiText.StringResource(R.string.error_firestore_out_of_range)
        FirestoreError.Unimplemented -> UiText.StringResource(R.string.error_not_supported)
        FirestoreError.Internal -> UiText.StringResource(R.string.error_firestore_internal)
        FirestoreError.Unavailable -> UiText.StringResource(R.string.error_firestore_unavailable)
        FirestoreError.DataLoss -> UiText.StringResource(R.string.error_data_corrupted)
        FirestoreError.Unauthenticated -> UiText.StringResource(R.string.error_auth_required)

        AlgoliaError.IndexNotFound -> UiText.StringResource(R.string.error_algolia_index_not_found)
        AlgoliaError.InsufficientPermissions -> UiText.StringResource(R.string.error_algolia_insufficient_permissions)
        AlgoliaError.InvalidCredentials -> UiText.StringResource(R.string.error_algolia_invalid_credentials)
        AlgoliaError.InvalidQuery -> UiText.StringResource(R.string.error_algolia_invalid_query)
        AlgoliaError.NetworkError -> UiText.StringResource(R.string.error_algolia_network_error)
        AlgoliaError.RateLimitExceeded -> UiText.StringResource(R.string.error_algolia_rate_limit_exceeded)
        AlgoliaError.RequestTooLarge -> UiText.StringResource(R.string.error_algolia_request_too_large)
        AlgoliaError.ServerError -> UiText.StringResource(R.string.error_algolia_server_error)
        AlgoliaError.ServiceUnavailable -> UiText.StringResource(R.string.error_algolia_service_unavailable)
        AlgoliaError.Timeout -> UiText.StringResource(R.string.error_algolia_timeout)

        PromocodeError.CreationFailure.CodeTooLong -> UiText.StringResource(R.string.error_promo_code_too_long)
        PromocodeError.CreationFailure.CodeTooShort -> UiText.StringResource(R.string.error_promo_code_too_short)
        PromocodeError.CreationFailure.DescriptionTooLong -> UiText.StringResource(R.string.error_promo_description_too_long)
        PromocodeError.CreationFailure.DiscountExceedsMinimumAmount -> UiText.StringResource(R.string.error_promo_discount_exceeds_minimum)
        PromocodeError.CreationFailure.EmptyCode -> UiText.StringResource(R.string.error_promo_code_empty)
        PromocodeError.CreationFailure.InvalidDateRange -> UiText.StringResource(R.string.error_promo_invalid_date_range)
        PromocodeError.CreationFailure.InvalidFixedAmountDiscount -> UiText.StringResource(R.string.error_promo_invalid_fixed_amount)
        PromocodeError.CreationFailure.InvalidMinimumAmount -> UiText.StringResource(R.string.error_promo_invalid_minimum_amount)
        PromocodeError.CreationFailure.InvalidPercentageDiscount -> UiText.StringResource(R.string.error_promo_invalid_percentage)
        PromocodeError.CreationFailure.InvalidPromocodeId -> UiText.StringResource(R.string.error_promo_invalid_id)
        PromocodeError.SubmissionFailure.DuplicateCode -> UiText.StringResource(R.string.error_promo_code_already_exists)

        ServiceError.CreationFailure.EmptyName -> UiText.StringResource(R.string.error_service_name_empty)
        ServiceError.CreationFailure.InvalidServiceId -> UiText.StringResource(R.string.error_service_invalid_id)
        ServiceError.CreationFailure.NameTooLong -> UiText.StringResource(R.string.error_service_name_too_long)
        ServiceError.CreationFailure.NameTooShort -> UiText.StringResource(R.string.error_service_name_too_short)
        ServiceError.CreationFailure.EmptySiteUrl -> UiText.StringResource(R.string.error_service_site_url_empty)
        ServiceError.CreationFailure.InvalidDomainFormat -> UiText.StringResource(R.string.error_service_invalid_domain_format)
        ServiceError.CreationFailure.LogoNotFound -> UiText.StringResource(R.string.error_service_logo_not_found)
        ServiceError.SubmissionFailure.DuplicateService -> UiText.StringResource(R.string.error_service_duplicate)
        ServiceError.SubmissionFailure.InvalidData -> UiText.StringResource(R.string.error_service_submission_invalid_data)
        ServiceError.SubmissionFailure.NotAuthorized -> UiText.StringResource(R.string.error_service_submission_not_authorized)

        UserError.CreationFailure.InvalidEmail -> UiText.StringResource(R.string.error_user_invalid_email)
        UserError.CreationFailure.InvalidPhotoUrl -> UiText.StringResource(R.string.error_user_invalid_photo_url)
        UserError.CreationFailure.InvalidUserId -> UiText.StringResource(R.string.error_user_invalid_id)
        UserError.AuthenticationFailure.AccountConflict -> UiText.StringResource(R.string.error_user_auth_account_conflict)
        UserError.AuthenticationFailure.AccountDisabled -> UiText.StringResource(R.string.error_user_auth_account_disabled)
        UserError.AuthenticationFailure.ConfigurationError -> UiText.StringResource(R.string.error_user_auth_configuration_error)
        UserError.AuthenticationFailure.NoCredentialsAvailable -> UiText.StringResource(R.string.error_user_auth_no_credentials)
        UserError.AuthenticationFailure.Unknown -> UiText.StringResource(R.string.error_user_auth_unknown)
        UserError.AuthenticationFailure.UnsupportedCredential -> UiText.StringResource(R.string.error_user_auth_unsupported_credential)
        UserError.CreationFailure.DisplayNameTooLong -> UiText.StringResource(R.string.error_user_display_name_too_long)
    }

/**
 * Converts an OperationError to a localized error message string.
 */
@Composable
fun OperationError.asUiText(): String = toUiText().asString()
