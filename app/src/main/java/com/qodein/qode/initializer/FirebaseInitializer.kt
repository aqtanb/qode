package com.qodein.qode.initializer

import android.content.Context
import androidx.startup.Initializer
import com.google.firebase.Firebase
import com.google.firebase.initialize

/**
 * Initializes Firebase using androidx.startup library.
 *
 * This ensures Firebase is initialized before any other app components
 * (including Dagger/Hilt components) can access it, preventing the
 * "Default FirebaseApp is not initialized" exception.
 */
class FirebaseInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        Firebase.initialize(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        // No dependencies - Firebase should be initialized first
        return emptyList()
    }
}
