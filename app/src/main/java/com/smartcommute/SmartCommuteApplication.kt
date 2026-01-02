package com.smartcommute

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SmartCommuteApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            // Silent channel for "Good Service" notifications
            val silentChannel = NotificationChannel(
                CHANNEL_ID_SILENT,
                "Silent Status Updates",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Silent background status updates when all lines have good service"
                enableVibration(false)
                setSound(null, null)
            }

            // Default channel for general status notifications
            val defaultChannel = NotificationChannel(
                CHANNEL_ID_DEFAULT,
                "Status Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General line status updates and informational alerts"
                enableVibration(false)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .build()
                )
            }

            // Urgent channel for disruption notifications
            val urgentChannel = NotificationChannel(
                CHANNEL_ID_URGENT,
                "Urgent Status Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical disruptions and urgent service changes"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 250, 250)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .build()
                )
                enableLights(true)
            }

            notificationManager.createNotificationChannels(
                listOf(silentChannel, defaultChannel, urgentChannel)
            )
        }
    }

    companion object {
        const val CHANNEL_ID_SILENT = "status_alerts_silent"
        const val CHANNEL_ID_DEFAULT = "status_alerts_default"
        const val CHANNEL_ID_URGENT = "status_alerts_urgent"
    }
}
