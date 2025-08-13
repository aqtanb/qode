package com.qodein.feature.profile

sealed interface ProfileEvent {
    data object EditProfileRequested : ProfileEvent
    data object SignedOut : ProfileEvent
    data object AchievementsRequested : ProfileEvent
    data object UserJourneyRequested : ProfileEvent
}
