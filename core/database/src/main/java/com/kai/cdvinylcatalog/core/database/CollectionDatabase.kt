package com.kai.cdvinylcatalog.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kai.cdvinylcatalog.core.database.converter.FormatConverter
import com.kai.cdvinylcatalog.core.database.dao.CollectionDao
import com.kai.cdvinylcatalog.core.database.entity.CollectionItemEntity

@Database(
    entities = [CollectionItemEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(FormatConverter::class)
abstract class CollectionDatabase : RoomDatabase() {

    abstract fun collectionDao(): CollectionDao

    companion object {
        const val DATABASE_NAME = "cd_vinyl_catalog.db"
    }
}