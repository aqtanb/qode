package com.qodein.feature.promocode

sealed interface SubmissionUiState {

    data object Loading : SubmissionUiState

    data class Success(
        val serviceName: String = "",
        val promoCode: String = "",
        val description: String = "",
        val isSubmitting: Boolean = false
    ) : SubmissionUiState {
        val canSubmit: Boolean
            get() = serviceName.isNotBlank() && promoCode.isNotBlank() && !isSubmitting
    }

    data class Error(val exception: Throwable) : SubmissionUiState
}
