package com.qodein.core.testing.fake

import com.qodein.shared.common.permission.NotificationPermissionChecker
import com.qodein.shared.common.permission.NotificationPermissionState

class FakeNotificationPermissionChecker(var state: NotificationPermissionState = NotificationPermissionState.Granted) :
    NotificationPermissionChecker {
    override fun checkPermission(): NotificationPermissionState = state

    override fun isPermissionRequired(): Boolean = state != NotificationPermissionState.NotRequired
}
