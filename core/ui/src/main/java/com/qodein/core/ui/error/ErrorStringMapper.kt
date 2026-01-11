package com.qodein.core.ui.error

import androidx.compose.runtime.Composable
import com.qodein.core.ui.R
import com.qodein.core.ui.text.UiText
import com.qodein.core.ui.text.UiText.StringResource
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
        UserError.AuthenticationFailure.Cancelled -> StringResource(R.string.error_auth_user_cancelled)
        UserError.AuthenticationFailure.InvalidCredentials -> StringResource(R.string.error_auth_invalid_credentials)
        UserError.AuthenticationFailure.ServiceUnavailable -> StringResource(R.string.error_auth_service_unavailable)
        UserError.AuthenticationFailure.TooManyAttempts -> StringResource(R.string.error_auth_too_many_attempts)
        UserError.ProfileFailure.NotFound -> StringResource(R.string.error_user_not_found)
        UserError.ProfileFailure.AccessDenied -> StringResource(R.string.error_user_profile_access_denied)
        UserError.ProfileFailure.DataCorrupted -> StringResource(R.string.error_user_data_corrupted)
        UserError.ProfileFailure.UpdateFailed -> StringResource(R.string.error_user_update_failed)
        UserError.DeletionFailure.NotAuthenticated -> StringResource(R.string.error_user_deletion_not_authenticated)
        UserError.DeletionFailure.DeleteFailed -> StringResource(R.string.error_user_deletion_failed)
        UserError.DeletionFailure.PartialDeletion -> StringResource(R.string.error_user_deletion_partial)

        ServiceError.SearchFailure.NoResults -> StringResource(R.string.error_service_no_results)
        ServiceError.SearchFailure.QueryTooShort -> StringResource(R.string.error_service_query_too_short)
        ServiceError.SearchFailure.TooManyResults -> StringResource(R.string.error_service_too_many_results)
        ServiceError.SearchFailure.InvalidQuery -> StringResource(R.string.error_service_invalid_query)
        ServiceError.RetrievalFailure.NotFound -> StringResource(R.string.error_service_not_found)
        ServiceError.RetrievalFailure.DataCorrupted -> StringResource(R.string.error_service_data_corrupted)
        ServiceError.RetrievalFailure.CacheExpired -> StringResource(R.string.error_service_cache_expired)

        LegalDocumentError.NotFound -> StringResource(R.string.error_legal_document_not_found)
        LegalDocumentError.Unavailable -> StringResource(R.string.error_legal_document_unavailable)

        InteractionError.VotingFailure.NotAuthorized -> StringResource(R.string.error_interaction_vote_not_authorized)
        InteractionError.VotingFailure.AlreadyVoted -> StringResource(R.string.error_interaction_already_voted)
        InteractionError.VotingFailure.ContentNotFound -> StringResource(R.string.error_interaction_vote_content_not_found)
        InteractionError.VotingFailure.SaveFailed -> StringResource(R.string.error_interaction_vote_save_failed)
        InteractionError.BookmarkFailure.NotAuthorized -> StringResource(R.string.error_interaction_bookmark_not_authorized)
        InteractionError.BookmarkFailure.ContentNotFound -> StringResource(R.string.error_interaction_bookmark_content_not_found)
        InteractionError.BookmarkFailure.SaveFailed -> StringResource(R.string.error_interaction_bookmark_save_failed)
        InteractionError.BookmarkFailure.RemoveFailed -> StringResource(R.string.error_interaction_remove_failed)

        SystemError.Offline -> StringResource(R.string.error_network_no_connection)
        SystemError.Unknown -> StringResource(R.string.error_unknown)

        PostError.CreationFailure.ContentTooLong -> StringResource(R.string.error_post_content_too_long)
        PostError.CreationFailure.EmptyAuthorName -> StringResource(R.string.error_post_author_missing)
        PostError.CreationFailure.EmptyContent -> StringResource(R.string.error_post_content_empty)
        PostError.CreationFailure.EmptyTitle -> StringResource(R.string.error_post_title_empty)
        PostError.CreationFailure.InvalidTagData -> StringResource(R.string.error_post_invalid_tag)
        PostError.CreationFailure.TitleTooLong -> StringResource(R.string.error_post_title_too_long)
        PostError.CreationFailure.TooManyImages -> StringResource(R.string.error_post_too_many_images)
        PostError.CreationFailure.TooManyTags -> StringResource(R.string.error_post_too_many_tags)
        PostError.RetrievalFailure.AccessDenied -> StringResource(R.string.error_post_access_denied)
        PostError.RetrievalFailure.NoResults -> StringResource(R.string.error_post_no_results)
        PostError.RetrievalFailure.NotFound -> StringResource(R.string.error_post_not_found)
        PostError.SubmissionFailure.InvalidData -> StringResource(R.string.error_post_submission_invalid_data)
        PostError.SubmissionFailure.NotAuthorized -> StringResource(R.string.error_post_submission_not_authorized)

        StorageError.DeletionFailure.FileNotFound -> StringResource(R.string.error_storage_delete_file_not_found)
        StorageError.RetrievalFailure.FileNotFound -> StringResource(R.string.error_storage_retrieve_file_not_found)
        StorageError.UploadFailure.CorruptedFile -> StringResource(R.string.error_storage_upload_corrupted_file)
        StorageError.UploadFailure.FileTooLarge -> StringResource(R.string.error_storage_upload_file_too_large)
        StorageError.UploadFailure.InvalidFileType -> StringResource(R.string.error_storage_upload_invalid_file_type)
        StorageError.UploadFailure.NotAuthenticated -> StringResource(R.string.error_storage_upload_not_authenticated)
        StorageError.UploadFailure.QuotaExceeded -> StringResource(R.string.error_storage_upload_quota_exceeded)
        StorageError.UploadFailure.UploadCancelled -> StringResource(R.string.error_storage_upload_cancelled)
        StorageError.CompressionFailure.CannotReadImage -> StringResource(R.string.error_storage_compression_cannot_read)
        StorageError.CompressionFailure.InvalidImageFormat -> StringResource(R.string.error_storage_compression_invalid_format)
        StorageError.CompressionFailure.OutOfMemory -> StringResource(R.string.error_storage_compression_out_of_memory)
        StorageError.CompressionFailure.CompressionFailed -> StringResource(R.string.error_storage_compression_failed)

        FirestoreError.Cancelled -> StringResource(R.string.error_operation_cancelled)
        FirestoreError.InvalidArgument -> StringResource(R.string.error_firestore_invalid_argument)
        FirestoreError.DeadlineExceeded -> StringResource(R.string.error_request_timeout)
        FirestoreError.NotFound -> StringResource(R.string.error_not_found)
        FirestoreError.AlreadyExists -> StringResource(R.string.error_already_exists)
        FirestoreError.PermissionDenied -> StringResource(R.string.error_firestore_permission_denied)
        FirestoreError.ResourceExhausted -> StringResource(R.string.error_quota_exceeded)
        FirestoreError.FailedPrecondition -> StringResource(R.string.error_operation_failed)
        FirestoreError.Aborted -> StringResource(R.string.error_operation_aborted)
        FirestoreError.OutOfRange -> StringResource(R.string.error_firestore_out_of_range)
        FirestoreError.Unimplemented -> StringResource(R.string.error_not_supported)
        FirestoreError.Internal -> StringResource(R.string.error_firestore_internal)
        FirestoreError.Unavailable -> StringResource(R.string.error_firestore_unavailable)
        FirestoreError.DataLoss -> StringResource(R.string.error_data_corrupted)
        FirestoreError.Unauthenticated -> StringResource(R.string.error_auth_required)

        AlgoliaError.IndexNotFound -> StringResource(R.string.error_algolia_index_not_found)
        AlgoliaError.InsufficientPermissions -> StringResource(R.string.error_algolia_insufficient_permissions)
        AlgoliaError.InvalidCredentials -> StringResource(R.string.error_algolia_invalid_credentials)
        AlgoliaError.InvalidQuery -> StringResource(R.string.error_algolia_invalid_query)
        AlgoliaError.NetworkError -> StringResource(R.string.error_algolia_network_error)
        AlgoliaError.RateLimitExceeded -> StringResource(R.string.error_algolia_rate_limit_exceeded)
        AlgoliaError.RequestTooLarge -> StringResource(R.string.error_algolia_request_too_large)
        AlgoliaError.ServerError -> StringResource(R.string.error_algolia_server_error)
        AlgoliaError.ServiceUnavailable -> StringResource(R.string.error_algolia_service_unavailable)
        AlgoliaError.Timeout -> StringResource(R.string.error_algolia_timeout)

        PromocodeError.CreationFailure.CodeTooLong -> StringResource(R.string.error_promo_code_too_long)
        PromocodeError.CreationFailure.CodeTooShort -> StringResource(R.string.error_promo_code_too_short)
        PromocodeError.CreationFailure.DescriptionTooLong -> StringResource(R.string.error_promo_description_too_long)
        PromocodeError.CreationFailure.DiscountExceedsMinimumAmount -> StringResource(R.string.error_promo_discount_exceeds_minimum)
        PromocodeError.CreationFailure.EmptyCode -> StringResource(R.string.error_promo_code_empty)
        PromocodeError.CreationFailure.InvalidDateRange -> StringResource(R.string.error_promo_invalid_date_range)
        PromocodeError.CreationFailure.InvalidFixedAmountDiscount -> StringResource(R.string.error_promo_invalid_fixed_amount)
        PromocodeError.CreationFailure.InvalidMinimumAmount -> StringResource(R.string.error_promo_invalid_minimum_amount)
        PromocodeError.CreationFailure.InvalidPercentageDiscount -> StringResource(R.string.error_promo_invalid_percentage)
        PromocodeError.CreationFailure.InvalidPromocodeId -> StringResource(R.string.error_promo_invalid_id)
        PromocodeError.SubmissionFailure.DuplicateCode -> StringResource(R.string.error_promo_code_already_exists)
        PromocodeError.CreationFailure.InvalidFreeItemDescription -> StringResource(R.string.error_promo_invalid_free_item_description)
        PromocodeError.CreationFailure.FreeItemDescriptionTooLong -> StringResource(R.string.error_promo_free_item_description_too_long)
        PromocodeError.CreationFailure.FreeItemDescriptionInvalidCharacters -> StringResource(
            R.string.error_promo_free_item_invalid_characters,
        )

        ServiceError.CreationFailure.EmptyName -> StringResource(R.string.error_service_name_empty)
        ServiceError.CreationFailure.InvalidServiceId -> StringResource(R.string.error_service_invalid_id)
        ServiceError.CreationFailure.NameTooLong -> StringResource(R.string.error_service_name_too_long)
        ServiceError.CreationFailure.NameTooShort -> StringResource(R.string.error_service_name_too_short)
        ServiceError.CreationFailure.InvalidNameCharacters -> StringResource(R.string.error_service_name_invalid_characters)
        ServiceError.CreationFailure.EmptySiteUrl -> StringResource(R.string.error_service_site_url_empty)
        ServiceError.CreationFailure.InvalidDomainFormat -> StringResource(R.string.error_service_invalid_domain_format)
        ServiceError.CreationFailure.LogoNotFound -> StringResource(R.string.error_service_logo_not_found)
        ServiceError.SubmissionFailure.DuplicateService -> StringResource(R.string.error_service_duplicate)
        ServiceError.SubmissionFailure.InvalidData -> StringResource(R.string.error_service_submission_invalid_data)
        ServiceError.SubmissionFailure.NotAuthorized -> StringResource(R.string.error_service_submission_not_authorized)

        UserError.CreationFailure.InvalidEmail -> StringResource(R.string.error_user_invalid_email)
        UserError.CreationFailure.InvalidPhotoUrl -> StringResource(R.string.error_user_invalid_photo_url)
        UserError.CreationFailure.InvalidUserId -> StringResource(R.string.error_user_invalid_id)
        UserError.AuthenticationFailure.AccountConflict -> StringResource(R.string.error_user_auth_account_conflict)
        UserError.AuthenticationFailure.AccountDisabled -> StringResource(R.string.error_user_auth_account_disabled)
        UserError.AuthenticationFailure.ConfigurationError -> StringResource(R.string.error_user_auth_configuration_error)
        UserError.AuthenticationFailure.NoCredentialsAvailable -> StringResource(R.string.error_user_auth_no_credentials)
        UserError.AuthenticationFailure.Unknown -> StringResource(R.string.error_user_auth_unknown)
        UserError.AuthenticationFailure.UnsupportedCredential -> StringResource(R.string.error_user_auth_unsupported_credential)
        UserError.CreationFailure.DisplayNameTooLong -> StringResource(R.string.error_user_display_name_too_long)
    }

/**
 * Converts an OperationError to a localized error message string.
 */
@Composable
fun OperationError.asUiText(): String = toUiText().asString()
