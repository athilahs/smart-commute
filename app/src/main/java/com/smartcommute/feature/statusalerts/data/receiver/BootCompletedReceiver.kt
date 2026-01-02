package com.smartcommute.feature.statusalerts.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.smartcommute.feature.statusalerts.data.repository.StatusAlertsRepository
import com.smartcommute.feature.statusalerts.domain.util.AlarmScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * BroadcastReceiver that handles device boot completion.
 * Reschedules all enabled alarms after device reboot.
 */
@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: StatusAlertsRepository

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }

        // Reschedule all enabled alarms after boot
        scope.launch {
            try {
                val alarms = repository.observeAllAlarms().first()
                alarms.filter { it.isEnabled }.forEach { alarm ->
                    alarmScheduler.scheduleAlarm(context, alarm)
                }
            } catch (e: Exception) {
                // Log error but don't crash
                // TODO: Add proper logging
            }
        }
    }
}
