package com.smartcommute.feature.statusalerts.domain.model

import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import java.util.UUID

data class StatusAlert(
    val id: String = UUID.randomUUID().toString(),
    val time: LocalTime,
    val selectedDays: Set<DayOfWeek>, // Empty set = one-time alarm
    val selectedTubeLines: Set<String>, // TubeLine IDs (e.g., "central", "northern")
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastModifiedAt: Long = System.currentTimeMillis()
) {
    val isOneTime: Boolean
        get() = selectedDays.isEmpty()

    val isRecurring: Boolean
        get() = selectedDays.isNotEmpty()

    fun getDisplayTime(): String {
        // Format: "7:30 AM" or "19:30" depending on system locale
        return time.format(DateTimeFormatter.ofPattern("h:mm a"))
    }

    fun getDisplayDays(): String {
        return when {
            isOneTime -> "One time"
            selectedDays.size == 7 -> "Every day"
            selectedDays == setOf(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
            ) -> "Weekdays"
            selectedDays == setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY) -> "Weekends"
            else -> selectedDays
                .sortedBy { it.value }
                .joinToString(", ") { it.getDisplayName(TextStyle.SHORT, Locale.getDefault()) }
        }
    }
}
