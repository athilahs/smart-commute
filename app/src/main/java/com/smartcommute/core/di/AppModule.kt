package com.smartcommute.core.di

import com.smartcommute.feature.linestatus.data.LineStatusRepositoryImpl
import com.smartcommute.feature.linestatus.domain.repository.LineStatusRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindLineStatusRepository(
        impl: LineStatusRepositoryImpl
    ): LineStatusRepository
}
