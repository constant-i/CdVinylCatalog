package com.kai.cdvinylcatalog.core.database

import com.kai.cdvinylcatalog.domain.repository.CollectionRepository
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
    abstract fun bindCollectionRepository(
        impl: CollectionRepositoryImpl
    ): CollectionRepository
}