package com.qodein.feature.promocode.detail

import com.qodein.core.ui.AuthPromptAction
import com.qodein.shared.common.error.OperationError
import com.qodein.shared.model.Promocode

sealed class PromocodeDetailEvent {

    // Navigation Events
    data object NavigateBack : PromocodeDetailEvent()
    data class NavigateToAuth(val action: AuthPromptAction) : PromocodeDetailEvent()
    data class NavigateToReport(val reportedItemId: String, val itemTitle: String, val itemAuthor: String?) : PromocodeDetailEvent()

    // System Events
    data class SharePromocode(val promoCode: Promocode) : PromocodeDetailEvent()
    data class CopyCodeToClipboard(val code: String) : PromocodeDetailEvent()
    data class ShowError(val error: OperationError) : PromocodeDetailEvent()
}
