package com.qodein.shared.domain.di

import com.qodein.shared.domain.DeeplinkConfig
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import com.qodein.shared.domain.usecase.auth.SignInWithGoogleUseCase
import com.qodein.shared.domain.usecase.auth.SignOutUseCase
import com.qodein.shared.domain.usecase.banner.GetBannersUseCase
import com.qodein.shared.domain.usecase.interaction.GetUserInteractionUseCase
import com.qodein.shared.domain.usecase.interaction.ToggleVoteUseCase
import com.qodein.shared.domain.usecase.legal.GetLegalDocumentUseCase
import com.qodein.shared.domain.usecase.post.EnqueuePostSubmissionUseCase
import com.qodein.shared.domain.usecase.post.GetPostByIdUseCase
import com.qodein.shared.domain.usecase.post.GetPostShareContentUseCase
import com.qodein.shared.domain.usecase.post.GetPostsByUserUseCase
import com.qodein.shared.domain.usecase.post.GetPostsUseCase
import com.qodein.shared.domain.usecase.post.SubmitPostUseCase
import com.qodein.shared.domain.usecase.preferences.GetThemeUseCase
import com.qodein.shared.domain.usecase.preferences.ObserveLanguageUseCase
import com.qodein.shared.domain.usecase.preferences.SetLanguageUseCase
import com.qodein.shared.domain.usecase.preferences.SetThemeUseCase
import com.qodein.shared.domain.usecase.promocode.EnqueuePromocodeSubmissionUseCase
import com.qodein.shared.domain.usecase.promocode.GetPromocodeShareContentUseCase
import com.qodein.shared.domain.usecase.promocode.GetPromocodeUseCase
import com.qodein.shared.domain.usecase.promocode.GetPromocodesByUserUseCase
import com.qodein.shared.domain.usecase.promocode.GetPromocodesUseCase
import com.qodein.shared.domain.usecase.promocode.SubmitPromocodeUseCase
import com.qodein.shared.domain.usecase.report.GetReportedContentIdsUseCase
import com.qodein.shared.domain.usecase.service.GetOrCreateServiceUseCase
import com.qodein.shared.domain.usecase.service.GetPopularServicesUseCase
import com.qodein.shared.domain.usecase.service.GetServiceLogoUrlUseCase
import com.qodein.shared.domain.usecase.service.GetServicesByIdsUseCase
import com.qodein.shared.domain.usecase.service.SearchServicesUseCase
import com.qodein.shared.domain.usecase.user.AcceptConsentAndCreateUserUseCase
import com.qodein.shared.domain.usecase.user.AcceptLegalPoliciesUseCase
import com.qodein.shared.domain.usecase.user.BlockUserUseCase
import com.qodein.shared.domain.usecase.user.DeleteUserAccountUseCase
import com.qodein.shared.domain.usecase.user.EnsureUserExists
import com.qodein.shared.domain.usecase.user.GetBlockedUserIdsUseCase
import com.qodein.shared.domain.usecase.user.GetBlockedUsersUseCase
import com.qodein.shared.domain.usecase.user.GetUserByIdUseCase
import com.qodein.shared.domain.usecase.user.ObserveCurrentUserUseCase
import com.qodein.shared.domain.usecase.user.UnblockUserUseCase
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Provides domain-layer use cases.
 */
@OptIn(ExperimentalTime::class)
val domainModule = module {
    single<Clock> { Clock.System }

    single { CoroutineScope(SupervisorJob() + Dispatchers.Default + CoroutineName("DomainScope")) }

    single {
        DeeplinkConfig(
            webBaseUrl = "https://qodein.web.app",
            appScheme = "qodein",
        )
    }

    single { GetAuthStateUseCase(get(), get()) }
    single { SignOutUseCase(get()) }
    single { SignInWithGoogleUseCase(get(), get()) }

    single { EnsureUserExists(get()) }
    single { GetUserByIdUseCase(get()) }
    single { ObserveCurrentUserUseCase(get(), get(), get()) }
    single { AcceptConsentAndCreateUserUseCase(get(), get()) }
    single { AcceptLegalPoliciesUseCase(get()) }

    single { BlockUserUseCase(get(), get()) }
    single { UnblockUserUseCase(get(), get()) }
    single { GetBlockedUserIdsUseCase(get(), get()) }
    single { GetBlockedUsersUseCase(get(), get()) }

    single { DeleteUserAccountUseCase(get(), get()) }

    single { GetBannersUseCase(get()) }

    single { GetPromocodesByUserUseCase(get()) }
    single { GetPromocodesUseCase(get(), get(), get()) }
    single { GetPromocodeUseCase(get()) }
    single { SubmitPromocodeUseCase(get(), get(), get()) }
    single { EnqueuePromocodeSubmissionUseCase(get(), get()) }
    single { GetPromocodeShareContentUseCase(get(), get(), get()) }

    single { GetPostByIdUseCase(get()) }
    single { GetPostsByUserUseCase(get()) }
    single { GetPostsUseCase(get(), get(), get()) }
    single { SubmitPostUseCase(get(), get()) }
    single { EnqueuePostSubmissionUseCase(get(), get()) }
    single { GetPostShareContentUseCase(get(), get(), get()) }

    single { GetReportedContentIdsUseCase(get()) }

    single { GetThemeUseCase(get()) }
    single { ObserveLanguageUseCase(get()) }
    single { SetThemeUseCase(get()) }
    single { SetLanguageUseCase(get()) }

    single { ToggleVoteUseCase(get()) }
    single { GetUserInteractionUseCase(get()) }

    single { GetPopularServicesUseCase(get()) }
    single { SearchServicesUseCase(get()) }
    single { GetOrCreateServiceUseCase(get()) }
    single { GetServicesByIdsUseCase(get()) }
    single { GetServiceLogoUrlUseCase(get()) }

    single { GetLegalDocumentUseCase(get()) }
}
