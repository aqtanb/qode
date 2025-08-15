package com.qodein.qode

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.Modifier
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.qode.ui.QodeApp
import com.qodein.qode.ui.rememberQodeAppState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
