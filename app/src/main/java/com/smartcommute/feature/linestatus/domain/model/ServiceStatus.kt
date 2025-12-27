package com.smartcommute.feature.linestatus.domain.model

enum class StatusType {
    GOOD_SERVICE,
    MINOR_DELAYS,
    MAJOR_DELAYS,
    SEVERE_DELAYS,
    CLOSURE,
    SERVICE_DISRUPTION
}

data class ServiceStatus(
    val type: StatusType,
    val description: String = "",
    val severity: Int = 0
)
