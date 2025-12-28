package com.smartcommute.feature.linestatus.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LineStatusDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("modeName")
    val modeName: String,
    @SerializedName("lineStatuses")
    val lineStatuses: List<LineStatusResponseDto>
)

data class LineStatusResponseDto(
    @SerializedName("statusSeverity")
    val statusSeverity: Int,
    @SerializedName("statusSeverityDescription")
    val statusSeverityDescription: String,
    @SerializedName("reason")
    val reason: String? = null,
    @SerializedName("disruption")
    val disruption: DisruptionDto? = null,
    @SerializedName("validityPeriods")
    val validityPeriods: List<ValidityPeriodDto>? = null
)

data class DisruptionDto(
    @SerializedName("category")
    val category: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("categoryDescription")
    val categoryDescription: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("closureText")
    val closureText: String? = null,
    @SerializedName("affectedStops")
    val affectedStops: List<String>? = null,
    @SerializedName("affectedRoutes")
    val affectedRoutes: List<String>? = null,
    @SerializedName("created")
    val created: String? = null
)

data class ValidityPeriodDto(
    @SerializedName("fromDate")
    val fromDate: String? = null,
    @SerializedName("toDate")
    val toDate: String? = null,
    @SerializedName("isNow")
    val isNow: Boolean = false
)

