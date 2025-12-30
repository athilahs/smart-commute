# Research: Status Alerts Screen

**Feature**: `003-status-alerts-screen` | **Date**: 2025-12-29 | **Phase**: 0

## Overview

This document consolidates research findings to resolve all NEEDS CLARIFICATION markers from the Technical Context and to identify best practices for the Status Alerts feature implementation.

---

## 1. WorkManager vs AlarmManager for Alarm Scheduling

### Decision: Use AlarmManager for Time-Critical Alarms

**Rationale**:
- **WorkManager** is designed for **deferrable** background work with flexible timing guarantees (±10-15 minutes flexibility)
- **AlarmManager** provides **exact timing** control required for user-configured alarm notifications
- Our requirement: Notifications must trigger within 30 seconds of scheduled time (FR-053, Success Criteria SC-003)
- WorkManager's `PeriodicWorkRequest` minimum interval is 15 minutes, which doesn't suit custom weekday patterns (e.g., Monday + Wednesday only)

**AlarmManager Implementation Strategy**:
```kotlin
// Use setExactAndAllowWhileIdle for API 23+
alarmManager.setExactAndAllowWhileIdle(
    AlarmManager.RTC_WAKEUP,
    triggerTimeMillis,
    alarmPendingIntent
)
```

**Required Permission** (Android 12+/API 31+):
- Manifest: `<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM"/>`
- Runtime check: `alarmManager.canScheduleExactAlarms()`

**Alternatives Considered**:
- **WorkManager**: Rejected due to inexact timing (10+ minute flexibility) and inability to schedule custom weekday patterns
- **WorkManager + AlarmManager Hybrid**: Rejected as unnecessarily complex (YAGNI principle)

---

## 2. Recurring Alarm Scheduling with Custom Weekday Patterns

### Decision: Calculate Next Trigger Time Manually + AlarmManager

**Rationale**:
- AlarmManager doesn't natively support "every Monday+Wednesday" recurring patterns
- Solution: Calculate the next occurrence based on selected weekdays and reschedule after each trigger

**Implementation Pattern**:
```kotlin
fun calculateNextTriggerTime(
    hour: Int,
    minute: Int,
    selectedDaysOfWeek: Set<DayOfWeek> // e.g., {MONDAY, WEDNESDAY, FRIDAY}
): Long {
    val now = LocalDateTime.now()
    val targetTime = LocalTime.of(hour, minute)

    // Find next matching day
    var daysAhead = 0
    while (daysAhead < 7) {
        val candidateDate = now.plusDays(daysAhead.toLong())
        val candidateDay = candidateDate.dayOfWeek

        if (selectedDaysOfWeek.contains(candidateDay)) {
            val candidateDateTime = LocalDateTime.of(candidateDate.toLocalDate(), targetTime)
            if (candidateDateTime.isAfter(now)) {
                return candidateDateTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000
            }
        }
        daysAhead++
    }

    // If no match found in next 7 days, start from beginning
    return calculateNextTriggerTime(hour, minute, selectedDaysOfWeek)
}
```

**Reschedule After Trigger**:
- In `BroadcastReceiver.onReceive()`, immediately calculate and schedule the next occurrence
- Store alarm ID in PendingIntent extras to identify which alarm triggered

**Alternatives Considered**:
- **JobScheduler Periodic Jobs**: Doesn't support exact timing or custom day patterns
- **Multiple AlarmManager Instances (One Per Day)**: Overly complex and harder to manage

---

## 3. One-Time Alarm Implementation

### Decision: AlarmManager with Single Trigger + Auto-Disable

**Rationale**:
- One-time alarms (no weekdays selected) trigger once at the specified time
- After triggering, mark alarm as disabled in Room database (FR-094: "alarm is automatically disabled or marked as expired")

**Implementation**:
```kotlin
fun scheduleOneTimeAlarm(alarmId: Int, triggerTimeMillis: Long) {
    // If trigger time is in the past, add 1 day
    val adjustedTime = if (triggerTimeMillis < System.currentTimeMillis()) {
        triggerTimeMillis + TimeUnit.DAYS.toMillis(1)
    } else {
        triggerTimeMillis
    }

    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        adjustedTime,
        createAlarmPendingIntent(alarmId, isOneTime = true)
    )
}

// In BroadcastReceiver
override fun onReceive(context: Context, intent: Intent) {
    val isOneTime = intent.getBooleanExtra("IS_ONE_TIME", false)
    if (isOneTime) {
        // Disable alarm in database
        repository.disableAlarm(alarmId)
    } else {
        // Reschedule for next occurrence
        scheduleNextRecurringAlarm(alarmId)
    }
}
```

**Alternatives Considered**:
- **Delete After Trigger**: Rejected; user may want to re-enable (as per Clock app pattern)

---

## 4. Notification Permissions (Android 13+/API 33+)

### Decision: Runtime Permission Request on Screen Open + Settings Guidance

**Required Permission**:
- Manifest: `<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>`
- Runtime request required for Android 13+ (API 33+)

**Implementation in Jetpack Compose**:
```kotlin
@Composable
fun StatusAlertsScreen() {
    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Proceed with normal UI
        } else {
            // Show rationale or settings prompt
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Rest of UI
}
```

**Handling Permission Denial**:
- Use `ActivityCompat.shouldShowRequestPermissionRationale()` to detect permanent denial
- If permanently denied, show dialog with button to open app settings:
```kotlin
val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
    data = Uri.fromParts("package", context.packageName, null)
}
context.startActivity(intent)
```

**Alternatives Considered**:
- **Google Accompanist Permissions Library**: Rejected; native Compose ActivityResultContracts is sufficient

---

## 5. Material Design 3 Components

### 5.1. ModalBottomSheet

**Decision**: Use `ModalBottomSheet` from `androidx.compose.material3`

**Implementation**:
```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmConfigurationBottomSheet(
    onDismiss: () -> Unit,
    onSave: (StatusAlert) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Time picker, weekday selector, tube line picker
        }
    }
}
```

### 5.2. TimePicker

**Decision**: Use `TimePicker` composable from Material Design 3

**Implementation**:
```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onConfirm: (Int, Int) -> Unit, // hour, minute
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onConfirm(timePickerState.hour, timePickerState.minute)
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        text = {
            TimePicker(state = timePickerState)
        }
    )
}
```

### 5.3. Weekday Selector

**Decision**: Use `FilterChip` in a `FlowRow` for multi-select weekdays

**Implementation**:
```kotlin
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WeekdaySelector(
    selectedDays: Set<DayOfWeek>,
    onDaysChanged: (Set<DayOfWeek>) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        DayOfWeek.values().forEach { day ->
            FilterChip(
                selected = selectedDays.contains(day),
                onClick = {
                    val newSelection = if (selectedDays.contains(day)) {
                        selectedDays - day
                    } else {
                        selectedDays + day
                    }
                    onDaysChanged(newSelection)
                },
                label = { Text(day.getDisplayName(TextStyle.SHORT, Locale.getDefault())) }
            )
        }
    }
}
```

### 5.4. Tube Line Picker

**Decision**: Use `Checkbox` + `LazyColumn` for multi-select tube lines

**Rationale**:
- Chips would be too crowded for 11 tube lines
- Checkboxes in a list provide better scrollability and line information display (line color, name)

**Implementation**:
```kotlin
@Composable
fun TubeLinePicker(
    availableLines: List<TubeLine>,
    selectedLines: Set<String>,
    onSelectionChanged: (Set<String>) -> Unit
) {
    LazyColumn {
        items(availableLines) { line ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val newSelection = if (selectedLines.contains(line.id)) {
                            selectedLines - line.id
                        } else {
                            selectedLines + line.id
                        }
                        onSelectionChanged(newSelection)
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = selectedLines.contains(line.id),
                    onCheckedChange = null // Handled by Row click
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color(line.colorHex), CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(line.name, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
```

### 5.5. FAB Visibility Handling

**Decision**: Use `Scaffold` with conditional FAB

**Implementation**:
```kotlin
@Composable
fun StatusAlertsScreen(
    alarmCount: Int,
    onCreateAlarm: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            if (alarmCount < 10) {
                FloatingActionButton(onClick = onCreateAlarm) {
                    Icon(Icons.Default.Add, contentDescription = "Create Alarm")
                }
            }
        }
    ) { padding ->
        // Alarm list content
    }
}
```

### 5.6. Empty State

**Decision**: Reuse existing `EmptyStateCard` pattern from linedetails feature

**Implementation**: (Adapt existing pattern)
```kotlin
@Composable
fun AlarmsEmptyState(onCreateFirstAlarm: () -> Unit) {
    EmptyStateCard(
        title = "No alarms configured",
        description = "Create your first status alert to receive notifications about tube line disruptions",
        actionText = "Create Alert",
        onActionClick = onCreateFirstAlarm
    )
}
```

**Alternatives Considered**:
- **Custom Empty State**: Rejected; reusing existing component maintains consistency

---

## 6. Notification Implementation

### 6.1. Silent vs Audible Notifications

**Decision**: Use Notification Channels with Different Importance Levels

**Silent Notification (Good Service)**:
```kotlin
val silentChannel = NotificationChannel(
    "status_alerts_silent",
    "Silent Status Updates",
    NotificationManager.IMPORTANCE_LOW
).apply {
    setSound(null, null)
    enableVibration(false)
}

val notification = NotificationCompat.Builder(context, "status_alerts_silent")
    .setSmallIcon(R.drawable.ic_tube_status)
    .setContentTitle("Line Status: Good Service")
    .setContentText("All selected lines running normally")
    .build()
```

**Audible Notification (Disruptions)**:
```kotlin
val audibleChannel = NotificationChannel(
    "status_alerts_urgent",
    "Urgent Status Alerts",
    NotificationManager.IMPORTANCE_HIGH
).apply {
    setSound(
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
        AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .build()
    )
    enableVibration(true)
}

val notification = NotificationCompat.Builder(context, "status_alerts_urgent")
    .setSmallIcon(R.drawable.ic_tube_emergency)
    .setContentTitle("Service Disruption")
    .setContentText("Central Line - Minor Delays")
    .setPriority(NotificationCompat.PRIORITY_HIGH)
    .build()
```

### 6.2. Expandable Notifications for Multiple Tube Lines

**Decision**: Use `InboxStyle` for multiple line statuses

**Implementation**:
```kotlin
val inboxStyle = NotificationCompat.InboxStyle()
    .setBigContentTitle("Status Update: ${lineStatuses.size} Lines")
    .setSummaryText("Tap to view details")

lineStatuses.forEach { (lineName, status) ->
    inboxStyle.addLine("$lineName: $status")
}

val notification = NotificationCompat.Builder(context, channelId)
    .setSmallIcon(R.drawable.ic_tube_status)
    .setContentTitle("Multiple Line Updates")
    .setContentText("${lineStatuses.size} lines affected")
    .setStyle(inboxStyle)
    .build()
```

---

## 7. Device Reboot Persistence

### Decision: Use `BOOT_COMPLETED` BroadcastReceiver + Room Database

**Required Permission**:
- Manifest: `<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>`

**Implementation**:
```kotlin
class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule all enabled alarms from database
            val repository = (context.applicationContext as SmartCommuteApp)
                .appContainer
                .statusAlertsRepository

            CoroutineScope(Dispatchers.IO).launch {
                val enabledAlarms = repository.getEnabledAlarms()
                enabledAlarms.forEach { alarm ->
                    AlarmScheduler.scheduleAlarm(context, alarm)
                }
            }
        }
    }
}
```

**AndroidManifest.xml**:
```xml
<receiver
    android:name=".feature.statusalerts.data.receiver.BootCompletedReceiver"
    android:exported="false">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />
    </intent-filter>
</receiver>
```

---

## 8. Dependency Versions

### WorkManager (Not Primary, But Available for Future Use)

**Recommended Version**: `androidx.work:work-runtime-ktx:2.9.0` (latest as of 2025)

**Rationale**: Although we're using AlarmManager for exact timing, WorkManager may be useful for future background tasks (e.g., periodically syncing tube line status cache)

**Gradle Dependency** (add to `gradle/libs.versions.toml`):
```toml
[versions]
workmanager = "2.9.0"

[libraries]
workmanager-runtime = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "workmanager" }
```

**Implementation Decision**: Add WorkManager dependency during Phase 1 data model design if background status caching is needed; otherwise defer to future feature.

---

## 9. Edge Case Handling

### 9.1. Past Time Handling

**Decision**: Add 1 day if scheduled time has passed today

**Implementation**: (See One-Time Alarm section above)

### 9.2. Missed Alarm Handling

**Decision**: Silent skip (no retroactive notification)

**Rationale**: Matches Android Clock app behavior (FR-088: "silently skip the missed alarm")

### 9.3. TfL API Unavailable at Trigger Time

**Decision**: Show error notification with specific message

**Implementation**:
```kotlin
try {
    val lineStatuses = tflApiService.getLineStatus(selectedLines)
    sendStatusNotification(lineStatuses)
} catch (e: Exception) {
    sendErrorNotification(
        title = "Status Check Failed",
        message = "We tried to check the status for the following lines: " +
                  "${selectedLines.joinToString(", ")} but an error occurred"
    )
}
```

**Notification Channel**: Use same "default" channel as informational notifications

### 9.4. Conflicting Alarms (Recurring + One-Time)

**Decision**: Recurring alarm takes precedence

**Implementation**:
```kotlin
fun getNextAlarmToTrigger(alarms: List<StatusAlert>): StatusAlert? {
    val nextTime = calculateNextTriggerTime()
    val conflictingAlarms = alarms.filter { it.nextTriggerTime == nextTime }

    return when {
        conflictingAlarms.isEmpty() -> null
        conflictingAlarms.size == 1 -> conflictingAlarms.first()
        else -> {
            // Prefer recurring over one-time
            conflictingAlarms.firstOrNull { it.selectedDays.isNotEmpty() }
                ?: conflictingAlarms.first()
        }
    }
}
```

---

## 10. Room Database Schema

### StatusAlert Entity

**Decision**: Store weekdays as comma-separated string; tube lines as separate junction table

**Rationale**:
- Weekdays: Small set (max 7), comma-separated is simple and sufficient
- Tube lines: Many-to-many relationship with potential for reuse across features

**Schema**:
```kotlin
@Entity(tableName = "status_alerts")
data class StatusAlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hour: Int,
    val minute: Int,
    val selectedDaysOfWeek: String, // Comma-separated: "MONDAY,WEDNESDAY,FRIDAY"
    val isEnabled: Boolean,
    val createdAt: Long,
    val lastModifiedAt: Long
)

@Entity(tableName = "alert_tube_lines")
data class AlertTubeLineEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val alertId: Int,
    val tubeLineId: String
)
```

---

## Summary of Key Decisions

| Topic | Decision | Rationale |
|-------|----------|-----------|
| Alarm Scheduling | AlarmManager | Exact timing required (WorkManager has ±10-15 min flexibility) |
| Recurring Patterns | Manual calculation + reschedule | AlarmManager doesn't support custom day patterns |
| Notification Permission | Runtime request on screen open | Required for Android 13+; graceful fallback for denial |
| Material Design 3 UI | ModalBottomSheet, TimePicker, FilterChip, Checkbox list | Native M3 components for consistency |
| Silent vs Audible | Notification channels with different importance | Leverages Android 8+ channel architecture |
| Device Reboot | BOOT_COMPLETED receiver + Room | Standard Android pattern for alarm persistence |
| WorkManager Version | 2.9.0 (optional, not primary) | Latest stable; may be useful for future background tasks |
| One-Time Alarms | AlarmManager + auto-disable | Matches Android Clock app behavior |
| Edge Cases | Explicit handling per spec | Past time +1 day; missed alarm silent skip; API error notification |

---

## Phase 0 Complete

All NEEDS CLARIFICATION markers resolved. Proceed to Phase 1 (Data Model, Contracts, Quickstart).
