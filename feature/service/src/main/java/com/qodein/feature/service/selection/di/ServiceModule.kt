package com.qodein.feature.service.selection.di

import com.qodein.feature.service.selection.ServiceSelectionViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val serviceModule = module {
    viewModel {
        ServiceSelectionViewModel(
            getPopularServicesUseCase = get(),
            searchServicesUseCase = get(),
        )
    }
}
