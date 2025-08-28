package com.qodein.feature.home.model

import androidx.compose.runtime.Stable
import com.qodein.shared.model.Service

@Stable
sealed class ServiceFilter {
    data object All : ServiceFilter()
    data class Selected(val services: Set<Service>) : ServiceFilter() {
        val isEmpty: Boolean get() = services.isEmpty()

        fun toggle(service: Service): ServiceFilter {
            val newServices = if (services.contains(service)) {
                services - service
            } else {
                services + service
            }
            return if (newServices.isEmpty()) All else Selected(newServices)
        }

        fun contains(service: Service): Boolean = services.contains(service)
    }
}
