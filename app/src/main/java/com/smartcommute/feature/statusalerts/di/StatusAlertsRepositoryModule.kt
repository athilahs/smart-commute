package com.smartcommute.feature.statusalerts.di

import com.smartcommute.feature.statusalerts.data.repository.StatusAlertsRepository
import com.smartcommute.feature.statusalerts.data.repository.StatusAlertsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StatusAlertsRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindStatusAlertsRepository(
        impl: StatusAlertsRepositoryImpl
    ): StatusAlertsRepository
}
