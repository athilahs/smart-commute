package com.smartcommute.feature.linestatus.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.smartcommute.feature.linestatus.data.local.entity.TubeLineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TubeLineDao {
    @Query("SELECT * FROM tube_lines ORDER BY name ASC")
    fun getAllLineStatuses(): Flow<List<TubeLineEntity>>

    @Query("SELECT * FROM tube_lines WHERE id = :lineId")
    suspend fun getLineStatusById(lineId: String): TubeLineEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lineStatuses: List<TubeLineEntity>)

    @Query("DELETE FROM tube_lines")
    suspend fun deleteAll()

    @Query("SELECT MAX(lastUpdated) FROM tube_lines")
    suspend fun getLastUpdateTime(): Long?
}
