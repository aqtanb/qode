package com.qodein.feature.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.qodein.core.designsystem.component.QodeCard
import com.qodein.core.designsystem.component.QodeCardVariant
import com.qodein.core.designsystem.component.QodeLogo
import com.qodein.core.designsystem.component.QodeLogoSize
import com.qodein.core.designsystem.component.QodePrimaryGradient
import com.qodein.core.designsystem.theme.QodeSpacing

@Composable
fun AuthScreen(modifier: Modifier = Modifier) {
    QodePrimaryGradient {
        QodeCard(
            variant = QodeCardVariant.Elevated,
            modifier = modifier
                .padding(top = QodeSpacing.xxxl, start = QodeSpacing.lg, end = QodeSpacing.lg).fillMaxWidth(),
        ) {
            Column(
                modifier = modifier.padding(QodeSpacing.lg).fillMaxWidth(),
            ) {
                QodeLogo(
                    size = QodeLogoSize.Large,
                    modifier = modifier.align(Alignment.CenterHorizontally),
                )
                Text(
                    text = "Sign In",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    modifier = modifier
                        .padding(top = QodeSpacing.sm)
                        .align(Alignment.CenterHorizontally),
                )
                Text(
                    text = "Enter your phone number to continue",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = modifier
                        .padding(top = QodeSpacing.sm)
                        .align(Alignment.CenterHorizontally),
                )
                Text(
                    text = "Phone Number",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = modifier
                        .padding(top = QodeSpacing.lg),
                )
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun AuthScreenPreview() {
    AuthScreen()
}
