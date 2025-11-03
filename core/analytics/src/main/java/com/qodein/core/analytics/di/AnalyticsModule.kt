package com.qodein.core.analytics.di

import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.qodein.core.analytics.AnalyticsHelper
import com.qodein.core.analytics.FirebaseAnalyticsHelper
import org.koin.dsl.module

val analyticsModule = module {
    single<FirebaseAnalytics> { Firebase.analytics }
    single<AnalyticsHelper> { FirebaseAnalyticsHelper(get()) }
}
