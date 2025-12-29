package com.qodein.core.ui.text

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource

sealed interface UiText {
    data class DynamicString(val value: String) : UiText

    data class StringResource(@param:StringRes val resId: Int, val args: List<Any> = emptyList()) : UiText
}

fun UiText.asString(context: Context): String =
    when (this) {
        is UiText.DynamicString -> value
        is UiText.StringResource -> context.getString(resId, *args.toTypedArray())
    }

@Composable
fun UiText.asString(): String {
    val context = LocalContext.current
    return when (this) {
        is UiText.DynamicString -> value
        is UiText.StringResource -> {
            if (args.isEmpty()) {
                stringResource(resId)
            } else {
                asString(context)
            }
        }
    }
}
