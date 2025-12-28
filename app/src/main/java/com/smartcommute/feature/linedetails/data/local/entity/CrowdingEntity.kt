package com.smartcommute.feature.linedetails.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.smartcommute.feature.linestatus.data.local.entity.TubeLineEntity

@Entity(
    tableName = "crowding",
    foreignKeys = [
        ForeignKey(
            entity = TubeLineEntity::class,
            parentColumns = ["id"],
            childColumns = ["lineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["lineId"], unique = true)]
)
data class CrowdingEntity(
    @PrimaryKey
    val lineId: String,

    // Crowding Level
    val level: String,
    val levelCode: Int,

    // Measurement
    val measurementTime: Long,
    val dataSource: String,

    // Additional Context
    val notes: String?
)
