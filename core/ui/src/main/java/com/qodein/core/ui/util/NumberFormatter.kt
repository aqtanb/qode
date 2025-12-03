package com.qodein.core.ui.util

import java.math.BigDecimal

fun formatNumber(value: Double): String = BigDecimal.valueOf(value).stripTrailingZeros().toPlainString()
