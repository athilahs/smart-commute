package com.smartcommute.core.analytics

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationAnalytics @Inject constructor(
    private val analytics: AnalyticsService
) {
    fun logAlarmFired(alarmId: String, lineCount: Int, isRecurring: Boolean) {
        analytics.log("alarm_fired", mapOf(
            "alarm_id" to alarmId,
            "line_count" to lineCount,
            "is_recurring" to isRecurring
        ))
    }

    fun logAlarmResult(alarmId: String, disruptedCount: Int, totalChecked: Int, linesDisrupted: String) {
        analytics.log("alarm_result", mapOf(
            "alarm_id" to alarmId,
            "disrupted_count" to disruptedCount,
            "total_checked" to totalChecked,
            "lines_disrupted" to linesDisrupted
        ))
    }

    fun logNotificationSent(channel: String, disruptedCount: Int) {
        analytics.log("notification_sent", mapOf(
            "channel" to channel,
            "disrupted_count" to disruptedCount
        ))
    }

    fun logNotificationTapped(destination: String, lineId: String? = null) {
        val params = mutableMapOf<String, Any>("destination" to destination)
        lineId?.let { params["line_id"] = it }
        analytics.log("notification_tapped", params)
    }

    fun logAlarmFetchFailed(alarmId: String, errorType: String) {
        analytics.log("alarm_fetch_failed", mapOf(
            "alarm_id" to alarmId,
            "error_type" to errorType
        ))
    }
}
