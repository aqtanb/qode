package com.qodein.feature.post.di

import com.qodein.feature.post.detail.PostDetailViewModel
import com.qodein.feature.post.feed.FeedViewModel
import com.qodein.feature.post.submission.PostSubmissionViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val postModule = module {
    viewModel { FeedViewModel(get(), get()) }
    viewModel { PostDetailViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { PostSubmissionViewModel(androidApplication(), get(), get()) }
}
