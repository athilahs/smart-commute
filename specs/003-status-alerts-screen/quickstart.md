# Quickstart Guide: Status Alerts Screen Implementation

**Feature**: `003-status-alerts-screen` | **Date**: 2025-12-29

## Overview

This quickstart guide provides the implementation order, critical paths, and key integration points for building the Status Alerts feature.

---

## Prerequisites

### Dependencies to Add

Update `gradle/libs.versions.toml`:

```toml
[versions]
# Already exists:
# kotlin = "2.3.0"
# compose-bom = "2025.12.01"
# hilt = "2.57.2"
# room = "2.8.4"
# (No additional dependencies needed - reuse existing)

[libraries]
# All required dependencies already present in existing project
# (No additions needed)
```

### Manifest Updates

Add to `AndroidManifest.xml`:

```xml
<!-- Permissions -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

<!-- Receivers -->
<receiver
    android:name=".feature.statusalerts.data.receiver.AlarmReceiver"
    android:exported="false">
    <intent-filter>
        <action android:name="com.smartcommute.ALARM_TRIGGERED" />
    </intent-filter>
</receiver>

<receiver
    android:name=".feature.statusalerts.data.receiver.BootCompletedReceiver"
    android:exported="false">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />
    </intent-filter>
</receiver>
```

---

## Implementation Order

### Phase 1: Data Layer (Days 1-2)

**Critical Path**: Room database → Repository → AlarmScheduler

1. **Create Room Entity** (`StatusAlertEntity.kt`)
   - Location: `com.smartcommute.feature.statusalerts.data.local`
   - Contract: [alarm-entity.md](./contracts/alarm-entity.md)
   - Dependencies: None

2. **Create DAO** (`StatusAlertDao.kt`)
   - Location: `com.smartcommute.feature.statusalerts.data.local`
   - Contract: [alarm-entity.md](./contracts/alarm-entity.md)
   - Dependencies: StatusAlertEntity

3. **Update Room Database** (Modify existing `SmartCommuteDatabase.kt`)
   - Location: `com.smartcommute.core.database` (or similar)
   - Add StatusAlertEntity to entities list
   - Add StatusAlertDao abstract method
   - Increment database version

4. **Create Domain Model** (`StatusAlert.kt`)
   - Location: `com.smartcommute.feature.statusalerts.domain.model`
   - Contract: [data-model.md](./data-model.md)
   - Dependencies: None

5. **Create AlarmScheduler** (`AlarmSchedulerImpl.kt`)
   - Location: `com.smartcommute.feature.statusalerts.domain.util`
   - Contract: [alarm-scheduler.md](./contracts/alarm-scheduler.md)
   - Dependencies: AlarmManager

6. **Create Repository** (`StatusAlertsRepositoryImpl.kt`)
   - Location: `com.smartcommute.feature.statusalerts.data.repository`
   - Contract: [alarm-repository.md](./contracts/alarm-repository.md)
   - Dependencies: StatusAlertDao, AlarmScheduler

7. **Create BroadcastReceivers** (`AlarmReceiver.kt`, `BootCompletedReceiver.kt`)
   - Location: `com.smartcommute.feature.statusalerts.data.receiver`
   - Contract: [alarm-scheduler.md](./contracts/alarm-scheduler.md)
   - Dependencies: StatusAlertsRepository, AlarmScheduler

8. **Create Hilt Modules** (`AlarmModule.kt`, `StatusAlertsRepositoryModule.kt`)
   - Location: `com.smartcommute.feature.statusalerts.di`
   - Provide AlarmManager, bind Repository

**Testing**: Manually verify CRUD operations via ViewModel stubs

---

### Phase 2: UI Components (Days 3-4)

**Critical Path**: Empty State → Alarm List → FAB → Bottom Sheet

1. **Create UI State Models** (`StatusAlertsUiState.kt`, `AlarmConfigurationState.kt`)
   - Location: `com.smartcommute.feature.statusalerts.ui`
   - Contract: [data-model.md](./data-model.md)
   - Dependencies: StatusAlert domain model

2. **Create ViewModel** (`StatusAlertsViewModel.kt`)
   - Location: `com.smartcommute.feature.statusalerts.ui`
   - Expose StateFlow<StatusAlertsUiState>
   - Handle user actions (create, update, delete, toggle)
   - Dependencies: StatusAlertsRepository

3. **Create Empty State** (`EmptyStateView.kt` or reuse existing `EmptyStateCard.kt`)
   - Location: `com.smartcommute.feature.statusalerts.ui`
   - Show when alarms list is empty
   - Call-to-action button to create first alarm

4. **Create Alarm List Item** (`AlarmListItem.kt`)
   - Location: `com.smartcommute.feature.statusalerts.ui`
   - Display time, weekdays, tube lines
   - Toggle switch for enable/disable
   - Tap to open bottom sheet

5. **Create Main Screen** (`StatusAlertsScreen.kt`)
   - Location: `com.smartcommute.feature.statusalerts.ui`
   - Scaffold with FAB
   - LazyColumn for alarm list
   - Empty state conditional
   - FAB visibility based on alarm count

6. **Create Weekday Selector** (`WeekdaySelector.kt`)
   - Location: `com.smartcommute.feature.statusalerts.ui.components`
   - FilterChip multi-select for days of week
   - Contract: [research.md](./research.md) section 5.3

7. **Create Time Picker** (`TimePickerDialog.kt`)
   - Location: `com.smartcommute.feature.statusalerts.ui.components`
   - Material Design 3 TimePicker in AlertDialog
   - Contract: [research.md](./research.md) section 5.2

8. **Create Tube Line Picker** (`TubeLinePicker.kt`)
   - Location: `com.smartcommute.feature.statusalerts.ui.components`
   - Checkbox + LazyColumn for tube lines
   - Reuse TubeLine domain model from feature 001
   - Contract: [research.md](./research.md) section 5.4

9. **Create Bottom Sheet** (`AlarmBottomSheet.kt`)
   - Location: `com.smartcommute.feature.statusalerts.ui`
   - ModalBottomSheet with time picker, weekday selector, tube line picker
   - Save/Cancel buttons
   - Validation error display
   - Contract: [research.md](./research.md) section 5.1

**Testing**: Manually verify UI flows (create, edit, delete alarms)

---

### Phase 3: Notification System (Day 5)

**Critical Path**: Notification Channels → Notification Manager → Permission Handling

1. **Create Notification Channels** (Initialize in Application class)
   - Location: `com.smartcommute.SmartCommuteApplication.kt` (or similar)
   - Create silent, default, and urgent channels
   - Contract: [research.md](./research.md) section 6.1

2. **Create Notification Manager** (`NotificationManager.kt`)
   - Location: `com.smartcommute.feature.statusalerts.notification`
   - sendStatusNotification()
   - sendErrorNotification()
   - Use InboxStyle for multiple lines
   - Contract: [research.md](./research.md) sections 6.1, 6.2

3. **Create Permission Handler** (Composable in StatusAlertsScreen)
   - Request POST_NOTIFICATIONS on Android 13+
   - Show rationale dialog on denial
   - Guide to settings if permanently denied
   - Contract: [research.md](./research.md) section 4

4. **Integrate TfL API Call** (In AlarmReceiver)
   - Reuse existing TfL API service from feature 001
   - Fetch line statuses for selected tube lines
   - Handle network errors gracefully

**Testing**: Manually verify notifications (silent vs audible, error notifications)

---

### Phase 4: Navigation & Integration (Day 6)

**Critical Path**: Navigation Route → Bottom Nav → Testing

1. **Add Navigation Route** (Modify `NavGraph.kt`)
   - Location: `com.smartcommute.core.navigation`
   - Add composable for StatusAlertsScreen
   - Route: "status_alerts"

2. **Update Bottom Navigation** (Modify `MainScreen.kt` or similar)
   - Location: `com.smartcommute.ui`
   - Add "Alerts" tab to bottom navigation bar
   - Icon: Notification bell or alarm clock
   - Navigate to StatusAlertsScreen on tap

3. **End-to-End Testing**
   - Create alarms
   - Verify alarm triggers at scheduled time
   - Verify notifications (silent vs audible)
   - Verify boot persistence
   - Verify 10-alarm limit
   - Verify enable/disable toggle
   - Verify edit and delete

---

## Critical Integration Points

### 1. Reusing TubeLine Data (Feature 001)

**Location**: `com.smartcommute.feature.linestatus.domain.TubeLine`

**Usage**:
```kotlin
// Fetch tube lines from existing repository
val tubeLines: List<TubeLine> = lineStatusRepository.getTubeLines()

// Use in TubeLinePicker
TubeLinePicker(
    availableLines = tubeLines,
    selectedLines = selectedTubeLineIds,
    onSelectionChanged = { /* update state */ }
)
```

**Note**: TubeLine model includes line color (hex) for visual representation

---

### 2. Shared Room Database

**Location**: Existing database (e.g., `SmartCommuteDatabase.kt` in `core.database`)

**Update**:
```kotlin
@Database(
    entities = [
        // Existing entities from features 001 and 002
        LineStatusEntity::class,
        LineDetailsEntity::class,
        // NEW: Status Alerts entity
        StatusAlertEntity::class
    ],
    version = 2, // Increment version
    exportSchema = true
)
abstract class SmartCommuteDatabase : RoomDatabase() {
    // Existing DAOs
    abstract fun lineStatusDao(): LineStatusDao
    abstract fun lineDetailsDao(): LineDetailsDao

    // NEW: Status Alerts DAO
    abstract fun statusAlertDao(): StatusAlertDao
}
```

**Migration**:
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE status_alerts (
                id TEXT PRIMARY KEY NOT NULL,
                hour INTEGER NOT NULL,
                minute INTEGER NOT NULL,
                selectedDaysOfWeek TEXT NOT NULL,
                selectedTubeLines TEXT NOT NULL,
                isEnabled INTEGER NOT NULL,
                createdAt INTEGER NOT NULL,
                lastModifiedAt INTEGER NOT NULL
            )
        """.trimIndent())

        database.execSQL("""
            CREATE INDEX idx_status_alerts_enabled
            ON status_alerts(isEnabled)
        """.trimIndent())
    }
}
```

---

### 3. TfL API Integration (Feature 001)

**Location**: `com.smartcommute.feature.linestatus.data.repository.LineStatusRepository`

**Reuse Existing API Service**:
```kotlin
// In AlarmReceiver
@Inject
lateinit var tflApiService: TflApiService // Reuse from feature 001

override fun onReceive(context: Context, intent: Intent) {
    val tubeLineIds = intent.getStringExtra("selected_tube_lines")?.split(",") ?: emptyList()

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val lineStatuses = tflApiService.getLineStatuses(tubeLineIds)
            // Send notification with statuses
        } catch (e: Exception) {
            // Send error notification
        }
    }
}
```

---

### 4. Bottom Navigation (Existing)

**Location**: `com.smartcommute.ui.MainScreen.kt`

**Update BottomNavigationItem**:
```kotlin
sealed class BottomNavScreen(val route: String, val title: String, val icon: ImageVector) {
    // Existing screens
    object LineStatus : BottomNavScreen("line_status", "Status", Icons.Default.Home)

    // NEW: Status Alerts screen
    object StatusAlerts : BottomNavScreen("status_alerts", "Alerts", Icons.Default.Notifications)
}
```

---

## Development Tips

### 1. Use Fake Data for UI Development

Create a preview provider to speed up Compose UI development:

```kotlin
@Preview
@Composable
fun StatusAlertsScreenPreview() {
    val fakeAlarms = listOf(
        StatusAlert(
            id = "1",
            time = LocalTime.of(7, 30),
            selectedDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
            selectedTubeLines = setOf("central", "northern"),
            isEnabled = true
        )
    )

    SmartCommuteTheme {
        StatusAlertsScreen(
            alarms = fakeAlarms,
            onCreateAlarm = {},
            onToggleAlarm = { _, _ -> },
            onEditAlarm = {},
            onDeleteAlarm = {}
        )
    }
}
```

---

### 2. Debug Alarm Scheduling

Use `adb` to inspect scheduled alarms:

```bash
# List all alarms for the app
adb shell dumpsys alarm | grep com.smartcommute

# Check next alarm trigger time
adb shell dumpsys alarm | grep -A 5 "com.smartcommute.ALARM_TRIGGERED"
```

---

### 3. Test Notification Permissions

Force reset notification permission for testing:

```bash
# Revoke permission
adb shell pm revoke com.smartcommute android.permission.POST_NOTIFICATIONS

# Grant permission
adb shell pm grant com.smartcommute android.permission.POST_NOTIFICATIONS
```

---

### 4. Simulate Device Reboot

Test boot persistence without physically rebooting:

```bash
# Send BOOT_COMPLETED broadcast
adb shell am broadcast -a android.intent.action.BOOT_COMPLETED
```

---

### 5. Fast-Forward System Time (Testing Only)

Trigger alarms immediately by changing system time:

```bash
# Set time to 7:30 AM
adb shell su root date 073000

# Restore automatic time
adb shell settings put global auto_time 1
```

**Warning**: Requires root access; only use on emulators

---

## Common Pitfalls

### 1. AlarmManager Doze Mode

**Issue**: Alarms may not trigger on time when device is in Doze mode

**Solution**: Use `setExactAndAllowWhileIdle()` API (already implemented in AlarmSchedulerImpl)

---

### 2. PendingIntent Immutability

**Issue**: Android 12+ requires FLAG_IMMUTABLE for PendingIntents

**Solution**: Always use `PendingIntent.FLAG_IMMUTABLE` flag (already in contract)

---

### 3. Room Database Migration Errors

**Issue**: App crashes on version mismatch

**Solution**: Always add migration path or use `fallbackToDestructiveMigration()` for development

---

### 4. Compose Recomposition Loops

**Issue**: Infinite recomposition when using mutable state incorrectly

**Solution**: Use `remember`, `rememberSaveable`, and `derivedStateOf` appropriately

---

### 5. WorkManager Confusion

**Issue**: WorkManager doesn't support exact timing

**Solution**: Use AlarmManager for exact alarms (as per research decision)

---

## Testing Checklist

- [ ] Create alarm (weekdays selected)
- [ ] Create alarm (no weekdays - one-time)
- [ ] Edit alarm time
- [ ] Edit alarm weekdays
- [ ] Edit alarm tube lines
- [ ] Delete alarm
- [ ] Toggle alarm enabled/disabled
- [ ] Verify notification triggers at scheduled time
- [ ] Verify silent notification for "Good Service"
- [ ] Verify audible notification for disruptions
- [ ] Verify error notification when TfL API fails
- [ ] Verify 10-alarm limit enforcement
- [ ] Verify FAB hides at 10 alarms
- [ ] Verify empty state shows when no alarms
- [ ] Verify alarms persist after app restart
- [ ] Verify alarms reschedule after device reboot
- [ ] Verify notification permission request on Android 13+
- [ ] Verify multiple tube lines display in notification

---

## File Structure Reference

```
app/src/main/java/com/smartcommute/
├── feature/statusalerts/
│   ├── ui/
│   │   ├── StatusAlertsScreen.kt
│   │   ├── StatusAlertsViewModel.kt
│   │   ├── AlarmListItem.kt
│   │   ├── AlarmBottomSheet.kt
│   │   ├── EmptyStateView.kt
│   │   └── components/
│   │       ├── TimePickerDialog.kt
│   │       ├── WeekdaySelector.kt
│   │       └── TubeLinePicker.kt
│   ├── data/
│   │   ├── local/
│   │   │   ├── StatusAlertEntity.kt
│   │   │   ├── StatusAlertDao.kt
│   │   │   └── StatusAlertsDatabase.kt
│   │   ├── repository/
│   │   │   ├── StatusAlertsRepository.kt
│   │   │   └── StatusAlertsRepositoryImpl.kt
│   │   └── receiver/
│   │       ├── AlarmReceiver.kt
│   │       └── BootCompletedReceiver.kt
│   ├── domain/
│   │   ├── model/
│   │   │   └── StatusAlert.kt
│   │   └── util/
│   │       ├── AlarmScheduler.kt
│   │       └── AlarmSchedulerImpl.kt
│   ├── notification/
│   │   └── NotificationManager.kt
│   └── di/
│       ├── AlarmModule.kt
│       └── StatusAlertsRepositoryModule.kt
└── core/
    └── navigation/
        └── NavGraph.kt (UPDATE)
```

---

## Phase 1 Complete

Implementation order, critical paths, integration points, and testing checklist fully documented. Proceed to agent context update.
