package com.qodein.shared.domain.di

import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import com.qodein.shared.domain.usecase.auth.SignInWithGoogleUseCase
import com.qodein.shared.domain.usecase.auth.SignOutUseCase
import com.qodein.shared.domain.usecase.banner.GetBannersUseCase
import com.qodein.shared.domain.usecase.interaction.GetUserBookmarksUseCase
import com.qodein.shared.domain.usecase.interaction.GetUserInteractionUseCase
import com.qodein.shared.domain.usecase.interaction.ToggleBookmarkUseCase
import com.qodein.shared.domain.usecase.interaction.ToggleVoteUseCase
import com.qodein.shared.domain.usecase.legal.GetLegalDocumentUseCase
import com.qodein.shared.domain.usecase.post.GetPostByIdUseCase
import com.qodein.shared.domain.usecase.post.GetPostsUseCase
import com.qodein.shared.domain.usecase.post.SubmitPostUseCase
import com.qodein.shared.domain.usecase.preferences.GetThemeUseCase
import com.qodein.shared.domain.usecase.preferences.ObserveLanguageUseCase
import com.qodein.shared.domain.usecase.preferences.SetLanguageUseCase
import com.qodein.shared.domain.usecase.preferences.SetThemeUseCase
import com.qodein.shared.domain.usecase.promocode.GetPromocodeUseCase
import com.qodein.shared.domain.usecase.promocode.GetPromocodesUseCase
import com.qodein.shared.domain.usecase.promocode.SubmitPromocodeUseCase
import com.qodein.shared.domain.usecase.service.GetOrCreateServiceUseCase
import com.qodein.shared.domain.usecase.service.GetPopularServicesUseCase
import com.qodein.shared.domain.usecase.service.SearchServicesUseCase
import com.qodein.shared.domain.usecase.user.AcceptConsentAndCreateUserUseCase
import com.qodein.shared.domain.usecase.user.AcceptLegalPoliciesUseCase
import com.qodein.shared.domain.usecase.user.BlockUserUseCase
import com.qodein.shared.domain.usecase.user.EnsureUserExists
import com.qodein.shared.domain.usecase.user.GetBlockedUserIdsUseCase
import com.qodein.shared.domain.usecase.user.GetUserByIdUseCase
import com.qodein.shared.domain.usecase.user.ObserveUserUseCase
import org.koin.dsl.module
import kotlin.time.Clock

/**
 * Provides domain-layer use cases.
 */
val domainModule = module {
    single<Clock> { Clock.System }

    single { GetAuthStateUseCase(get(), get()) }
    single { SignOutUseCase(get()) }
    single { SignInWithGoogleUseCase(get(), get()) }

    single { EnsureUserExists(get()) }
    single { GetUserByIdUseCase(get()) }
    single { ObserveUserUseCase(get()) }
    single { AcceptConsentAndCreateUserUseCase(get(), get()) }
    single { AcceptLegalPoliciesUseCase(get()) }
    single { BlockUserUseCase(get(), get()) }
    single { GetBlockedUserIdsUseCase(get(), get()) }

    single { GetBannersUseCase(get()) }
    single { GetPromocodesUseCase(get(), get(), get()) }
    single { GetPromocodeUseCase(get()) }
    single { SubmitPromocodeUseCase(get(), get()) }

    single { GetPostsUseCase(get()) }
    single { GetPostByIdUseCase(get()) }
    single { SubmitPostUseCase(get(), get()) }

    single { GetThemeUseCase(get()) }
    single { ObserveLanguageUseCase(get()) }
    single { SetThemeUseCase(get()) }
    single { SetLanguageUseCase(get()) }

    single { ToggleVoteUseCase(get()) }
    single { ToggleBookmarkUseCase(get()) }
    single { GetUserBookmarksUseCase(get()) }
    single { GetUserInteractionUseCase(get()) }

    single { GetPopularServicesUseCase(get()) }
    single { SearchServicesUseCase(get(), get()) }
    single { GetOrCreateServiceUseCase(get()) }

    single { GetLegalDocumentUseCase(get()) }
}
