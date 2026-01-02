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
            refreshCoordinator = get(),
            getServicesByIdsUseCase = get(),
        )
    }

    viewModel { (promoCodeIdString: String) ->
        PromocodeDetailViewModel(
            promoCodeIdString = promoCodeIdString,
            savedStateHandle = get(),
            getPromocodeUseCase = get(),
            getUserInteractionUseCase = get(),
            toggleVoteUseCase = get(),
            getAuthStateUseCase = get(),
        )
    }
}
