package com.qodein.feature.promocode.detail

import android.content.Context
import com.qodein.shared.model.PromocodeId
import com.qodein.shared.model.UserId
import com.qodein.shared.model.VoteState

sealed class PromocodeDetailAction {
    data object RefreshData : PromocodeDetailAction()

    data class VoteClicked(val voteState: VoteState) : PromocodeDetailAction()

    data object ShareClicked : PromocodeDetailAction()

    data object BackClicked : PromocodeDetailAction()

    data class BlockUserClicked(val userId: UserId) : PromocodeDetailAction()
    data class ReportPromocodeClicked(val promocodeId: PromocodeId) : PromocodeDetailAction()

    data class SignInWithGoogleClicked(val context: Context) : PromocodeDetailAction()
    data object DismissAuthSheet : PromocodeDetailAction()

    data object RetryClicked : PromocodeDetailAction()
}
