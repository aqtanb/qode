package com.qodein.feature.settings

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.qodein.core.analytics.TrackScreenViewEvent
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.component.QodeLogo
import com.qodein.core.designsystem.component.QodeLogoSize
import com.qodein.core.designsystem.component.QodeTopAppBar
import com.qodein.core.designsystem.icon.ActionIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SpacingTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AboutScreen(onNavigateBack: () -> Unit) {
    TrackScreenViewEvent("About")

    val context = LocalContext.current

    val deviceVersion = stringResource(
        R.string.about_device_version,
        Build.VERSION.RELEASE,
        Build.VERSION.SDK_INT,
    )

    Scaffold(
        topBar = {
            QodeTopAppBar(
                title = stringResource(R.string.settings_about_title),
                navigationIcon = ActionIcons.Back,
                onNavigationClick = onNavigateBack,
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
            Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(modifier = Modifier.weight(1f))

            QodeLogo(size = QodeLogoSize.XLarge)

            Spacer(modifier = Modifier.height(SpacingTokens.lg))

            Text(
                text = stringResource(R.string.about_app_name),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(SpacingTokens.xs))

            Text(
                text = stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(SpacingTokens.xs))

            Text(
                text = deviceVersion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = buildAnnotatedString {
                    append("Author: ")

                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
                        append("Aktanberdi Ybyraiym")
                    }
                },
                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(SpacingTokens.xxl))
        }
    }
}

@ThemePreviews
@Composable
private fun AboutScreenPreview() {
    QodeTheme {
        Surface {
            AboutScreen(onNavigateBack = {})
        }
    }
}
