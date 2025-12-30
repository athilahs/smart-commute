package com.smartcommute.feature.linedetails.domain.model

data class Closure(
    val id: Long,
    val description: String,
    val reason: String,
    val affectedStations: List<String>,
    val affectedSegment: String?,
    val startDate: Long,
    val endDate: Long,
    val alternativeRoute: String?,
    val replacementBus: Boolean
)
