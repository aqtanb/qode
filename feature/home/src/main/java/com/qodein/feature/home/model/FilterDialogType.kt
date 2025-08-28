package com.qodein.feature.home.model

import androidx.compose.runtime.Stable

@Stable
sealed class FilterDialogType {
    data object Category : FilterDialogType()
    data object Service : FilterDialogType()
    data object Sort : FilterDialogType()
}
