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
    val reason: String? = null
)
