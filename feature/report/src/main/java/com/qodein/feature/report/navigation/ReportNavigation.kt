package com.qodein.feature.report.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.qodein.feature.report.ReportScreen
import com.qodein.shared.model.ContentType
import kotlinx.serialization.Serializable

@Serializable
data class ReportRoute(val reportedItemId: String, val reportedItemType: ContentType, val itemTitle: String, val itemAuthor: String? = null)

fun NavController.navigateToReport(
    reportedItemId: String,
    reportedItemType: ContentType,
    itemTitle: String,
    itemAuthor: String? = null,
    navOptions: NavOptions? = null
) {
    navigate(
        route = ReportRoute(
            reportedItemId = reportedItemId,
            reportedItemType = reportedItemType,
            itemTitle = itemTitle,
            itemAuthor = itemAuthor,
        ),
        navOptions = navOptions,
    )
}

fun NavGraphBuilder.reportSection(
    onNavigateBack: () -> Unit,
    onReportSubmitted: (ContentType) -> Unit
) {
    composable<ReportRoute> { backStackEntry ->
        val args = backStackEntry.toRoute<ReportRoute>()
        ReportScreen(
            reportedItemId = args.reportedItemId,
            reportedItemType = args.reportedItemType,
            itemTitle = args.itemTitle,
            itemAuthor = args.itemAuthor,
            onNavigateBack = onNavigateBack,
            onReportSubmitted = { onReportSubmitted(args.reportedItemType) },
        )
    }
}
