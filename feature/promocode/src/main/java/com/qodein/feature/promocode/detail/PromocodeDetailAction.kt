package com.qodein.feature.promocode.detail

import android.content.Context

sealed class PromocodeDetailAction {
    data object RefreshData : PromocodeDetailAction()

    data object UpvoteClicked : PromocodeDetailAction()
    data object DownvoteClicked : PromocodeDetailAction()

    data object CopyCodeClicked : PromocodeDetailAction()
    data object ShareClicked : PromocodeDetailAction()

    data object BackClicked : PromocodeDetailAction()

    data class SignInWithGoogleClicked(val context: Context) : PromocodeDetailAction()
    data object DismissAuthSheet : PromocodeDetailAction()

    data object RetryClicked : PromocodeDetailAction()
    data object ErrorDismissed : PromocodeDetailAction()
}
