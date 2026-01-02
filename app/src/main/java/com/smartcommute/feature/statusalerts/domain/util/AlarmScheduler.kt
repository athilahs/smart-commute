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
