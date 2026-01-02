package com.smartcommute.feature.statusalerts.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.smartcommute.core.network.NetworkResult
import com.smartcommute.feature.linestatus.domain.repository.LineStatusRepository
import com.smartcommute.feature.statusalerts.data.repository.StatusAlertsRepository
import com.smartcommute.feature.statusalerts.domain.util.AlarmScheduler
import com.smartcommute.feature.statusalerts.domain.util.AlarmSchedulerImpl
import com.smartcommute.feature.statusalerts.notification.StatusAlertsNotificationManager
import com.smartcommute.feature.statusalerts.notification.TubeLineStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * BroadcastReceiver that handles alarm triggers for Status Alerts.
 * Fetches TfL API status for selected tube lines and sends notifications.
 */
@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var lineStatusRepository: LineStatusRepository

    @Inject
    lateinit var statusAlertsRepository: StatusAlertsRepository

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var notificationManager: StatusAlertsNotificationManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "AlarmReceiver.onReceive() called - Action: ${intent.action}")

        if (intent.action != AlarmSchedulerImpl.ACTION_ALARM_TRIGGERED) {
            Log.w(TAG, "Received intent with wrong action: ${intent.action}")
            return
        }

        val alarmId = intent.getStringExtra(AlarmSchedulerImpl.EXTRA_ALARM_ID)
        if (alarmId == null) {
            Log.e(TAG, "No alarm ID in intent")
            return
        }

        val selectedTubeLinesString = intent.getStringExtra(AlarmSchedulerImpl.EXTRA_SELECTED_TUBE_LINES)
        if (selectedTubeLinesString == null) {
            Log.e(TAG, "No tube lines in intent for alarm $alarmId")
            return
        }

        val selectedTubeLines = selectedTubeLinesString.split(",").toSet()
        Log.d(TAG, "Alarm $alarmId triggered for lines: $selectedTubeLines")

        scope.launch {
            try {
                Log.d(TAG, "Fetching line statuses from TfL API...")
                // Fetch current line statuses from TfL API
                // Skip Loading state and wait for Success or Error
                val result = lineStatusRepository.getLineStatuses()
                    .firstOrNull { it !is NetworkResult.Loading }

                if (result == null) {
                    Log.e(TAG, "No result from API (timed out or no data)")
                    val lineNames = selectedTubeLines.toList()
                    notificationManager.sendErrorNotification(alarmId, lineNames)
                    return@launch
                }

                when (result) {
                    is NetworkResult.Success -> {
                        Log.d(TAG, "Successfully fetched ${result.data.size} lines from TfL API")
                        // Filter to only the selected tube lines
                        val relevantLines = result.data.filter { line ->
                            selectedTubeLines.contains(line.id)
                        }

                        if (relevantLines.isEmpty()) {
                            Log.w(TAG, "No matching lines found for alarm $alarmId")
                            // No matching lines found - send error notification
                            val lineNames = selectedTubeLines.toList()
                            notificationManager.sendErrorNotification(alarmId, lineNames)
                            return@launch
                        }

                        Log.d(TAG, "Found ${relevantLines.size} relevant lines")
                        // Convert to TubeLineStatus for notification
                        val lineStatuses = relevantLines.map { line ->
                            TubeLineStatus.fromLine(line)
                        }

                        // Determine if notification should be silent or audible
                        // If ANY line has disruptions, use audible notification
                        // If ALL lines are Good Service, use silent notification
                        val hasAnyDisruption = lineStatuses.any { it.hasDisruption }
                        val isSilent = !hasAnyDisruption
                        Log.d(TAG, "Disruption status: hasAnyDisruption=$hasAnyDisruption, isSilent=$isSilent")

                        // Send notification
                        notificationManager.sendStatusNotification(
                            alarmId = alarmId,
                            lines = lineStatuses,
                            isSilent = isSilent
                        )
                        Log.d(TAG, "Notification sent for alarm $alarmId")

                        // Reschedule recurring alarms for next occurrence
                        val alarm = statusAlertsRepository.getAlarmById(alarmId)
                        if (alarm != null && alarm.isRecurring && alarm.isEnabled) {
                            Log.d(TAG, "Rescheduling recurring alarm $alarmId")
                            alarmScheduler.scheduleAlarm(context, alarm)
                        } else {
                            Log.d(TAG, "Alarm $alarmId is one-time or not enabled, not rescheduling")
                        }
                    }

                    is NetworkResult.Error -> {
                        Log.e(TAG, "API error: ${result.message}")
                        // API error - send error notification
                        val lineNames = selectedTubeLines.toList()
                        notificationManager.sendErrorNotification(alarmId, lineNames)
                    }

                    is NetworkResult.Loading -> {
                        // This should never happen since we filtered it out
                        Log.e(TAG, "Unexpected Loading state after filter")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in alarm receiver: ${e.message}", e)
                // Error fetching data - send error notification
                val lineNames = selectedTubeLines.toList()
                notificationManager.sendErrorNotification(alarmId, lineNames)
            }
        }
    }

    companion object {
        private const val TAG = "AlarmReceiver"
    }
}
