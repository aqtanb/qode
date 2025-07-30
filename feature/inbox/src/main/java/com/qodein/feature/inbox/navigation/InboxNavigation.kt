package com.qodein.feature.inbox.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.qodein.feature.inbox.InboxScreen
import kotlinx.serialization.Serializable

@Serializable
object InboxBaseRoute

@Serializable
object InboxRoute

fun NavController.navigateToInbox() {
    navigate(InboxRoute)
}

fun NavGraphBuilder.inboxSection() {
    composable<InboxBaseRoute> {
        InboxScreen()
    }
    composable<InboxRoute> {
        InboxScreen()
    }
}
