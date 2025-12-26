package com.qodein.feature.report

import com.qodein.shared.model.ReportReason

sealed interface ReportAction {
    data class SelectReason(val reason: ReportReason) : ReportAction

    data class UpdateAdditionalDetails(val text: String) : ReportAction

    data object Submit : ReportAction

    data object NavigateBack : ReportAction

    data object DismissError : ReportAction
}
