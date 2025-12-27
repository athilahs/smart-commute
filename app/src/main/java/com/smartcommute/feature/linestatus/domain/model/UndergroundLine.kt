package com.smartcommute.feature.linestatus.domain.model

data class UndergroundLine(
    val id: String,
    val name: String,
    val modeName: String,
    val status: ServiceStatus
)
