package com.smartcommute.feature.linestatus.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.smartcommute.feature.linedetails.data.local.dao.LineDetailsDao
import com.smartcommute.feature.linedetails.data.local.entity.ClosureEntity
import com.smartcommute.feature.linedetails.data.local.entity.CrowdingEntity
import com.smartcommute.feature.linedetails.data.local.entity.DisruptionEntity
import com.smartcommute.feature.linestatus.data.local.dao.TubeLineDao
import com.smartcommute.feature.linestatus.data.local.entity.TubeLineEntity

@Database(
    entities = [
        TubeLineEntity::class,
        DisruptionEntity::class,
        ClosureEntity::class,
        CrowdingEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class LineStatusDatabase : RoomDatabase() {
    abstract fun tubeLineDao(): TubeLineDao
    abstract fun lineDetailsDao(): LineDetailsDao

    companion object {
        const val DATABASE_NAME = "line_status_db"
    }
}
