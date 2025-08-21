package com.qodein.core.ui.error

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.qodein.core.ui.R
import com.qodein.shared.common.result.ErrorAction
import com.qodein.shared.common.result.ErrorType

/**
 * Maps ErrorType enums to localized string resources.
 *
 * This is the ONLY place where error types are converted to user-facing strings.
 * Supports proper localization through Android string resources.
 */

/**
 * Converts an ErrorType to a localized error message string.
 */
@Composable
fun ErrorType.toLocalizedMessage(): String =
    when (this) {
        // Network errors
        ErrorType.NETWORK_TIMEOUT -> stringResource(R.string.error_network_timeout)
        ErrorType.NETWORK_NO_CONNECTION -> stringResource(R.string.error_network_no_connection)
        ErrorType.NETWORK_HOST_UNREACHABLE -> stringResource(R.string.error_network_host_unreachable)
        ErrorType.NETWORK_GENERAL -> stringResource(R.string.error_network_general)

        // Authentication errors
        ErrorType.AUTH_PERMISSION_DENIED -> stringResource(R.string.error_auth_permission_denied)
        ErrorType.AUTH_USER_CANCELLED -> stringResource(R.string.error_auth_user_cancelled)
        ErrorType.AUTH_INVALID_CREDENTIALS -> stringResource(R.string.error_auth_invalid_credentials)
        ErrorType.AUTH_USER_NOT_FOUND -> stringResource(R.string.error_auth_user_not_found)
        ErrorType.AUTH_UNAUTHORIZED -> stringResource(R.string.error_auth_unauthorized)

        // Promo code errors
        ErrorType.PROMO_CODE_NOT_FOUND -> stringResource(R.string.error_promo_code_not_found)
        ErrorType.PROMO_CODE_EXPIRED -> stringResource(R.string.error_promo_code_expired)
        ErrorType.PROMO_CODE_INACTIVE -> stringResource(R.string.error_promo_code_inactive)
        ErrorType.PROMO_CODE_INVALID -> stringResource(R.string.error_promo_code_invalid)
        ErrorType.PROMO_CODE_ALREADY_EXISTS -> stringResource(R.string.error_promo_code_already_exists)
        ErrorType.PROMO_CODE_ALREADY_USED -> stringResource(R.string.error_promo_code_already_used)
        ErrorType.PROMO_CODE_MINIMUM_ORDER_NOT_MET -> stringResource(R.string.error_promo_code_minimum_order_not_met)

        // User errors
        ErrorType.USER_NOT_FOUND -> stringResource(R.string.error_user_not_found)
        ErrorType.USER_BANNED -> stringResource(R.string.error_user_banned)
        ErrorType.USER_SUSPENDED -> stringResource(R.string.error_user_suspended)

        // Service errors
        ErrorType.SERVICE_NOT_FOUND -> stringResource(R.string.error_service_not_found)
        ErrorType.SERVICE_UNAVAILABLE -> stringResource(R.string.error_service_unavailable)

        // Validation errors
        ErrorType.VALIDATION_REQUIRED_FIELD -> stringResource(R.string.error_validation_required_field)
        ErrorType.VALIDATION_INVALID_FORMAT -> stringResource(R.string.error_validation_invalid_format)
        ErrorType.VALIDATION_TOO_SHORT -> stringResource(R.string.error_validation_too_short)
        ErrorType.VALIDATION_TOO_LONG -> stringResource(R.string.error_validation_too_long)

        // System errors
        ErrorType.SERVICE_UNAVAILABLE_GENERAL -> stringResource(R.string.error_service_unavailable_general)
        ErrorType.SERVICE_CONFIGURATION_ERROR -> stringResource(R.string.error_service_configuration_error)
        ErrorType.SERVICE_INITIALIZATION_ERROR -> stringResource(R.string.error_service_initialization_error)

        // Unknown error
        ErrorType.UNKNOWN_ERROR -> stringResource(R.string.error_unknown)
    }

/**
 * Converts an ErrorAction to a localized action button text.
 */
@Composable
fun ErrorAction.toLocalizedActionText(): String =
    when (this) {
        ErrorAction.RETRY -> stringResource(R.string.action_retry)
        ErrorAction.SIGN_IN -> stringResource(R.string.action_sign_in)
        ErrorAction.CHECK_NETWORK -> stringResource(R.string.action_check_network)
        ErrorAction.CONTACT_SUPPORT -> stringResource(R.string.action_contact_support)
        ErrorAction.DISMISS_ONLY -> stringResource(R.string.action_dismiss)
    }
