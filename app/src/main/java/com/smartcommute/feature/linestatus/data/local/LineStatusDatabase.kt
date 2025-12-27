package com.smartcommute.feature.linestatus.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.smartcommute.feature.linestatus.data.local.dao.LineStatusDao
import com.smartcommute.feature.linestatus.data.local.entity.LineStatusEntity

@Database(
    entities = [LineStatusEntity::class],
    version = 1,
    exportSchema = false
)
abstract class LineStatusDatabase : RoomDatabase() {
    abstract fun lineStatusDao(): LineStatusDao

    companion object {
        const val DATABASE_NAME = "line_status_db"
    }
}
