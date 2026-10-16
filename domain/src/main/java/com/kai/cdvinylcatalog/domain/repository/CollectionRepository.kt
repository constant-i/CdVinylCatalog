package com.kai.cdvinylcatalog.domain.repository

import com.kai.cdvinylcatalog.domain.catalog.Format
import com.kai.cdvinylcatalog.domain.catalog.Release
import com.kai.cdvinylcatalog.domain.collection.CollectionItem
import com.kai.cdvinylcatalog.domain.result.Outcome
import kotlinx.coroutines.flow.Flow

/**
 * Репозиторий для работы с коллекцией пользователя.
 *
 * Domain-интерфейс. Реализация — в:data:database.
 */
interface CollectionRepository {

    /**
     * Наблюдает за всеми записями коллекции.
     * Реактивный поток — Room эмитит новое значение при любых изменениях.
     */
    fun observeAll(): Flow<List<CollectionItem>>

    /**
     * Наблюдает за одной записью коллекции.
     * Эмитит null, если запись удалена.
     */
    fun observeById(id: Long): Flow<CollectionItem?>

    /**
     * Проверяет, есть ли релиз в коллекции.
     */
    suspend fun isInCollection(releaseId: Long): Outcome<Boolean>

    /**
     * Добавляет релиз в коллекцию.
     * Если запись уже существует — увеличивает quantity (не создаёт дубликат).
     *
     * @return Outcome.Success с id созданной/обновлённой записи
     */
    suspend fun addToCollection(
        release: Release,
        notes: String? = null
    ): Outcome<Long>

    /**
     * Добавляет запись вручную (для дисков, которых нет в Discogs).
     */
    suspend fun addManualItem(
        title: String,
        artist: String,
        year: Int?,
        format: Format,
        label: String?,
        country: String?,
        barcode: String?,
        notes: String?,
        photoPaths: List<String> = emptyList()
    ): Outcome<Long>

    /**
     * Обновляет существующую запись.
     */
    suspend fun updateItem(
        id: Long,
        title: String,
        artist: String,
        year: Int?,
        format: Format,
        label: String?,
        country: String?,
        barcode: String?,
        notes: String?,
        photoPaths: List<String> = emptyList(),
        preserveDiscogsImages: List<String> = emptyList()
    ): Outcome<Unit>

    /**
     * Удаляет запись из коллекции.
     */
    suspend fun removeFromCollection(id: Long): Outcome<Unit>
}