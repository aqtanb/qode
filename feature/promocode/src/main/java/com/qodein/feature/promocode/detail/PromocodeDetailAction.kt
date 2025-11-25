package com.qodein.feature.promocode.detail

import android.content.Context

sealed class PromocodeDetailAction {

    // Data Loading Actions
    data object RefreshData : PromocodeDetailAction()

    // Voting Actions
    data object UpvoteClicked : PromocodeDetailAction()
    data object DownvoteClicked : PromocodeDetailAction()

    // Interaction Actions
    data object CopyCodeClicked : PromocodeDetailAction()
    data object ShareClicked : PromocodeDetailAction()
    data object BookmarkToggleClicked : PromocodeDetailAction()
    data object CommentsClicked : PromocodeDetailAction()

    // Follow Actions (TODO implementations as requested)
    data object FollowServiceClicked : PromocodeDetailAction()

    // Navigation Actions
    data object BackClicked : PromocodeDetailAction()
    data object ServiceClicked : PromocodeDetailAction()

    // Authentication Actions
    data class SignInWithGoogleClicked(val context: Context) : PromocodeDetailAction()
    data object DismissAuthSheet : PromocodeDetailAction()

    // Error Handling Actions
    data object RetryClicked : PromocodeDetailAction()
    data object ErrorDismissed : PromocodeDetailAction()
}
