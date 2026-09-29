package com.kai.cdvinylcatalog.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kai.cdvinylcatalog.core.database.entity.CollectionItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {

    /**
     * Все записи коллекции, отсортированные по дате добавления (новые сверху).
     * Flow — Room будет автоматически обновлять при изменениях.
     */
    @Query("SELECT * FROM collection_items ORDER BY addedAt DESC")
    fun getAllItems(): Flow<List<CollectionItemEntity>>

    /**
     * Поиск записи по ID релиза.
     * Возвращает null, если записи нет.
     */
    @Query("SELECT * FROM collection_items WHERE releaseId = :releaseId LIMIT 1")
    suspend fun getByReleaseId(releaseId: Long): CollectionItemEntity?

    /**
     * Поиск всех записей с одинаковым releaseId (дубликаты).
     */
    @Query("SELECT * FROM collection_items WHERE releaseId = :releaseId")
    suspend fun getAllByReleaseId(releaseId: Long): List<CollectionItemEntity>

    /**
     * Вставка записи. Если такая уже есть — заменяем.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CollectionItemEntity): Long

    /**
     * Обновление записи.
     */
    @Update
    suspend fun update(item: CollectionItemEntity)

    /**
     * Удаление записи.
     */
    @Delete
    suspend fun delete(item: CollectionItemEntity)

    /**
     * Удаление по ID.
     */
    @Query("DELETE FROM collection_items WHERE id = :id")
    suspend fun deleteById(id: Long)
}