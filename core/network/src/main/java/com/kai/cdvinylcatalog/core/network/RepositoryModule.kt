package com.kai.cdvinylcatalog.core.network

import com.kai.cdvinylcatalog.domain.repository.ReleaseRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindReleaseRepository(impl: DiscogsRepository): ReleaseRepository
}