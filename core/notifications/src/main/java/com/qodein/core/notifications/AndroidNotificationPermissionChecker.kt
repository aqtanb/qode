package com.qodein.core.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.qodein.shared.common.permission.NotificationPermissionChecker
import com.qodein.shared.common.permission.NotificationPermissionState

class AndroidNotificationPermissionChecker(private val context: Context) : NotificationPermissionChecker {

    override fun checkPermission(): NotificationPermissionState {
        if (!isPermissionRequired()) {
            return NotificationPermissionState.NotRequired
        }

        return if (hasPermission()) {
            NotificationPermissionState.Granted
        } else {
            NotificationPermissionState.Denied
        }
    }

    override fun isPermissionRequired(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    private fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
}
