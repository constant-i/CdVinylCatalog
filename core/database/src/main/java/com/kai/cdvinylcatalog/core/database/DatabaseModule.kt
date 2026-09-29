package com.kai.cdvinylcatalog.core.database

import android.content.Context
import androidx.room.Room
import com.kai.cdvinylcatalog.core.database.dao.CollectionDao
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
    fun provideDatabase(
        @ApplicationContext context: Context
    ): CollectionDatabase = Room.databaseBuilder(
        context,
        CollectionDatabase::class.java,
        CollectionDatabase.DATABASE_NAME
    )
        .fallbackToDestructiveMigration()  // <-- для MVP: при изменении схемы — удаляем базу
        .build()

    @Provides
    @Singleton
    fun provideCollectionDao(database: CollectionDatabase): CollectionDao =
        database.collectionDao()
}