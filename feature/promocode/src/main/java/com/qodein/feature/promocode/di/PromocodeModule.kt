package com.qodein.feature.promocode.di

import com.qodein.feature.promocode.detail.PromocodeDetailViewModel
import com.qodein.feature.promocode.submission.PromocodeSubmissionViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val promocodeModule = module {
    viewModel {
        PromocodeSubmissionViewModel(
            savedStateHandle = get(),
            submitPromocodeUseCase = get(),
            analyticsHelper = get(),
            getAuthStateUseCase = get(),
            getUserByIdUseCase = get(),
            getServicesByIdsUseCase = get(),
            getServiceLogoUrlUseCase = get(),
        )
    }

    viewModel {
        PromocodeDetailViewModel(
            savedStateHandle = get(),
            getPromocodeUseCase = get(),
            getUserInteractionUseCase = get(),
            toggleVoteUseCase = get(),
            getAuthStateUseCase = get(),
            getPromocodeShareContentUseCase = get(),
        )
    }
}
