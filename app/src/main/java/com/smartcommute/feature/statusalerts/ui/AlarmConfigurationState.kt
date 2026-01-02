package com.smartcommute.feature.statusalerts.ui

import com.smartcommute.feature.statusalerts.domain.model.StatusAlert
import java.time.DayOfWeek
import java.time.LocalTime

data class AlarmConfigurationState(
    val alarmId: String? = null, // null = creating new alarm
    val time: LocalTime = LocalTime.of(7, 30),
    val selectedDays: Set<DayOfWeek> = emptySet(),
    val selectedTubeLines: Set<String> = emptySet(),
    val validationErrors: List<ValidationError> = emptyList()
) {
    val isEditMode: Boolean
        get() = alarmId != null

    val isCreateMode: Boolean
        get() = alarmId == null

    val isValid: Boolean
        get() = selectedTubeLines.isNotEmpty() && validationErrors.isEmpty()

    val validationError: String?
        get() = when {
            selectedTubeLines.isEmpty() -> "Please select at least one tube line"
            validationErrors.contains(ValidationError.TooManyAlarms) -> "Maximum 10 alarms reached"
            else -> null
        }

    fun toStatusAlert(): StatusAlert {
        return StatusAlert(
            id = alarmId ?: java.util.UUID.randomUUID().toString(),
            time = time,
            selectedDays = selectedDays,
            selectedTubeLines = selectedTubeLines,
            isEnabled = true
        )
    }

    companion object {
        fun fromStatusAlert(alarm: StatusAlert): AlarmConfigurationState {
            return AlarmConfigurationState(
                alarmId = alarm.id,
                time = alarm.time,
                selectedDays = alarm.selectedDays,
                selectedTubeLines = alarm.selectedTubeLines
            )
        }
    }
}

sealed class ValidationError {
    data object NoTubeLinesSelected : ValidationError()
    data object TooManyAlarms : ValidationError()
}

