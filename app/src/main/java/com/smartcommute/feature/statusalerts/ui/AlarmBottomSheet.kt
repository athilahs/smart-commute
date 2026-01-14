package com.smartcommute.feature.statusalerts.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
 * Includes time picker, weekday selector, tube line picker, and save/cancel/delete buttons.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmBottomSheet(
    alarmConfiguration: AlarmConfigurationState,
    onConfigurationChanged: (AlarmConfigurationState) -> Unit,
    onSave: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showTimePicker by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

    // Expand to full height on initial composition
    LaunchedEffect(Unit) {
        sheetState.expand()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = {
            BottomSheetDefaults.DragHandle()
        },
        modifier = modifier
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Title
            item {
                Text(
                    text = if (alarmConfiguration.isEditMode) "Edit Alarm" else "Create Alarm",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            // Time selection section
            item {
                Text(
                    text = "Time",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
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
            }

            // Weekday selection section
            item {
                Text(
                    text = "Repeat on",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                Text(
                    text = "Leave empty for one-time alarm",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                WeekdaySelector(
                    selectedDays = alarmConfiguration.selectedDays,
                    onDaysChanged = { days ->
                        onConfigurationChanged(alarmConfiguration.copy(selectedDays = days))
                    },
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            // Tube line selection section
            item {
                Text(
                    text = "Monitor Lines",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                Text(
                    text = "Select at least one tube line",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                TubeLinePicker(
                    selectedLineIds = alarmConfiguration.selectedTubeLines,
                    onSelectionChanged = { lines ->
                        onConfigurationChanged(alarmConfiguration.copy(selectedTubeLines = lines))
                    },
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Validation error display
            if (!alarmConfiguration.isValid) {
                item {
                    Text(
                        text = alarmConfiguration.validationError ?: "Please fix the errors above",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }

            // Action buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Left button: Cancel (create mode) or Delete (edit mode)
                    if (alarmConfiguration.isEditMode && onDelete != null) {
                        OutlinedButton(
                            onClick = onDelete,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Delete")
                        }
                    } else {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                    }

                    // Right button: Save or Update
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
