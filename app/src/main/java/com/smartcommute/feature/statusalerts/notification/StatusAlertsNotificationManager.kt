package com.smartcommute.feature.statusalerts.notification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager as AndroidNotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.smartcommute.MainActivity
import com.smartcommute.R
import com.smartcommute.feature.linestatus.domain.model.UndergroundLine
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages status alert notifications.
 * Handles sending notifications with appropriate styling and channels.
 */
@Singleton
class StatusAlertsNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_ID_SILENT = "status_alerts_silent"
        const val CHANNEL_ID_DEFAULT = "status_alerts_default"
        const val CHANNEL_ID_URGENT = "status_alerts_urgent"

        private const val NOTIFICATION_ID_BASE = 1000

        // Intent extras for navigation
        const val EXTRA_NAVIGATE_TO = "navigate_to"
        const val EXTRA_LINE_ID = "line_id"
        const val NAVIGATE_TO_STATUS = "status"
        const val NAVIGATE_TO_LINE_DETAILS = "line_details"
    }

    /**
     * Sends a status notification for an alarm trigger.
     * Uses InboxStyle to display multiple tube lines.
     *
     * @param alarmId ID of the alarm that triggered
     * @param lines List of tube lines with their current status
     * @param isSilent If true, use silent channel; otherwise use audible based on status
     */
    @SuppressLint("MissingPermission")
    fun sendStatusNotification(
        alarmId: String,
        lines: List<TubeLineStatus>,
        isSilent: Boolean = false
    ) {
        val channelId = when {
            isSilent -> CHANNEL_ID_SILENT
            lines.any { it.hasDisruption } -> CHANNEL_ID_URGENT
            else -> CHANNEL_ID_DEFAULT
        }

        val title = if (lines.any { it.hasDisruption }) {
            "Service Disruptions Detected"
        } else {
            "Tube Status Update"
        }

        val inboxStyle = NotificationCompat.InboxStyle()
        lines.forEach { line ->
            val statusText = "${line.name}: ${line.statusText}"
            inboxStyle.addLine(statusText)
        }

        val summaryText = when {
            lines.any { it.hasDisruption } -> "${lines.count { it.hasDisruption }} line(s) affected"
            else -> "All lines running smoothly"
        }
        inboxStyle.setSummaryText(summaryText)

        // Create the appropriate PendingIntent based on number of lines
        val pendingIntent = createNotificationIntent(lines)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO: Replace with proper icon
            .setContentTitle(title)
            .setContentText("${lines.size} line(s) checked")
            .setStyle(inboxStyle)
            .setPriority(if (lines.any { it.hasDisruption }) {
                NotificationCompat.PRIORITY_HIGH
            } else {
                NotificationCompat.PRIORITY_DEFAULT
            })
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_ID_BASE + alarmId.hashCode(), notification)
    }

    /**
     * Creates the appropriate PendingIntent based on the number of lines.
     * - Single line: Navigate to line details screen
     * - Multiple lines: Navigate to tube status screen
     */
    private fun createNotificationIntent(lines: List<TubeLineStatus>): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

            if (lines.size == 1) {
                // Single line: navigate to line details
                putExtra(EXTRA_NAVIGATE_TO, NAVIGATE_TO_LINE_DETAILS)
                putExtra(EXTRA_LINE_ID, lines.first().lineId)
            } else {
                // Multiple lines: navigate to status screen
                putExtra(EXTRA_NAVIGATE_TO, NAVIGATE_TO_STATUS)
            }
        }

        return PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(), // Unique request code
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Sends an error notification when TfL API fails.
     *
     * @param alarmId ID of the alarm that triggered
     * @param lineNames List of tube line names that failed to fetch
     */
    @SuppressLint("MissingPermission")
    fun sendErrorNotification(
        alarmId: String,
        lineNames: List<String>
    ) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_DEFAULT)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // TODO: Replace with proper icon
            .setContentTitle("Status Check Failed")
            .setContentText("We tried to check the status for: ${lineNames.joinToString(", ")}")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("We tried to check the status for the following lines: ${lineNames.joinToString(", ")} but an error occurred. Please check your connection and try again.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_ID_BASE + alarmId.hashCode(), notification)
    }
}

/**
 * Data class representing a tube line's status for notification display.
 */
data class TubeLineStatus(
    val lineId: String,
    val name: String,
    val statusText: String,
    val hasDisruption: Boolean
) {
    companion object {
        fun fromLine(line: UndergroundLine): TubeLineStatus {
            val statusText = line.status.description.ifEmpty {
                when (line.status.type) {
                    com.smartcommute.feature.linestatus.domain.model.StatusType.GOOD_SERVICE -> "Good Service"
                    com.smartcommute.feature.linestatus.domain.model.StatusType.MINOR_DELAYS -> "Minor Delays"
                    com.smartcommute.feature.linestatus.domain.model.StatusType.MAJOR_DELAYS -> "Major Delays"
                    com.smartcommute.feature.linestatus.domain.model.StatusType.SEVERE_DELAYS -> "Severe Delays"
                    com.smartcommute.feature.linestatus.domain.model.StatusType.CLOSURE -> "Closure"
                    com.smartcommute.feature.linestatus.domain.model.StatusType.SERVICE_DISRUPTION -> "Service Disruption"
                }
            }

            // Good Service severity = 10, anything less is a disruption
            val hasDisruption = line.status.severity < 10

            return TubeLineStatus(
                lineId = line.id,
                name = line.name,
                statusText = statusText,
                hasDisruption = hasDisruption
            )
        }
    }
}
