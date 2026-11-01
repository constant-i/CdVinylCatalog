package com.kai.cdvinylcatalog.data.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kai.cdvinylcatalog.data.database.dao.CollectionDao
import com.kai.cdvinylcatalog.data.database.converter.FormatConverter
import com.kai.cdvinylcatalog.data.database.entity.CollectionItemEntity

@Database(
    entities = [CollectionItemEntity::class],
    version = 4,
    exportSchema = true,
    // 3 -> 4: индексы по releaseId и barcode. Room выведет CREATE INDEX
    // из разницы схем, для этого schemas/3.json лежит в репозитории.
    autoMigrations = [AutoMigration(from = 3, to = 4)]
)
@TypeConverters(FormatConverter::class)
abstract class CollectionDatabase : RoomDatabase() {

    abstract fun collectionDao(): CollectionDao

    companion object {
        const val DATABASE_NAME = "cd_vinyl_catalog.db"
    }
}
