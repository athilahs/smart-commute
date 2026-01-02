package com.smartcommute.feature.statusalerts.data.repository

import android.content.Context
import com.smartcommute.feature.statusalerts.data.local.StatusAlertDao
import com.smartcommute.feature.statusalerts.data.local.toDomain
import com.smartcommute.feature.statusalerts.data.local.toEntity
import com.smartcommute.feature.statusalerts.domain.model.StatusAlert
import com.smartcommute.feature.statusalerts.domain.util.AlarmScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StatusAlertsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: StatusAlertDao,
    private val alarmScheduler: AlarmScheduler
) : StatusAlertsRepository {

    override fun observeAllAlarms(): Flow<List<StatusAlert>> {
        return dao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getEnabledAlarms(): List<StatusAlert> {
        return dao.getEnabledAlarms().map { it.toDomain() }
    }

    override suspend fun getAlarmById(id: String): StatusAlert? {
        return dao.getById(id)?.toDomain()
    }

    override suspend fun createAlarm(alarm: StatusAlert): Result<StatusAlert> {
        return try {
            // Enforce 10-alarm limit
            val currentCount = dao.getCount()
            if (currentCount >= 10) {
                return Result.failure(
                    IllegalStateException("Maximum 10 alarms reached. Delete an alarm to create a new one.")
                )
            }

            // Validate alarm
            if (alarm.selectedTubeLines.isEmpty()) {
                return Result.failure(
                    IllegalArgumentException("At least one tube line must be selected")
                )
            }

            // Insert into database
            dao.insert(alarm.toEntity())

            // Schedule if enabled
            if (alarm.isEnabled) {
                alarmScheduler.scheduleAlarm(context, alarm)
            }

            Result.success(alarm)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateAlarm(alarm: StatusAlert): Result<StatusAlert> {
        return try {
            val existing = dao.getById(alarm.id)
                ?: return Result.failure(IllegalArgumentException("Alarm not found"))

            // Validate alarm
            if (alarm.selectedTubeLines.isEmpty()) {
                return Result.failure(
                    IllegalArgumentException("At least one tube line must be selected")
                )
            }

            // Update database
            dao.update(alarm.copy(lastModifiedAt = System.currentTimeMillis()).toEntity())

            // Reschedule if enabled
            if (alarm.isEnabled) {
                alarmScheduler.rescheduleAlarm(context, alarm)
            } else {
                alarmScheduler.cancelAlarm(context, alarm.id)
            }

            Result.success(alarm)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAlarm(id: String): Result<Unit> {
        return try {
            val alarm = dao.getById(id)
                ?: return Result.failure(IllegalArgumentException("Alarm not found"))

            // Cancel scheduled alarm
            alarmScheduler.cancelAlarm(context, id)

            // Delete from database
            dao.deleteById(id)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun enableAlarm(id: String): Result<Unit> {
        return try {
            val alarm = dao.getById(id)?.toDomain()
                ?: return Result.failure(IllegalArgumentException("Alarm not found"))

            // Update database
            dao.enable(id)

            // Schedule alarm
            alarmScheduler.scheduleAlarm(context, alarm.copy(isEnabled = true))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun disableAlarm(id: String): Result<Unit> {
        return try {
            val alarm = dao.getById(id)
                ?: return Result.failure(IllegalArgumentException("Alarm not found"))

            // Update database
            dao.disable(id)

            // Cancel scheduled alarm
            alarmScheduler.cancelAlarm(context, id)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAlarmCount(): Int {
        return dao.getCount()
    }
}
