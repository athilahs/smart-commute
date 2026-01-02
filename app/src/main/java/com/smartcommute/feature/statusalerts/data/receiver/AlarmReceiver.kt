package com.smartcommute.feature.statusalerts.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.smartcommute.core.network.NetworkResult
import com.smartcommute.feature.linestatus.domain.repository.LineStatusRepository
import com.smartcommute.feature.statusalerts.data.repository.StatusAlertsRepository
import com.smartcommute.feature.statusalerts.domain.util.AlarmSchedulerImpl
import com.smartcommute.feature.statusalerts.notification.StatusAlertsNotificationManager
import com.smartcommute.feature.statusalerts.notification.TubeLineStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
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
    lateinit var notificationManager: StatusAlertsNotificationManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AlarmSchedulerImpl.ACTION_ALARM_TRIGGERED) {
            return
        }

        val alarmId = intent.getStringExtra(AlarmSchedulerImpl.EXTRA_ALARM_ID) ?: return
        val selectedTubeLinesString = intent.getStringExtra(AlarmSchedulerImpl.EXTRA_SELECTED_TUBE_LINES) ?: return
        val selectedTubeLines = selectedTubeLinesString.split(",").toSet()

        scope.launch {
            try {
                // Fetch current line statuses from TfL API
                when (val result = lineStatusRepository.getLineStatuses().first()) {
                    is NetworkResult.Success -> {
                        // Filter to only the selected tube lines
                        val relevantLines = result.data.filter { line ->
                            selectedTubeLines.contains(line.id)
                        }

                        if (relevantLines.isEmpty()) {
                            // No matching lines found - send error notification
                            val lineNames = selectedTubeLines.toList()
                            notificationManager.sendErrorNotification(alarmId, lineNames)
                            return@launch
                        }

                        // Convert to TubeLineStatus for notification
                        val lineStatuses = relevantLines.map { line ->
                            TubeLineStatus.fromLine(line)
                        }

                        // Determine if notification should be silent or audible
                        // If ANY line has disruptions, use audible notification
                        // If ALL lines are Good Service, use silent notification
                        val hasAnyDisruption = lineStatuses.any { it.hasDisruption }
                        val isSilent = !hasAnyDisruption

                        // Send notification
                        notificationManager.sendStatusNotification(
                            alarmId = alarmId,
                            lines = lineStatuses,
                            isSilent = isSilent
                        )

                        // If this is a recurring alarm, reschedule it for the next occurrence
                        // (This will be handled in Phase 6 - User Story 4)
                        // For now, one-time alarms will auto-disable in Phase 7 - User Story 5
                    }

                    is NetworkResult.Error -> {
                        // API error - send error notification
                        val lineNames = selectedTubeLines.toList()
                        notificationManager.sendErrorNotification(alarmId, lineNames)
                    }

                    is NetworkResult.Loading -> {
                        // This shouldn't happen with .first(), but handle it just in case
                        // Do nothing, wait for actual result
                    }
                }
            } catch (e: Exception) {
                // Error fetching data - send error notification
                val lineNames = selectedTubeLines.toList()
                notificationManager.sendErrorNotification(alarmId, lineNames)
            }
        }
    }
}
