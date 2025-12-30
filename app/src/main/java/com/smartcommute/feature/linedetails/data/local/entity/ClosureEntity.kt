package com.smartcommute.feature.linedetails.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.smartcommute.feature.linestatus.data.local.entity.TubeLineEntity

@Entity(
    tableName = "closures",
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
data class ClosureEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val lineId: String,

    // Closure Details
    val description: String,
    val reason: String,

    // Affected Stations (comma-separated)
    val affectedStations: String,
    val affectedSegment: String?,

    // Schedule
    val startDate: Long,
    val endDate: Long,

    // Alternative Service
    val alternativeRoute: String?,
    val replacementBus: Boolean
)
