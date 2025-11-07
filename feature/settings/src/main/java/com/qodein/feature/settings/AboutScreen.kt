package com.qodein.feature.settings

import android.content.Intent
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import com.qodein.core.analytics.TrackScreenViewEvent
import com.qodein.core.designsystem.ThemePreviews
import com.qodein.core.designsystem.component.QodeLogo
import com.qodein.core.designsystem.component.QodeLogoSize
import com.qodein.core.designsystem.component.QodeTopAppBar
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeSocialIcons
import com.qodein.core.designsystem.theme.QodeTheme
import com.qodein.core.designsystem.theme.SizeTokens
import com.qodein.core.designsystem.theme.SpacingTokens
import com.qodein.shared.common.AppConstants

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
                navigationIcon = QodeActionIcons.Back,
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

            Spacer(modifier = Modifier.height(SpacingTokens.xxxl))

            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, AppConstants.TELEGRAM_URL.toUri())
                    context.startActivity(intent)
                },
                modifier = Modifier.padding(horizontal = SpacingTokens.xl),
            ) {
                Icon(
                    imageVector = QodeSocialIcons.Telegram,
                    contentDescription = null,
                    modifier = Modifier.size(SizeTokens.Icon.sizeMedium),
                )
                Spacer(modifier = Modifier.width(SpacingTokens.xs))
                Text(stringResource(R.string.about_join_telegram))
            }
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
