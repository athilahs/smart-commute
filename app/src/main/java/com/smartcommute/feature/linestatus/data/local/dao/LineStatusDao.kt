package com.smartcommute.feature.linestatus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.smartcommute.feature.linestatus.data.local.entity.LineStatusEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LineStatusDao {
    @Query("SELECT * FROM line_status ORDER BY name ASC")
    fun getAllLineStatuses(): Flow<List<LineStatusEntity>>

    @Query("SELECT * FROM line_status WHERE id = :lineId")
    suspend fun getLineStatusById(lineId: String): LineStatusEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lineStatuses: List<LineStatusEntity>)

    @Query("DELETE FROM line_status")
    suspend fun deleteAll()

    @Query("SELECT MAX(lastUpdated) FROM line_status")
    suspend fun getLastUpdateTime(): Long?
}
