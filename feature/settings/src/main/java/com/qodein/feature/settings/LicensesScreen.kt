package com.qodein.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.qodein.core.analytics.TrackScreenViewEvent
import com.qodein.core.designsystem.component.QodeTopAppBar
import com.qodein.core.designsystem.icon.QodeActionIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(onBackClick: () -> Unit) {
    TrackScreenViewEvent(screenName = "Licenses")

    val listState = rememberLazyListState()
    val libraries by produceLibraries(R.raw.aboutlibraries)

    Scaffold(
        topBar = {
            QodeTopAppBar(
                title = stringResource(R.string.settings_open_source_licences_title),
                navigationIcon = QodeActionIcons.Back,
                onNavigationClick = onBackClick,
            )
        },
    ) { innerPadding ->
        LibrariesContainer(
            libraries = libraries,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = innerPadding,
            lazyListState = listState,
        )
    }
}
