package com.kai.cdvinylcatalog.data.database

import com.kai.cdvinylcatalog.domain.repository.CollectionMembership
import com.kai.cdvinylcatalog.domain.repository.CollectionReader
import com.kai.cdvinylcatalog.domain.repository.CollectionWriter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Один экземпляр `CollectionRepositoryImpl` отдаётся под тремя портами.
 *
 * Hilt сам не дедуплицирует `@Binds` для одного типа — три привязки создадут
 * три экземпляра. Чтобы база была общей, `@Singleton` вешается на реализации,
 * а здесь остаются только привязки.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DatabaseRepositoryModule {

    @Binds
    abstract fun bindCollectionReader(
        impl: CollectionRepositoryImpl
    ): CollectionReader

    @Binds
    abstract fun bindCollectionMembership(
        impl: CollectionRepositoryImpl
    ): CollectionMembership

    @Binds
    abstract fun bindCollectionWriter(
        impl: CollectionRepositoryImpl
    ): CollectionWriter
}
