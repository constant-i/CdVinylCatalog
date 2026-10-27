package com.kai.cdvinylcatalog.data.database

import android.content.Context
import androidx.room.Room
import com.kai.cdvinylcatalog.data.database.dao.CollectionDao
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
        // Без fallbackToDestructiveMigration: при изменении схемы без миграции
        // приложение упадёт с явной ошибкой вместо тихого удаления коллекции.
        // Миграции обязательны, сравнивать с чем — schemas/ версии N.
        .build()

    @Provides
    @Singleton
    fun provideCollectionDao(database: CollectionDatabase): CollectionDao =
        database.collectionDao()
}