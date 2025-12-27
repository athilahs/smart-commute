package com.smartcommute.feature.linestatus.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "line_status")
data class LineStatusEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val modeName: String,
    val statusType: String,
    val statusDescription: String,
    val statusSeverity: Int,
    val lastUpdated: Long
)
