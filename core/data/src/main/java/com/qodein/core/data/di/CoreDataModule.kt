package com.qodein.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.WorkManager
import com.qodein.core.data.BuildConfig
import com.qodein.core.data.datasource.DevicePreferencesDataSource
import com.qodein.core.data.datasource.LocalReportDataSource
import com.qodein.core.data.post.WorkManagerPostSubmissionScheduler
import com.qodein.core.data.promocode.WorkManagerPromocodeSubmissionScheduler
import com.qodein.core.data.repository.DevicePreferencesRepositoryImpl
import com.qodein.core.data.repository.ReportRepositoryImpl
import com.qodein.core.data.repository.StorageRepositoryImpl
import com.qodein.core.data.worker.UploadPostWorker
import com.qodein.core.data.worker.UploadPromocodeWorker
import com.qodein.shared.data.di.sharedDataModule
import com.qodein.shared.domain.repository.DevicePreferencesRepository
import com.qodein.shared.domain.repository.PostSubmissionScheduler
import com.qodein.shared.domain.repository.PromocodeSubmissionScheduler
import com.qodein.shared.domain.repository.ReportRepository
import com.qodein.shared.domain.repository.StorageRepository
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.dsl.module

private val Context.preferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "device_preferences")
private val Context.reportDataStore: DataStore<Preferences> by preferencesDataStore(name = "report_preferences")

val coreDataModule = module {
    single<DataStore<Preferences>> { androidContext().preferencesDataStore }
    single<WorkManager> { WorkManager.getInstance(androidContext()) }

    single { DevicePreferencesDataSource(get()) }
    single { LocalReportDataSource(androidContext().reportDataStore) }

    single<DevicePreferencesRepository> { DevicePreferencesRepositoryImpl(androidContext(), get()) }
    single<StorageRepository> { StorageRepositoryImpl(androidContext(), get()) }
    single<ReportRepository> { ReportRepositoryImpl(get(), get()) }
    single<PostSubmissionScheduler> { WorkManagerPostSubmissionScheduler(get()) }
    single<PromocodeSubmissionScheduler> { WorkManagerPromocodeSubmissionScheduler(get()) }

    workerOf(::UploadPostWorker)
    workerOf(::UploadPromocodeWorker)
}

val supabaseDataModule = sharedDataModule(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_PUBLISHABLE_KEY)
