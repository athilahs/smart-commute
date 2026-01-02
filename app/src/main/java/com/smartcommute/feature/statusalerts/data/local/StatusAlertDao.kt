package com.smartcommute.feature.statusalerts.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StatusAlertDao {
    /**
     * Observe all alarms, sorted by time (hour ASC, minute ASC)
     */
    @Query("SELECT * FROM status_alerts ORDER BY hour ASC, minute ASC")
    fun observeAll(): Flow<List<StatusAlertEntity>>

    /**
     * Get all enabled alarms (for alarm scheduling after reboot)
     */
    @Query("SELECT * FROM status_alerts WHERE isEnabled = 1 ORDER BY hour ASC, minute ASC")
    suspend fun getEnabledAlarms(): List<StatusAlertEntity>

    /**
     * Get a single alarm by ID
     */
    @Query("SELECT * FROM status_alerts WHERE id = :id")
    suspend fun getById(id: String): StatusAlertEntity?

    /**
     * Insert a new alarm
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(alarm: StatusAlertEntity)

    /**
     * Update an existing alarm
     */
    @Update
    suspend fun update(alarm: StatusAlertEntity)

    /**
     * Delete an alarm by ID
     */
    @Query("DELETE FROM status_alerts WHERE id = :id")
    suspend fun deleteById(id: String)

    /**
     * Get total count of alarms (for enforcing 10-alarm limit)
     */
    @Query("SELECT COUNT(*) FROM status_alerts")
    suspend fun getCount(): Int

    /**
     * Enable an alarm (set isEnabled = 1)
     */
    @Query("UPDATE status_alerts SET isEnabled = 1 WHERE id = :id")
    suspend fun enable(id: String)

    /**
     * Disable an alarm (set isEnabled = 0)
     */
    @Query("UPDATE status_alerts SET isEnabled = 0 WHERE id = :id")
    suspend fun disable(id: String)

    /**
     * Delete all alarms (for testing/debugging only - NOT exposed to user)
     */
    @Query("DELETE FROM status_alerts")
    suspend fun deleteAll()
}
