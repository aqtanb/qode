package com.qodein.core.data.di

import com.qodein.shared.domain.auth.AuthStateManager
import com.qodein.shared.domain.repository.AuthRepository
import com.qodein.shared.domain.repository.BannerRepository
import com.qodein.shared.domain.repository.DevicePreferencesRepository
import com.qodein.shared.domain.repository.PromocodeRepository
import com.qodein.shared.domain.repository.UnifiedUserInteractionRepository
import com.qodein.shared.domain.usecase.auth.GetAuthStateUseCase
import com.qodein.shared.domain.usecase.auth.SignInWithGoogleUseCase
import com.qodein.shared.domain.usecase.auth.SignOutUseCase
import com.qodein.shared.domain.usecase.banner.GetBannersUseCase
import com.qodein.shared.domain.usecase.interaction.GetUserBookmarksUseCase
import com.qodein.shared.domain.usecase.interaction.GetUserInteractionUseCase
import com.qodein.shared.domain.usecase.interaction.ToggleBookmarkUseCase
import com.qodein.shared.domain.usecase.interaction.ToggleVoteUseCase
import com.qodein.shared.domain.usecase.preferences.GetLanguageUseCase
import com.qodein.shared.domain.usecase.preferences.GetThemeUseCase
import com.qodein.shared.domain.usecase.preferences.SetLanguageUseCase
import com.qodein.shared.domain.usecase.preferences.SetThemeUseCase
import com.qodein.shared.domain.usecase.promocode.GetPromocodeByIdUseCase
import com.qodein.shared.domain.usecase.promocode.GetPromocodesUseCase
import com.qodein.shared.domain.usecase.promocode.SubmitPromocodeUseCase
import com.qodein.shared.domain.usecase.service.GetPopularServicesUseCase
import com.qodein.shared.domain.usecase.service.SearchServicesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    // Auth State Manager
    @Provides
    @Singleton
    fun provideAuthStateManager(authRepository: AuthRepository): AuthStateManager = AuthStateManager(authRepository)

    // Auth Use Cases
    @Provides
    @Singleton
    fun provideGetAuthStateUseCase(authRepository: AuthRepository): GetAuthStateUseCase = GetAuthStateUseCase(authRepository)

    @Provides
    @Singleton
    fun provideSignInWithGoogleUseCase(authRepository: AuthRepository): SignInWithGoogleUseCase = SignInWithGoogleUseCase(authRepository)

    @Provides
    @Singleton
    fun provideSignOutUseCase(authRepository: AuthRepository): SignOutUseCase = SignOutUseCase(authRepository)

    // Banner Use Cases
    @Provides
    @Singleton
    fun provideGetBannersUseCase(bannerRepository: BannerRepository): GetBannersUseCase = GetBannersUseCase(bannerRepository)

    // Preferences Use Cases
    @Provides
    @Singleton
    fun provideGetThemeUseCase(preferencesRepository: DevicePreferencesRepository): GetThemeUseCase = GetThemeUseCase(preferencesRepository)

    @Provides
    @Singleton
    fun provideGetLanguageUseCase(preferencesRepository: DevicePreferencesRepository): GetLanguageUseCase =
        GetLanguageUseCase(preferencesRepository)

    @Provides
    @Singleton
    fun provideSetThemeUseCase(preferencesRepository: DevicePreferencesRepository): SetThemeUseCase = SetThemeUseCase(preferencesRepository)

    @Provides
    @Singleton
    fun provideSetLanguageUseCase(preferencesRepository: DevicePreferencesRepository): SetLanguageUseCase =
        SetLanguageUseCase(preferencesRepository)

    // PromoCode Use Cases
    @Provides
    @Singleton
    fun provideGetPromoCodesUseCase(promoCodeRepository: PromocodeRepository): GetPromocodesUseCase =
        GetPromocodesUseCase(promoCodeRepository)

    @Provides
    @Singleton
    fun provideGetPromoCodeByIdUseCase(promoCodeRepository: PromocodeRepository): GetPromocodeByIdUseCase =
        GetPromocodeByIdUseCase(promoCodeRepository)

    @Provides
    @Singleton
    fun provideCreatePromoCodeUseCase(promoCodeRepository: PromocodeRepository): SubmitPromocodeUseCase =
        SubmitPromocodeUseCase(promoCodeRepository)

    @Provides
    @Singleton
    fun provideToggleVoteUseCase(unifiedRepository: UnifiedUserInteractionRepository): ToggleVoteUseCase =
        ToggleVoteUseCase(unifiedRepository)

    @Provides
    @Singleton
    fun provideToggleBookmarkUseCase(unifiedRepository: UnifiedUserInteractionRepository): ToggleBookmarkUseCase =
        ToggleBookmarkUseCase(unifiedRepository)

    @Provides
    @Singleton
    fun provideGetUserBookmarksUseCase(unifiedRepository: UnifiedUserInteractionRepository): GetUserBookmarksUseCase =
        GetUserBookmarksUseCase(unifiedRepository)

    @Provides
    @Singleton
    fun provideGetUserInteractionUseCase(unifiedRepository: UnifiedUserInteractionRepository): GetUserInteractionUseCase =
        GetUserInteractionUseCase(unifiedRepository)

    // Service Use Cases
    @Provides
    @Singleton
    fun provideGetPopularServicesUseCase(promoCodeRepository: PromocodeRepository): GetPopularServicesUseCase =
        GetPopularServicesUseCase(promoCodeRepository)

    @Provides
    @Singleton
    fun provideSearchServicesUseCase(promoCodeRepository: PromocodeRepository): SearchServicesUseCase =
        SearchServicesUseCase(promoCodeRepository)
}
