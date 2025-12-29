package com.qodein.feature.report.di

import com.qodein.feature.report.ReportViewModel
import com.qodein.shared.model.ContentType
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val reportModule = module {
    viewModel { (reportedItemId: String, reportedItemType: ContentType) ->
        ReportViewModel(
            reportedItemId = reportedItemId,
            reportedItemType = reportedItemType,
            reportRepository = get(),
            getAuthStateUseCase = get(),
            analyticsHelper = get(),
        )
    }
}
