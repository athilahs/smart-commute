package com.smartcommute.feature.linedetails.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.smartcommute.feature.linedetails.data.local.entity.ClosureEntity
import com.smartcommute.feature.linedetails.data.local.entity.CrowdingEntity
import com.smartcommute.feature.linedetails.data.local.entity.DisruptionEntity

@Dao
interface LineDetailsDao {

    // Disruptions
    @Query("SELECT * FROM disruptions WHERE lineId = :lineId")
    suspend fun getDisruptionsByLineId(lineId: String): List<DisruptionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDisruptions(disruptions: List<DisruptionEntity>)

    @Query("DELETE FROM disruptions WHERE lineId = :lineId")
    suspend fun deleteDisruptionsByLineId(lineId: String)

    @Query("DELETE FROM disruptions")
    suspend fun deleteAllDisruptions()

    // Closures
    @Query("SELECT * FROM closures WHERE lineId = :lineId")
    suspend fun getClosuresByLineId(lineId: String): List<ClosureEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClosures(closures: List<ClosureEntity>)

    @Query("DELETE FROM closures WHERE lineId = :lineId")
    suspend fun deleteClosuresByLineId(lineId: String)

    @Query("DELETE FROM closures")
    suspend fun deleteAllClosures()

    // Crowding
    @Query("SELECT * FROM crowding WHERE lineId = :lineId")
    suspend fun getCrowdingByLineId(lineId: String): CrowdingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrowding(crowding: CrowdingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrowdingBatch(crowding: List<CrowdingEntity>)

    @Query("DELETE FROM crowding WHERE lineId = :lineId")
    suspend fun deleteCrowdingByLineId(lineId: String)

    @Query("DELETE FROM crowding")
    suspend fun deleteAllCrowding()

    // Transaction: Delete all line details for a specific line
    @Transaction
    suspend fun deleteAllDetailsForLine(lineId: String) {
        deleteDisruptionsByLineId(lineId)
        deleteClosuresByLineId(lineId)
        deleteCrowdingByLineId(lineId)
    }
}
