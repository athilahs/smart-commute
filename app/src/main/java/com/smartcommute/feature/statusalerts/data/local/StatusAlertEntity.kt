package com.smartcommute.feature.statusalerts.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.smartcommute.feature.statusalerts.domain.model.StatusAlert
import java.time.DayOfWeek
import java.time.LocalTime

@Entity(tableName = "status_alerts")
data class StatusAlertEntity(
    @PrimaryKey val id: String,
    val hour: Int,
    val minute: Int,
    val selectedDaysOfWeek: String, // Comma-separated: "MONDAY,WEDNESDAY,FRIDAY" or "" for one-time
    val selectedTubeLines: String, // Comma-separated: "central,northern,victoria"
    val isEnabled: Boolean,
    val createdAt: Long,
    val lastModifiedAt: Long
)

// Extension functions for domain mapping
fun StatusAlertEntity.toDomain(): StatusAlert {
    return StatusAlert(
        id = id,
        time = LocalTime.of(hour, minute),
        selectedDays = if (selectedDaysOfWeek.isEmpty()) {
            emptySet()
        } else {
            selectedDaysOfWeek.split(",").map { DayOfWeek.valueOf(it) }.toSet()
        },
        selectedTubeLines = selectedTubeLines.split(",").toSet(),
        isEnabled = isEnabled,
        createdAt = createdAt,
        lastModifiedAt = lastModifiedAt
    )
}

fun StatusAlert.toEntity(): StatusAlertEntity {
    return StatusAlertEntity(
        id = id,
        hour = time.hour,
        minute = time.minute,
        selectedDaysOfWeek = selectedDays.joinToString(",") { it.name },
        selectedTubeLines = selectedTubeLines.joinToString(","),
        isEnabled = isEnabled,
        createdAt = createdAt,
        lastModifiedAt = lastModifiedAt
    )
}
