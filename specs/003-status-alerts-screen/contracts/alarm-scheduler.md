# Contract: AlarmManager Integration

**Feature**: `003-status-alerts-screen` | **Type**: System Integration | **Date**: 2025-12-29

## Purpose

Defines the AlarmManager integration contract for scheduling exact-time alarm triggers and handling alarm broadcasts.

---

## AlarmScheduler Utility

### Interface

```kotlin
package com.smartcommute.feature.statusalerts.domain.util

import android.content.Context
import com.smartcommute.feature.statusalerts.domain.model.StatusAlert

interface AlarmScheduler {
    /**
     * Schedule an alarm using Android AlarmManager
     * @param context Application or Activity context
     * @param alarm StatusAlert to schedule
     */
    fun scheduleAlarm(context: Context, alarm: StatusAlert)

    /**
     * Cancel an existing scheduled alarm
     * @param context Application or Activity context
     * @param alarmId Unique identifier of the alarm to cancel
     */
    fun cancelAlarm(context: Context, alarmId: String)

    /**
     * Reschedule an alarm (cancel + schedule)
     * @param context Application or Activity context
     * @param alarm Updated StatusAlert
     */
    fun rescheduleAlarm(context: Context, alarm: StatusAlert) {
        cancelAlarm(context, alarm.id)
        scheduleAlarm(context, alarm)
    }

    /**
     * Calculate next trigger time in milliseconds (Unix timestamp)
     * @param alarm StatusAlert with time and selected days
     * @return Next trigger time in milliseconds, or null if alarm is disabled
     */
    fun calculateNextTriggerTime(alarm: StatusAlert): Long?
}
```

---

## Implementation

### AlarmSchedulerImpl

```kotlin
package com.smartcommute.feature.statusalerts.domain.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.smartcommute.feature.statusalerts.data.receiver.AlarmReceiver
import com.smartcommute.feature.statusalerts.domain.model.StatusAlert
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

class AlarmSchedulerImpl @Inject constructor(
    private val alarmManager: AlarmManager
) : AlarmScheduler {

    override fun scheduleAlarm(context: Context, alarm: StatusAlert) {
        if (!alarm.isEnabled) return // Don't schedule disabled alarms

        val triggerTimeMillis = calculateNextTriggerTime(alarm) ?: return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_ALARM_TRIGGERED
            putExtra(EXTRA_ALARM_ID, alarm.id)
            putExtra(EXTRA_IS_ONE_TIME, alarm.isOneTime)
            putExtra(EXTRA_SELECTED_TUBE_LINES, alarm.selectedTubeLines.joinToString(","))
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.hashCode(), // Use hashCode as request code
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Use exact alarm API
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            } else {
                // Fall back to inexact alarm if permission denied
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMillis,
                pendingIntent
            )
        }
    }

    override fun cancelAlarm(context: Context, alarmId: String) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_ALARM_TRIGGERED
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }

    override fun calculateNextTriggerTime(alarm: StatusAlert): Long? {
        if (!alarm.isEnabled) return null

        val now = LocalDateTime.now()
        val targetTime = alarm.time

        return if (alarm.isOneTime) {
            // One-time alarm: schedule for today if time hasn't passed, else tomorrow
            val todayTarget = LocalDateTime.of(now.toLocalDate(), targetTime)
            val triggerTime = if (todayTarget.isAfter(now)) {
                todayTarget
            } else {
                todayTarget.plusDays(1)
            }
            triggerTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } else {
            // Recurring alarm: find next matching weekday
            findNextRecurringTrigger(now, targetTime, alarm.selectedDays)
        }
    }

    private fun findNextRecurringTrigger(
        now: LocalDateTime,
        targetTime: LocalTime,
        selectedDays: Set<DayOfWeek>
    ): Long {
        if (selectedDays.isEmpty()) return 0L // Safety check

        var daysAhead = 0
        while (daysAhead < 7) {
            val candidateDate = now.plusDays(daysAhead.toLong())
            val candidateDay = candidateDate.dayOfWeek

            if (selectedDays.contains(candidateDay)) {
                val candidateDateTime = LocalDateTime.of(candidateDate.toLocalDate(), targetTime)
                if (candidateDateTime.isAfter(now)) {
                    return candidateDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                }
            }
            daysAhead++
        }

        // No match found in next 7 days (shouldn't happen), default to next week
        val nextWeekStart = now.plusDays(7)
        return findNextRecurringTrigger(nextWeekStart, targetTime, selectedDays)
    }

    companion object {
        const val ACTION_ALARM_TRIGGERED = "com.smartcommute.ALARM_TRIGGERED"
        const val EXTRA_ALARM_ID = "alarm_id"
        const val EXTRA_IS_ONE_TIME = "is_one_time"
        const val EXTRA_SELECTED_TUBE_LINES = "selected_tube_lines"
    }
}
```

---

## BroadcastReceiver

### AlarmReceiver

```kotlin
package com.smartcommute.feature.statusalerts.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.smartcommute.feature.statusalerts.data.repository.StatusAlertsRepository
import com.smartcommute.feature.statusalerts.domain.util.AlarmScheduler
import com.smartcommute.feature.statusalerts.notification.NotificationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: StatusAlertsRepository

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var notificationManager: NotificationManager

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_ID) ?: return
        val isOneTime = intent.getBooleanExtra(AlarmScheduler.EXTRA_IS_ONE_TIME, false)
        val tubeLines = intent.getStringExtra(AlarmScheduler.EXTRA_SELECTED_TUBE_LINES)
            ?.split(",") ?: emptyList()

        // Use goAsync() for long-running operations
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Fetch tube line statuses from TfL API
                val lineStatuses = fetchTubeLineStatuses(tubeLines)

                // Determine if notification should be silent or audible
                val hasDisruptions = lineStatuses.any { it.status != "Good Service" }

                // Send notification
                notificationManager.sendStatusNotification(
                    lineStatuses = lineStatuses,
                    isSilent = !hasDisruptions
                )

                // Handle post-trigger actions
                if (isOneTime) {
                    // Disable one-time alarm
                    repository.disableAlarm(alarmId)
                } else {
                    // Reschedule recurring alarm
                    val alarm = repository.getAlarmById(alarmId)
                    if (alarm != null) {
                        alarmScheduler.scheduleAlarm(context, alarm)
                    }
                }
            } catch (e: Exception) {
                // Send error notification
                notificationManager.sendErrorNotification(
                    tubeLines = tubeLines,
                    errorMessage = "Failed to check status: ${e.message}"
                )
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun fetchTubeLineStatuses(tubeLines: List<String>): List<LineStatusResult> {
        // Implementation: Call TfL API repository to fetch statuses
        // Returns list of LineStatusResult(lineName, status)
        // This will reuse existing TfL API integration from feature 001
        TODO("Implement TfL API status fetch")
    }
}

data class LineStatusResult(
    val lineName: String,
    val status: String
)
```

---

## AndroidManifest.xml Configuration

### Permissions

```xml
<!-- Required for scheduling exact alarms (Android 12+/API 31+) -->
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />

<!-- Required for rescheduling alarms after device reboot -->
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```

### Receiver Declaration

```xml
<receiver
    android:name=".feature.statusalerts.data.receiver.AlarmReceiver"
    android:exported="false">
    <intent-filter>
        <action android:name="com.smartcommute.ALARM_TRIGGERED" />
    </intent-filter>
</receiver>
```

---

## Boot Completed Receiver

### BootCompletedReceiver

```kotlin
package com.smartcommute.feature.statusalerts.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.smartcommute.feature.statusalerts.data.repository.StatusAlertsRepository
import com.smartcommute.feature.statusalerts.domain.util.AlarmScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: StatusAlertsRepository

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Reschedule all enabled alarms
                    val enabledAlarms = repository.getEnabledAlarms()
                    enabledAlarms.forEach { alarm ->
                        alarmScheduler.scheduleAlarm(context, alarm)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
```

**Manifest Declaration**:
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

## Hilt Module

### AlarmModule

```kotlin
package com.smartcommute.feature.statusalerts.di

import android.app.AlarmManager
import android.content.Context
import com.smartcommute.feature.statusalerts.domain.util.AlarmScheduler
import com.smartcommute.feature.statusalerts.domain.util.AlarmSchedulerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AlarmModule {

    @Provides
    @Singleton
    fun provideAlarmManager(@ApplicationContext context: Context): AlarmManager {
        return context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    @Provides
    @Singleton
    fun provideAlarmScheduler(alarmManager: AlarmManager): AlarmScheduler {
        return AlarmSchedulerImpl(alarmManager)
    }
}
```

---

## Testing Strategy (Manual Verification)

### Test Schedule Alarm
```kotlin
// Create test alarm
val alarm = StatusAlert(
    id = "test-123",
    time = LocalTime.of(10, 30),
    selectedDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
    selectedTubeLines = setOf("central"),
    isEnabled = true
)

// Schedule
alarmScheduler.scheduleAlarm(context, alarm)

// Verify via adb
adb shell dumpsys alarm | grep com.smartcommute
```

### Test Cancel Alarm
```kotlin
alarmScheduler.cancelAlarm(context, "test-123")
```

### Test Boot Persistence
1. Schedule alarms
2. Reboot device
3. Verify alarms are rescheduled (check via `dumpsys alarm`)

---

## Edge Cases Handling

| Scenario | Behavior |
|----------|----------|
| Alarm triggers while device is off | Missed; no retroactive notification (per spec) |
| Alarm triggers while in airplane mode | Missed; network unavailable → error notification when back online |
| Multiple alarms at same time | Each triggers independently (separate PendingIntents) |
| User changes system time | Alarms adjust to new time zone automatically (RTC_WAKEUP) |
| App is force-stopped | Alarms are canceled by Android (documented limitation) |

---

## Contract Complete

AlarmManager integration, BroadcastReceiver, boot persistence, and edge case handling fully specified.
