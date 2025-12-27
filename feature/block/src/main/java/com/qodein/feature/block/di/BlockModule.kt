package com.qodein.feature.block.di

import com.qodein.feature.block.BlockViewModel
import com.qodein.shared.model.UserId
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val blockModule = module {
    viewModel { (userId: UserId, username: String?, photoUrl: String?) ->
        BlockViewModel(
            savedStateHandle = get(),
            userId = userId,
            username = username,
            photoUrl = photoUrl,
            blockUserUseCase = get(),
            analyticsHelper = get(),
            refreshCoordinator = get(),
        )
    }
}
