package com.smartcommute.core.di

import com.smartcommute.feature.linedetails.data.repository.LineDetailsRepositoryImpl
import com.smartcommute.feature.linedetails.domain.repository.LineDetailsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LineDetailsModule {

    @Binds
    @Singleton
    abstract fun bindLineDetailsRepository(
        impl: LineDetailsRepositoryImpl
    ): LineDetailsRepository
}
