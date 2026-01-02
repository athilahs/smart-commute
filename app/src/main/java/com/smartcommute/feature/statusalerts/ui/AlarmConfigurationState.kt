package com.smartcommute.feature.statusalerts.ui

import java.time.DayOfWeek
import java.time.LocalTime

data class AlarmConfigurationState(
    val alarmId: String? = null, // null = creating new alarm
    val time: LocalTime = LocalTime.of(7, 30),
    val selectedDays: Set<DayOfWeek> = emptySet(),
    val selectedTubeLines: Set<String> = emptySet(),
    val availableTubeLines: List<TubeLine> = emptyList(),
    val validationErrors: List<ValidationError> = emptyList()
) {
    val isEditing: Boolean
        get() = alarmId != null

    val isCreating: Boolean
        get() = alarmId == null

    val canSave: Boolean
        get() = selectedTubeLines.isNotEmpty() && validationErrors.isEmpty()
}

// Simplified TubeLine for UI (reuse from feature 001 in actual implementation)
data class TubeLine(
    val id: String,
    val name: String,
    val colorHex: String
)

sealed class ValidationError {
    data object NoTubeLinesSelected : ValidationError()
    data object TooManyAlarms : ValidationError()
}
