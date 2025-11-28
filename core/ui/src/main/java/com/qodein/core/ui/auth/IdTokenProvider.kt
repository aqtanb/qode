package com.qodein.core.ui.auth

import android.content.Context
import com.qodein.shared.common.Result
import com.qodein.shared.common.error.OperationError

/**
 * Abstraction for obtaining an ID token from the platform.
 */
interface IdTokenProvider {
    suspend fun getIdToken(activityContext: Context): Result<String, OperationError>
}
