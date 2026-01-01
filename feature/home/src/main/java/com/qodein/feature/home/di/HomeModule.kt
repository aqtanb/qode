package com.qodein.feature.home.di

import com.qodein.feature.home.HomeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val homeModule = module {
    viewModel {
        HomeViewModel(
            savedStateHandle = get(),
            getBannersUseCase = get(),
            getPromocodesUseCase = get(),
            observeLanguageUseCase = get(),
            screenRefreshCoordinator = get(),
            analyticsHelper = get(),
            getServicesByIdsUseCase = get(),
        )
    }
}
