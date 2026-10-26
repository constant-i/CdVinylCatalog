package com.kai.cdvinylcatalog.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kai.cdvinylcatalog.data.database.dao.CollectionDao
import com.kai.cdvinylcatalog.data.database.converter.FormatConverter
import com.kai.cdvinylcatalog.data.database.entity.CollectionItemEntity

@Database(
    entities = [CollectionItemEntity::class],
    version = 3,
    exportSchema = true
)
@TypeConverters(FormatConverter::class)
abstract class CollectionDatabase : RoomDatabase() {

    abstract fun collectionDao(): CollectionDao

    companion object {
        const val DATABASE_NAME = "cd_vinyl_catalog.db"
    }
}