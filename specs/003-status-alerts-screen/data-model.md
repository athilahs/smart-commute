# Data Model: Status Alerts Screen

**Feature**: `003-status-alerts-screen` | **Date**: 2025-12-29 | **Phase**: 1

## Overview

This document defines the data entities, relationships, validation rules, and state transitions for the Status Alerts feature.

---

## 1. Domain Entities

### 1.1. StatusAlert (Domain Model)

**Purpose**: Represents a user-configured alarm/alert in the domain layer (business logic).

```kotlin
package com.smartcommute.feature.statusalerts.domain.model

import java.time.DayOfWeek
import java.time.LocalTime
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
        return time.format(java.time.format.DateTimeFormatter.ofPattern("h:mm a"))
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
                .joinToString(", ") { it.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault()) }
        }
    }
}
```

**Validation Rules**:
- `selectedTubeLines` MUST NOT be empty (FR-032: "at least one tube line to be selected before saving")
- `time` MUST be valid 24-hour time (0:00 to 23:59)
- `selectedDays` CAN be empty (one-time alarm)
- Maximum 10 alarms per user enforced at repository level (FR-044, FR-045)

---

### 1.2. StatusAlertEntity (Data Layer - Room)

**Purpose**: Room database entity for persistent alarm storage.

```kotlin
package com.smartcommute.feature.statusalerts.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
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
```

**Database Schema**:
```sql
CREATE TABLE status_alerts (
    id TEXT PRIMARY KEY NOT NULL,
    hour INTEGER NOT NULL,
    minute INTEGER NOT NULL,
    selectedDaysOfWeek TEXT NOT NULL, -- Comma-separated day names
    selectedTubeLines TEXT NOT NULL, -- Comma-separated tube line IDs
    isEnabled INTEGER NOT NULL, -- 0 = false, 1 = true
    createdAt INTEGER NOT NULL,
    lastModifiedAt INTEGER NOT NULL
);

CREATE INDEX idx_status_alerts_enabled ON status_alerts(isEnabled);
CREATE INDEX idx_status_alerts_created_at ON status_alerts(createdAt);
```

---

### 1.3. TubeLine (Reuse from Feature 001)

**Purpose**: Represents a London Underground line (reused from linestatus feature).

```kotlin
package com.smartcommute.feature.linestatus.domain

data class TubeLine(
    val id: String, // e.g., "central", "northern"
    val name: String, // e.g., "Central", "Northern"
    val colorHex: String, // e.g., "#DC241F" (TfL red for Central)
    val status: String // e.g., "Good Service", "Minor Delays"
)
```

**Relationship**: Many-to-many with StatusAlert (one alarm can monitor multiple lines; one line can be in multiple alarms).

---

## 2. UI State Models

### 2.1. StatusAlertsUiState

**Purpose**: Represents the UI state for the main Status Alerts screen.

```kotlin
package com.smartcommute.feature.statusalerts.ui

import com.smartcommute.feature.statusalerts.domain.model.StatusAlert

sealed class StatusAlertsUiState {
    data object Loading : StatusAlertsUiState()

    data class Success(
        val alarms: List<StatusAlert>,
        val alarmCount: Int = alarms.size,
        val canCreateMore: Boolean = alarmCount < 10
    ) : StatusAlertsUiState()

    data class Error(
        val message: String
    ) : StatusAlertsUiState()
}
```

---

### 2.2. AlarmConfigurationState

**Purpose**: Represents the state of the alarm configuration bottom sheet.

```kotlin
package com.smartcommute.feature.statusalerts.ui

import com.smartcommute.feature.linestatus.domain.TubeLine
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

sealed class ValidationError {
    data object NoTubeLinesSelected : ValidationError()
    data object TooManyAlarms : ValidationError()
}
```

---

## 3. Repository Contracts

### 3.1. StatusAlertsRepository Interface

```kotlin
package com.smartcommute.feature.statusalerts.data.repository

import com.smartcommute.feature.statusalerts.domain.model.StatusAlert
import kotlinx.coroutines.flow.Flow

interface StatusAlertsRepository {
    /**
     * Observe all alarms (enabled + disabled), sorted by time (earliest first)
     */
    fun observeAllAlarms(): Flow<List<StatusAlert>>

    /**
     * Get all enabled alarms (for alarm scheduling)
     */
    suspend fun getEnabledAlarms(): List<StatusAlert>

    /**
     * Get a single alarm by ID
     */
    suspend fun getAlarmById(id: String): StatusAlert?

    /**
     * Create a new alarm
     * @throws IllegalStateException if alarm limit (10) is reached
     */
    suspend fun createAlarm(alarm: StatusAlert): Result<StatusAlert>

    /**
     * Update an existing alarm
     */
    suspend fun updateAlarm(alarm: StatusAlert): Result<StatusAlert>

    /**
     * Delete an alarm by ID
     */
    suspend fun deleteAlarm(id: String): Result<Unit>

    /**
     * Enable an alarm (sets isEnabled = true)
     */
    suspend fun enableAlarm(id: String): Result<Unit>

    /**
     * Disable an alarm (sets isEnabled = false)
     */
    suspend fun disableAlarm(id: String): Result<Unit>

    /**
     * Get current alarm count (for enforcing 10-alarm limit)
     */
    suspend fun getAlarmCount(): Int
}
```

---

## 4. State Transitions

### 4.1. Alarm Lifecycle

```
┌─────────────────┐
│   Not Created   │
└────────┬────────┘
         │ User taps FAB
         ↓
┌─────────────────────────────┐
│  Creating (Bottom Sheet)    │
│  - Time: default 7:30 AM    │
│  - Days: empty set          │
│  - Lines: empty set         │
└────────┬───────────┬────────┘
         │           │
         │ Save      │ Cancel
         ↓           ↓
┌─────────────────────────────┐
│   Enabled (Active)          │
│  - Scheduled with AlarmMgr  │
│  - Triggers at set time     │
│  - Shows in alarm list      │
└────────┬────────────────────┘
         │
         ├─────────────────────┐
         │ User toggles OFF    │
         ↓                     │
┌─────────────────────────────┤
│   Disabled (Inactive)       │
│  - NOT scheduled            │
│  - Shows in list (grayed)   │
│  - No notifications         │
└────────┬────────────────────┘
         │
         ├─────────────────────┐
         │ User toggles ON     │
         ↓                     │
┌─────────────────────────────┤
│   Enabled (Re-activated)    │
│  - Rescheduled              │
└─────────────────────────────┘
         │
         ├─────────────────────┐
         │ User taps alarm     │
         ↓                     │
┌─────────────────────────────┤
│   Editing (Bottom Sheet)    │
│  - Shows current config     │
│  - User modifies fields     │
└────────┬───────────┬────────┘
         │           │
         │ Save      │ Cancel/Delete
         ↓           ↓
┌─────────────────────────────┐
│   Updated Alarm             │
│  - Rescheduled if changed   │
└─────────────────────────────┘
         │
         │ User deletes
         ↓
┌─────────────────────────────┐
│   Deleted (Removed)         │
│  - Cancelled in AlarmMgr    │
│  - Removed from database    │
└─────────────────────────────┘
```

---

### 4.2. One-Time Alarm Trigger Transition

```
┌─────────────────────────────┐
│   One-Time Alarm (Enabled)  │
│  - selectedDays: empty set  │
└────────┬────────────────────┘
         │
         │ Trigger time reached
         ↓
┌─────────────────────────────┐
│   Trigger Action            │
│  - Fetch TfL API status     │
│  - Send notification        │
│  - Auto-disable alarm       │
└────────┬────────────────────┘
         │
         ↓
┌─────────────────────────────┐
│   Disabled (Expired)        │
│  - isEnabled = false        │
│  - Shows in list (grayed)   │
│  - No auto-deletion         │
└─────────────────────────────┘
```

---

### 4.3. Recurring Alarm Trigger Transition

```
┌─────────────────────────────┐
│   Recurring Alarm (Enabled) │
│  - selectedDays: non-empty  │
└────────┬────────────────────┘
         │
         │ Trigger time reached (matching weekday)
         ↓
┌─────────────────────────────┐
│   Trigger Action            │
│  - Fetch TfL API status     │
│  - Send notification        │
│  - Calculate next trigger   │
│  - Reschedule alarm         │
└────────┬────────────────────┘
         │
         ↓
┌─────────────────────────────┐
│   Recurring Alarm (Enabled) │
│  - Scheduled for next day   │
│  - Remains active           │
└─────────────────────────────┘
```

---

## 5. Validation Rules Summary

| Field | Rule | Error Message |
|-------|------|---------------|
| `selectedTubeLines` | MUST NOT be empty | "Please select at least one tube line" |
| `time` | MUST be valid time (0:00-23:59) | "Invalid time format" |
| `selectedDays` | CAN be empty (one-time) | N/A |
| `alarmCount` | MUST be < 10 before creating | "Maximum 10 alarms reached. Delete an alarm to create a new one" |
| `id` | MUST be unique | Enforced by database PRIMARY KEY |

---

## 6. Data Flow Diagram

```
┌──────────────────────────────────────────────────────────────────┐
│                        StatusAlertsScreen                         │
│  ┌───────────────────────────────────────────────────────────┐   │
│  │              StatusAlertsViewModel                         │   │
│  │  - observeAlarms()                                         │   │
│  │  - createAlarm()                                           │   │
│  │  - updateAlarm()                                           │   │
│  │  - deleteAlarm()                                           │   │
│  │  - toggleAlarmEnabled()                                    │   │
│  └────────────────────────┬──────────────────────────────────┘   │
│                           │                                       │
└───────────────────────────┼───────────────────────────────────────┘
                            │
                            ↓
┌───────────────────────────────────────────────────────────────────┐
│                   StatusAlertsRepository                          │
│  - observeAllAlarms() → Flow<List<StatusAlert>>                   │
│  - createAlarm() → schedules with AlarmManager                    │
│  - updateAlarm() → reschedules with AlarmManager                  │
│  - deleteAlarm() → cancels with AlarmManager                      │
└────────────────────┬──────────────────────────────────────────────┘
                     │
                     ↓
┌────────────────────────────────────────┬─────────────────────────┐
│          StatusAlertDao                │    AlarmScheduler       │
│  - getAll(): Flow<List<Entity>>        │  - scheduleAlarm()      │
│  - insert(entity)                      │  - cancelAlarm()        │
│  - update(entity)                      │  - rescheduleAlarm()    │
│  - delete(id)                          │  - calculateNextTrigger │
└────────────────────────────────────────┴─────────────────────────┘
```

---

## 7. Edge Case Data Handling

### 7.1. Conflicting Alarms (Same Time, Same Lines)

**Scenario**: User has both a recurring alarm (Mon-Fri @ 7:30 AM, Central line) and a one-time alarm (Monday @ 7:30 AM, Central line).

**Solution**: Recurring alarm takes precedence (FR-056, research.md decision).

**Implementation**:
```kotlin
fun resolveConflictingAlarms(alarms: List<StatusAlert>): List<StatusAlert> {
    return alarms
        .groupBy { it.time }
        .mapValues { (_, conflicting) ->
            // Prefer recurring over one-time
            conflicting.firstOrNull { it.isRecurring } ?: conflicting.first()
        }
        .values
        .toList()
}
```

---

### 7.2. Past Time Handling

**Scenario**: User creates an alarm at 8:00 PM for 7:30 AM.

**Solution**: Alarm triggers tomorrow at 7:30 AM (research.md decision).

**Implementation**: Handled in `AlarmScheduler.calculateNextTriggerTime()`

---

### 7.3. Maximum Alarm Limit

**Scenario**: User has 10 alarms and tries to create an 11th.

**Solution**: Repository throws exception; ViewModel shows error message; FAB is hidden.

**Implementation**:
```kotlin
suspend fun createAlarm(alarm: StatusAlert): Result<StatusAlert> {
    val currentCount = getAlarmCount()
    if (currentCount >= 10) {
        return Result.failure(IllegalStateException("Maximum 10 alarms reached"))
    }
    // Proceed with creation...
}
```

---

## 8. Database Migration Strategy

**Current Version**: 1 (initial)

**Future Migrations** (if needed):
- Version 2: Add `notificationPreference` field (e.g., sound, vibration options)
- Version 3: Add `snoozeCount` if snooze feature is added later

**Migration File**: `app/src/main/java/com/smartcommute/core/database/Migrations.kt`

---

## Phase 1 Entities Complete

All domain models, data entities, UI states, and validation rules defined. Proceed to contracts generation.
