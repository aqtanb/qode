package com.qodein.qode.di

import com.qodein.core.analytics.AnalyticsHelper
import com.qodein.core.ui.auth.IdTokenProvider
import com.qodein.core.ui.refresh.ScreenRefreshCoordinator
import com.qodein.shared.domain.repository.DevicePreferencesRepository
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import com.qodein.shared.domain.usecase.auth.SignInWithGoogleUseCase
import com.qodein.shared.domain.usecase.auth.SignOutUseCase
import com.qodein.shared.domain.usecase.banner.GetBannersUseCase
import com.qodein.shared.domain.usecase.interaction.GetUserBookmarksUseCase
import com.qodein.shared.domain.usecase.interaction.GetUserInteractionUseCase
import com.qodein.shared.domain.usecase.interaction.ToggleBookmarkUseCase
import com.qodein.shared.domain.usecase.interaction.ToggleVoteUseCase
import com.qodein.shared.domain.usecase.post.GetPostByIdUseCase
import com.qodein.shared.domain.usecase.post.GetPostsUseCase
import com.qodein.shared.domain.usecase.preferences.GetThemeUseCase
import com.qodein.shared.domain.usecase.preferences.ObserveLanguageUseCase
import com.qodein.shared.domain.usecase.preferences.SetLanguageUseCase
import com.qodein.shared.domain.usecase.preferences.SetThemeUseCase
import com.qodein.shared.domain.usecase.promocode.GetPromocodeUseCase
import com.qodein.shared.domain.usecase.promocode.GetPromocodesUseCase
import com.qodein.shared.domain.usecase.promocode.SubmitPromocodeUseCase
import com.qodein.shared.domain.usecase.service.GetServicesByIdsUseCase
import com.qodein.shared.domain.usecase.user.DeleteUserAccountUseCase
import com.qodein.shared.domain.usecase.user.GetUserByIdUseCase
import com.qodein.shared.domain.usecase.user.ObserveUserUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object KoinBridgeModule : KoinComponent {

    @Provides
    @Singleton
    fun provideAnalyticsHelper(): AnalyticsHelper {
        val helper: AnalyticsHelper by inject()
        return helper
    }

    @Provides
    @Singleton
    fun provideIdTokenProvider(): IdTokenProvider {
        val provider: IdTokenProvider by inject()
        return provider
    }

    @Provides
    @Singleton
    fun provideScreenRefreshCoordinator(): ScreenRefreshCoordinator {
        val coordinator: ScreenRefreshCoordinator by inject()
        return coordinator
    }

    @Provides
    @Singleton
    fun provideGetPostsUseCase(): GetPostsUseCase {
        val useCase: GetPostsUseCase by inject()
        return useCase
    }

    @Provides
    @Singleton
    fun provideGetBannersUseCase(): GetBannersUseCase {
        val useCase: GetBannersUseCase by inject()
        return useCase
    }

    @Provides
    @Singleton
    fun provideGetPromocodesUseCase(): GetPromocodesUseCase {
        val useCase: GetPromocodesUseCase by inject()
        return useCase
    }

    @Provides
    @Singleton
    fun provideDevicePreferencesRepository(): DevicePreferencesRepository {
        val repo: DevicePreferencesRepository by inject()
        return repo
    }

    @Provides
    @Singleton
    fun provideGetPostByIdUseCase(): GetPostByIdUseCase {
        val useCase: GetPostByIdUseCase by inject()
        return useCase
    }

    @Provides
    @Singleton
    fun provideGetUserInteractionUseCase(): GetUserInteractionUseCase {
        val useCase: GetUserInteractionUseCase by inject()
        return useCase
    }

    @Provides
    @Singleton
    fun provideToggleVoteUseCase(): ToggleVoteUseCase {
        val useCase: ToggleVoteUseCase by inject()
        return useCase
    }

    @Provides
    @Singleton
    fun provideSignInWithGoogleUseCase(): SignInWithGoogleUseCase {
        val useCase: SignInWithGoogleUseCase by inject()
        return useCase
    }

    @Provides
    @Singleton
    fun provideGetAuthStateUseCase(): GetAuthStateUseCase {
        val useCase: GetAuthStateUseCase by inject()
        return useCase
    }

    @Provides
    @Singleton
    fun provideGetUserByIdUseCase(): GetUserByIdUseCase {
        val useCase: GetUserByIdUseCase by inject()
        return useCase
    }

    @Provides
    @Singleton
    fun provideObserveUserUseCase(): ObserveUserUseCase {
        val useCase: ObserveUserUseCase by inject()
        return useCase
    }

    @Provides
    @Singleton
    fun provideSignOutUseCase(): SignOutUseCase {
        val useCase: SignOutUseCase by inject()
        return useCase
    }

    @Provides
    @Singleton
    fun provideDeleteUserAccountUseCase(): DeleteUserAccountUseCase {
        val useCase: DeleteUserAccountUseCase by inject()
        return useCase
    }

    @Provides
    @Singleton
    fun provideSubmitPromocodeUseCase(): SubmitPromocodeUseCase {
        val useCase: SubmitPromocodeUseCase by inject()
        return useCase
    }

    @Provides
    @Singleton
    fun provideGetThemeUseCase(): GetThemeUseCase {
        val useCase: GetThemeUseCase by inject()
        return useCase
    }

    @Provides
    @Singleton
    fun provideGetLanguageUseCase(): ObserveLanguageUseCase {
        val useCase: ObserveLanguageUseCase by inject()
        return useCase
    }

    @Provides
    @Singleton
    fun provideSetThemeUseCase(): SetThemeUseCase {
        val useCase: SetThemeUseCase by inject()
        return useCase
    }

    @Provides
    @Singleton
    fun provideSetLanguageUseCase(): SetLanguageUseCase {
        val useCase: SetLanguageUseCase by inject()
        return useCase
    }

    @Provides
    @Singleton
    fun provideGetPromocodeByIdUseCase(): GetPromocodeUseCase {
        val useCase: GetPromocodeUseCase by inject()
        return useCase
    }

    @Provides
    @Singleton
    fun provideToggleBookmarkUseCase(): ToggleBookmarkUseCase {
        val useCase: ToggleBookmarkUseCase by inject()
        return useCase
    }

    @Provides
    @Singleton
    fun provideGetUserBookmarksUseCase(): GetUserBookmarksUseCase {
        val useCase: GetUserBookmarksUseCase by inject()
        return useCase
    }

    @Provides
    @Singleton
    fun provideGetServicesByIdsUseCase(): GetServicesByIdsUseCase {
        val useCase: GetServicesByIdsUseCase by inject()
        return useCase
    }
}
