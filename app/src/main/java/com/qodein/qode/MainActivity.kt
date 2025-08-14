package com.qodein.qode

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.Modifier
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.qode.ui.QodeApp
import com.qodein.qode.ui.rememberQodeAppState
import com.qodein.qode.util.FirestoreDataInitializer
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var firestoreDataInitializer: FirestoreDataInitializer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize sample data in Firestore on first launch
        firestoreDataInitializer.initializeSampleData()

        setContent {
            QodeTheme {
                QodeApp(
                    appState = rememberQodeAppState(),
                    modifier = Modifier,
                )
            }
        }
    }
}
