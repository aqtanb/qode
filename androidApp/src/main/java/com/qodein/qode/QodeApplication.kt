package com.qodein.qode

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Qode.
 *
 * Firebase initialization is handled by FirebaseInitializer via androidx.startup
 * to ensure it's initialized before any Dagger/Hilt components are created.
 */
@HiltAndroidApp
class QodeApplication : Application()
