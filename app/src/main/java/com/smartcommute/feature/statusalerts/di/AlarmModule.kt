package com.smartcommute.feature.statusalerts.di

import android.app.AlarmManager
import android.content.Context
import com.smartcommute.feature.statusalerts.domain.util.AlarmScheduler
import com.smartcommute.feature.statusalerts.domain.util.AlarmSchedulerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AlarmModule {

    @Provides
    @Singleton
    fun provideAlarmManager(@ApplicationContext context: Context): AlarmManager {
        return context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    @Provides
    @Singleton
    fun provideAlarmScheduler(alarmManager: AlarmManager): AlarmScheduler {
        return AlarmSchedulerImpl(alarmManager)
    }
}
