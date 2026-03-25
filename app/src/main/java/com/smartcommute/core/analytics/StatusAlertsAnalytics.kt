package com.smartcommute.core.analytics

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatusAlertsAnalytics @Inject constructor(
    private val analytics: AnalyticsService
) {
    fun logAlarmCreated(lineCount: Int, dayCount: Int, isRecurring: Boolean, hour: Int, minute: Int) {
        analytics.log("alarm_created", mapOf(
            "line_count" to lineCount,
            "day_count" to dayCount,
            "is_recurring" to isRecurring,
            "alarm_hour" to hour,
            "alarm_minute" to minute
        ))
    }

    fun logAlarmUpdated(alarmId: String, lineCount: Int, dayCount: Int, isRecurring: Boolean) {
        analytics.log("alarm_updated", mapOf(
            "alarm_id" to alarmId,
            "line_count" to lineCount,
            "day_count" to dayCount,
            "is_recurring" to isRecurring
        ))
    }

    fun logAlarmDeleted(alarmId: String) {
        analytics.log("alarm_deleted", mapOf("alarm_id" to alarmId))
    }

    fun logAlarmToggled(alarmId: String, isEnabled: Boolean) {
        analytics.log("alarm_toggled", mapOf(
            "alarm_id" to alarmId,
            "is_enabled" to isEnabled
        ))
    }

    fun logCreateTapped(currentAlarmCount: Int) {
        analytics.log("alarm_create_tapped", mapOf("current_alarm_count" to currentAlarmCount))
    }

    fun logPermissionRequested(type: String) {
        analytics.log("permission_requested", mapOf("type" to type))
    }

    fun logPermissionGranted(type: String) {
        analytics.log("permission_granted", mapOf("type" to type))
    }

    fun logPermissionDenied(type: String) {
        analytics.log("permission_denied", mapOf("type" to type))
    }

    fun logPermissionSettingsOpened(type: String) {
        analytics.log("permission_settings_opened", mapOf("type" to type))
    }

    fun setAlarmUserProperties(alarmCount: Int, hasActiveAlarm: Boolean) {
        analytics.setUserProperty("alarm_count", alarmCount.toString())
        analytics.setUserProperty("has_active_alarm", hasActiveAlarm.toString())
    }
}
