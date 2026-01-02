package com.smartcommute.feature.linestatus.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.smartcommute.feature.linedetails.data.local.dao.LineDetailsDao
import com.smartcommute.feature.linedetails.data.local.entity.ClosureEntity
import com.smartcommute.feature.linedetails.data.local.entity.CrowdingEntity
import com.smartcommute.feature.linedetails.data.local.entity.DisruptionEntity
import com.smartcommute.feature.linestatus.data.local.dao.TubeLineDao
import com.smartcommute.feature.linestatus.data.local.entity.TubeLineEntity
import com.smartcommute.feature.statusalerts.data.local.StatusAlertDao
import com.smartcommute.feature.statusalerts.data.local.StatusAlertEntity

@Database(
    entities = [
        TubeLineEntity::class,
        DisruptionEntity::class,
        ClosureEntity::class,
        CrowdingEntity::class,
        StatusAlertEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class LineStatusDatabase : RoomDatabase() {
    abstract fun tubeLineDao(): TubeLineDao
    abstract fun lineDetailsDao(): LineDetailsDao
    abstract fun statusAlertDao(): StatusAlertDao

    companion object {
        const val DATABASE_NAME = "line_status_db"

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create status_alerts table for alarm configurations
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS status_alerts (
                        id TEXT PRIMARY KEY NOT NULL,
                        hour INTEGER NOT NULL,
                        minute INTEGER NOT NULL,
                        selectedDaysOfWeek TEXT NOT NULL,
                        selectedTubeLines TEXT NOT NULL,
                        isEnabled INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        lastModifiedAt INTEGER NOT NULL
                    )
                """.trimIndent())

                // Create indexes for performance
                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS idx_status_alerts_enabled
                    ON status_alerts(isEnabled)
                """.trimIndent())

                database.execSQL("""
                    CREATE INDEX IF NOT EXISTS idx_status_alerts_created_at
                    ON status_alerts(createdAt)
                """.trimIndent())
            }
        }
    }
}
