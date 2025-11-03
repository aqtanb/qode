package com.qodein.feature.profile

sealed interface ProfileAction {
    data object SignOutClicked : ProfileAction
    data object RetryClicked : ProfileAction
}
