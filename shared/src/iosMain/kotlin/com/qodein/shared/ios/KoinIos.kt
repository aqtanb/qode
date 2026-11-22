package com.qodein.shared.ios

import com.qodein.shared.data.di.sharedDataModule
import com.qodein.shared.domain.di.domainModule
import com.qodein.shared.domain.repository.ReportRepository
import com.qodein.shared.domain.repository.StorageRepository
import org.koin.core.context.startKoin
import org.koin.dsl.module

private val iosModule = module {
    single<ReportRepository> { IosReportRepository(get()) }
    single<StorageRepository> { IosStorageRepository(get()) }
}

/** Called once from the Swift app before any [QodeApi] use. */
fun initKoin(
    supabaseUrl: String,
    supabaseKey: String
) {
    startKoin {
        modules(sharedDataModule(supabaseUrl, supabaseKey), domainModule, iosModule)
    }
}
