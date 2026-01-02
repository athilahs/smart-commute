package com.smartcommute.feature.statusalerts.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smartcommute.feature.statusalerts.ui.components.TimePickerDialog
import com.smartcommute.feature.statusalerts.ui.components.TubeLinePicker
import com.smartcommute.feature.statusalerts.ui.components.WeekdaySelector
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Bottom sheet for creating or editing status alarms.
 * Includes time picker, weekday selector, tube line picker, and save/cancel buttons.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmBottomSheet(
    alarmConfiguration: AlarmConfigurationState,
    onConfigurationChanged: (AlarmConfigurationState) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showTimePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 32.dp)
        ) {
            // Title
            Text(
                text = if (alarmConfiguration.isEditMode) "Edit Alarm" else "Create Alarm",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Time selection section
            Text(
                text = "Time",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedButton(
                onClick = { showTimePicker = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = alarmConfiguration.time.format(
                        java.time.format.DateTimeFormatter.ofPattern("h:mm a")
                    )
                )
            }

            // Weekday selection section
            Text(
                text = "Repeat on",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Leave empty for one-time alarm",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            WeekdaySelector(
                selectedDays = alarmConfiguration.selectedDays,
                onDaysChanged = { days ->
                    onConfigurationChanged(alarmConfiguration.copy(selectedDays = days))
                },
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Tube line selection section
            Text(
                text = "Monitor Lines",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Select at least one tube line",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            TubeLinePicker(
                selectedLineIds = alarmConfiguration.selectedTubeLines,
                onSelectionChanged = { lines ->
                    onConfigurationChanged(alarmConfiguration.copy(selectedTubeLines = lines))
                },
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Validation error display
            if (!alarmConfiguration.isValid) {
                Text(
                    text = alarmConfiguration.validationError ?: "Please fix the errors above",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = onSave,
                    enabled = alarmConfiguration.isValid,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (alarmConfiguration.isEditMode) "Update" else "Save")
                }
            }
        }
    }

    // Time picker dialog
    if (showTimePicker) {
        TimePickerDialog(
            selectedTime = alarmConfiguration.time,
            onTimeSelected = { time ->
                onConfigurationChanged(alarmConfiguration.copy(time = time))
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}
