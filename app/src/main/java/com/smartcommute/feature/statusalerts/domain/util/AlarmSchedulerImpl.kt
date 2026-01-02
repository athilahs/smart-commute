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
