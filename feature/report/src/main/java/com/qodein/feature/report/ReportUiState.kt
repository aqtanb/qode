package com.qodein.feature.report

import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.ReportReason

sealed interface ReportUiState {
    data class Input(
        val selectedReason: ReportReason? = null,
        val additionalDetails: String = "",
        val validationErrorResId: Int? = null,
        val isSubmitting: Boolean = false
    ) : ReportUiState

    data object Success : ReportUiState

    data class Error(val error: OperationError) : ReportUiState
}
