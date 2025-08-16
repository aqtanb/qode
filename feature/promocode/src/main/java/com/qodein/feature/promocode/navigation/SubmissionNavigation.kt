package com.qodein.feature.promocode.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.qodein.feature.promocode.submission.SubmissionWizardScreen
import kotlinx.serialization.Serializable

@Serializable object SubmissionRoute

fun NavController.navigateToSubmission(navOptions: NavOptions? = null) = navigate(route = SubmissionRoute, navOptions = navOptions)

fun NavGraphBuilder.submissionSection(onNavigateBack: () -> Unit = {}) {
    composable<SubmissionRoute> {
        SubmissionWizardScreen(
            onNavigateBack = onNavigateBack,
        )
    }
}
