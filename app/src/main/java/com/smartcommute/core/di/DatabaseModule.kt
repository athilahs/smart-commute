package com.smartcommute.core.di

import android.content.Context
import androidx.room.Room
import com.smartcommute.feature.linestatus.data.local.LineStatusDatabase
import com.smartcommute.feature.linestatus.data.local.dao.LineStatusDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideLineStatusDatabase(@ApplicationContext context: Context): LineStatusDatabase {
        return Room.databaseBuilder(
            context,
            LineStatusDatabase::class.java,
            LineStatusDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideLineStatusDao(database: LineStatusDatabase): LineStatusDao {
        return database.lineStatusDao()
    }
}
