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
     * Возвращает все записи коллекции, отсортированные по дате добавления (новые сверху).
     *
     * Возвращает `Flow` — Room автоматически эмитит новый список при любом изменении
     * таблицы `collection_items`. Это позволяет UI обновляться в реальном времени
     * без ручного перезапроса.
     *
     * @return реактивный поток со списком записей
     */
    @Query("SELECT * FROM collection_items ORDER BY addedAt DESC")
    fun getAllItems(): Flow<List<CollectionItemEntity>>

    /**
     * Возвращает запись по **ID релиза из Discogs**.
     *
     * Обрати внимание: это `releaseId` из Discogs, а НЕ локальный `id` записи в Room.
     * Для поиска по локальному ID используй [getById].
     *
     * @param releaseId ID релиза в Discogs
     * @return запись или null, если не найдена
     */
    @Query("SELECT * FROM collection_items WHERE releaseId = :releaseId LIMIT 1")
    suspend fun getByReleaseId(releaseId: Long): CollectionItemEntity?

    /**
     * Возвращает **все** записи с указанным `releaseId`.
     *
     * Используется для проверки дубликатов: если пользователь добавил несколько
     * копий одного релиза, они будут возвращены все.
     *
     * @param releaseId ID релиза в Discogs
     * @return список записей (может быть пустым)
     */
    @Query("SELECT * FROM collection_items WHERE releaseId = :releaseId")
    suspend fun getAllByReleaseId(releaseId: Long): List<CollectionItemEntity>

    /**
     * Возвращает запись коллекции по её **локальному** ID.
     *
     * Обрати внимание: это ID записи в таблице `collection_items`,
     * а НЕ `releaseId` из Discogs. Для поиска по `releaseId`
     * используй [getByReleaseId].
     *
     * @param id локальный ID записи в Room
     * @return запись или null, если не найдена
     */
    @Query("SELECT * FROM collection_items WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): CollectionItemEntity?

    /**
     * Вставляет новую запись в коллекцию.
     *
     * При конфликте (совпадение по `id`) — **заменяет** существующую запись.
     * Это поведение задано стратегией [OnConflictStrategy.REPLACE].
     *
     * @param item запись для вставки
     * @return сгенерированный локальный ID записи
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CollectionItemEntity): Long

    /**
     * Обновляет существующую запись.
     *
     * @param item запись с обновлёнными полями
     */
    @Update
    suspend fun update(item: CollectionItemEntity)

    /**
     * Удаляет запись из коллекции.
     *
     * @param item запись для удаления
     */
    @Delete
    suspend fun delete(item: CollectionItemEntity)

    /**
     * Удаляет запись по её **локальному** ID.
     *
     * В отличие от [delete], принимает только ID, а не всю запись.
     * Удобно, когда есть только ID (например, из навигации).
     *
     * @param id локальный ID записи в Room
     */
    @Query("DELETE FROM collection_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * Возвращает `Flow` — Room автоматически эмитит новую запись при её изменении
     * по её **локальному** ID.
     *
     * Принимает ID записи в локальной БД.
     * @param id локальный ID записи в Room
     */
    @Query("SELECT * FROM collection_items WHERE id = :id LIMIT 1")
    fun observeById(id: Long): Flow<CollectionItemEntity?>
}