package com.smartcommute.feature.linedetails.domain.model

data class Disruption(
    val id: Long,
    val category: String,
    val type: String,
    val categoryDescription: String,
    val description: String,
    val closureText: String?,
    val affectedStops: List<String>,
    val createdDate: Long,
    val startDate: Long?,
    val endDate: Long?,
    val severity: Int
)
