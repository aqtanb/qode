package com.qodein.feature.profile

sealed interface ProfileAction {
    data object SignOutClicked : ProfileAction
    data object RetryClicked : ProfileAction
    data object EditProfileClicked : ProfileAction
    data object AchievementsClicked : ProfileAction
    data object UserJourneyClicked : ProfileAction
}
