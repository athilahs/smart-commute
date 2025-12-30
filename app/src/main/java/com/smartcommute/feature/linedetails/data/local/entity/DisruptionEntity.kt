package com.smartcommute.feature.linedetails.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.smartcommute.feature.linestatus.data.local.entity.TubeLineEntity

@Entity(
    tableName = "disruptions",
    foreignKeys = [
        ForeignKey(
            entity = TubeLineEntity::class,
            parentColumns = ["id"],
            childColumns = ["lineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["lineId"])]
)
data class DisruptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val lineId: String,

    // Disruption Details
    val category: String,
    val type: String,
    val categoryDescription: String,
    val description: String,
    val closureText: String?,

    // Affected Stations (comma-separated)
    val affectedStops: String,

    // Timing
    val createdDate: Long,
    val startDate: Long?,
    val endDate: Long?,

    // Severity
    val severity: Int
)
