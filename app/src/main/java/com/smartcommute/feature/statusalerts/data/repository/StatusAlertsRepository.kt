package com.smartcommute.feature.statusalerts.data.repository

import com.smartcommute.feature.statusalerts.domain.model.StatusAlert
import kotlinx.coroutines.flow.Flow

interface StatusAlertsRepository {
    /**
     * Observe all alarms (enabled + disabled), sorted by time (earliest first)
     */
    fun observeAllAlarms(): Flow<List<StatusAlert>>

    /**
     * Get all enabled alarms (for alarm scheduling)
     */
    suspend fun getEnabledAlarms(): List<StatusAlert>

    /**
     * Get a single alarm by ID
     */
    suspend fun getAlarmById(id: String): StatusAlert?

    /**
     * Create a new alarm
     * @throws IllegalStateException if alarm limit (10) is reached
     */
    suspend fun createAlarm(alarm: StatusAlert): Result<StatusAlert>

    /**
     * Update an existing alarm
     */
    suspend fun updateAlarm(alarm: StatusAlert): Result<StatusAlert>

    /**
     * Delete an alarm by ID
     */
    suspend fun deleteAlarm(id: String): Result<Unit>

    /**
     * Enable an alarm (sets isEnabled = true)
     */
    suspend fun enableAlarm(id: String): Result<Unit>

    /**
     * Disable an alarm (sets isEnabled = false)
     */
    suspend fun disableAlarm(id: String): Result<Unit>

    /**
     * Get current alarm count (for enforcing 10-alarm limit)
     */
    suspend fun getAlarmCount(): Int
}
