package com.smartcommute.core.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.smartcommute.feature.linestatus.data.local.LineStatusDatabase
import com.smartcommute.feature.linestatus.data.local.dao.TubeLineDao
import com.smartcommute.feature.linedetails.data.local.dao.LineDetailsDao
import com.smartcommute.feature.statusalerts.data.local.StatusAlertDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Step 1: Create new tube_lines table with additional columns
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS tube_lines (
                    id TEXT PRIMARY KEY NOT NULL,
                    name TEXT NOT NULL,
                    modeName TEXT NOT NULL,
                    statusType TEXT NOT NULL,
                    statusDescription TEXT NOT NULL,
                    statusSeverity INTEGER NOT NULL,
                    brandColor TEXT NOT NULL DEFAULT '#000000',
                    lastUpdated INTEGER NOT NULL,
                    cacheExpiry INTEGER NOT NULL DEFAULT 0
                )
            """.trimIndent())

            // Step 2: Copy data from line_status to tube_lines
            db.execSQL("""
                INSERT INTO tube_lines (id, name, modeName, statusType, statusDescription, statusSeverity, lastUpdated, cacheExpiry)
                SELECT id, name, modeName, statusType, statusDescription, statusSeverity, lastUpdated, lastUpdated + 600000
                FROM line_status
            """.trimIndent())

            // Step 3: Drop old line_status table
            db.execSQL("DROP TABLE IF EXISTS line_status")

            // Step 4: Create new tables for line details
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS disruptions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    lineId TEXT NOT NULL,
                    category TEXT NOT NULL,
                    type TEXT NOT NULL,
                    categoryDescription TEXT NOT NULL,
                    description TEXT NOT NULL,
                    closureText TEXT,
                    affectedStops TEXT NOT NULL,
                    createdDate INTEGER NOT NULL,
                    startDate INTEGER,
                    endDate INTEGER,
                    severity INTEGER NOT NULL,
                    FOREIGN KEY(lineId) REFERENCES tube_lines(id) ON DELETE CASCADE
                )
            """.trimIndent())

            db.execSQL("CREATE INDEX IF NOT EXISTS index_disruptions_lineId ON disruptions(lineId)")

            db.execSQL("""
                CREATE TABLE IF NOT EXISTS closures (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    lineId TEXT NOT NULL,
                    description TEXT NOT NULL,
                    reason TEXT NOT NULL,
                    affectedStations TEXT NOT NULL,
                    affectedSegment TEXT,
                    startDate INTEGER NOT NULL,
                    endDate INTEGER NOT NULL,
                    alternativeRoute TEXT,
                    replacementBus INTEGER NOT NULL,
                    FOREIGN KEY(lineId) REFERENCES tube_lines(id) ON DELETE CASCADE
                )
            """.trimIndent())

            db.execSQL("CREATE INDEX IF NOT EXISTS index_closures_lineId ON closures(lineId)")

            db.execSQL("""
                CREATE TABLE IF NOT EXISTS crowding (
                    lineId TEXT PRIMARY KEY NOT NULL,
                    level TEXT NOT NULL,
                    levelCode INTEGER NOT NULL,
                    measurementTime INTEGER NOT NULL,
                    dataSource TEXT NOT NULL,
                    notes TEXT,
                    FOREIGN KEY(lineId) REFERENCES tube_lines(id) ON DELETE CASCADE
                )
            """.trimIndent())

            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_crowding_lineId ON crowding(lineId)")
        }
    }

    @Provides
    @Singleton
    fun provideLineStatusDatabase(@ApplicationContext context: Context): LineStatusDatabase {
        return Room.databaseBuilder(
            context,
            LineStatusDatabase::class.java,
            LineStatusDatabase.DATABASE_NAME
        )
            .addMigrations(MIGRATION_1_2, LineStatusDatabase.MIGRATION_2_3)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideTubeLineDao(database: LineStatusDatabase): TubeLineDao {
        return database.tubeLineDao()
    }

    @Provides
    @Singleton
    fun provideLineDetailsDao(database: LineStatusDatabase): LineDetailsDao {
        return database.lineDetailsDao()
    }

    @Provides
    @Singleton
    fun provideStatusAlertDao(database: LineStatusDatabase): StatusAlertDao {
        return database.statusAlertDao()
    }
}
