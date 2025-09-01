package com.qodein.core.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.qodein.core.designsystem.icon.QodeActionIcons
import com.qodein.core.designsystem.icon.QodeNavigationIcons
import com.qodein.core.designsystem.icon.QodeSecurityIcons
import com.qodein.core.designsystem.theme.extendedColorScheme
import com.qodein.shared.model.PromoCode
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

data class StatusInfo(val text: String, val icon: ImageVector, val backgroundColor: Color, val contentColor: Color)

@Composable
fun getPromoCodeStatus(
    promoCode: PromoCode,
    now: Instant
): StatusInfo {
    val threeDaysFromNow = now.plus(3.days)

    return when {
        now < promoCode.startDate -> StatusInfo(
            text = "Not Active",
            icon = QodeNavigationIcons.Calendar,
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        now > promoCode.endDate -> StatusInfo(
            text = "Expired",
            icon = QodeActionIcons.Block,
            backgroundColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        )

        promoCode.endDate < threeDaysFromNow -> StatusInfo(
            text = "Expiring Soon",
            icon = QodeNavigationIcons.Warning,
            backgroundColor = Color(0xFFFF8A00).copy(alpha = 0.1f),
            contentColor = Color(0xFFFF8A00),
        )

        else -> StatusInfo(
            text = "Active",
            icon = QodeActionIcons.Play,
            backgroundColor = MaterialTheme.extendedColorScheme.successContainer,
            contentColor = MaterialTheme.extendedColorScheme.onSuccessContainer,
        )
    }
}

// Helper functions to ensure icon consistency across components
fun getFirstUserOnlyIcon(isFirstUserOnly: Boolean): ImageVector =
    if (isFirstUserOnly) QodeNavigationIcons.Popular else QodeNavigationIcons.Team

fun getVerifiedIcon(isVerified: Boolean): ImageVector = if (isVerified) QodeActionIcons.Check else QodeSecurityIcons.Secure
