package com.qodein.shared.common.permission

interface NotificationPermissionChecker {
    fun checkPermission(): NotificationPermissionState
    fun isPermissionRequired(): Boolean
}
