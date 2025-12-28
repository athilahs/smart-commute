package com.smartcommute.feature.linestatus.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tube_lines")
data class TubeLineEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val modeName: String,
    val statusType: String,
    val statusDescription: String,
    val statusSeverity: Int,
    val brandColor: String = "#000000",
    val headerImageRes: String = "placeholder",
    val lastUpdated: Long,
    val cacheExpiry: Long = lastUpdated + 600000L // 10 minutes default
)
