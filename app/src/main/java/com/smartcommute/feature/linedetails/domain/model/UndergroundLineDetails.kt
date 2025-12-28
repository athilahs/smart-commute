package com.smartcommute.feature.linedetails.domain.model

import com.smartcommute.feature.linestatus.domain.model.ServiceStatus

data class UndergroundLineDetails(
    val id: String,
    val name: String,
    val modeName: String,
    val status: ServiceStatus,
    val brandColor: String,
    val headerImageRes: String,
    val lastUpdated: Long,
    val disruptions: List<Disruption>,
    val closures: List<Closure>,
    val crowding: Crowding?
)
