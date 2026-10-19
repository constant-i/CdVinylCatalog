package com.kai.cdvinylcatalog.data.network

import com.kai.cdvinylcatalog.domain.repository.ReleaseRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindReleaseRepository(impl: DiscogsRepositoryImpl): ReleaseRepository
}