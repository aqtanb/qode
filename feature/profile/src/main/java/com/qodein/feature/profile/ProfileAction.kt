package com.qodein.feature.profile

sealed interface ProfileAction {
    data object RetryClicked : ProfileAction
    data object BlockedClicked : ProfileAction
    data object SignOutClicked : ProfileAction
}
