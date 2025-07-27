package com.qodein.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Country(val code: String, val name: String, val phoneCode: String, val flagResourceId: Int)
