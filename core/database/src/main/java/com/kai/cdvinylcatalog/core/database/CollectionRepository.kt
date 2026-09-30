package com.kai.cdvinylcatalog.core.database

import com.kai.cdvinylcatalog.core.database.dao.CollectionDao
import com.kai.cdvinylcatalog.core.database.entity.CollectionItemEntity
import com.kai.cdvinylcatalog.core.model.CollectionItem
import com.kai.cdvinylcatalog.core.model.Format
import com.kai.cdvinylcatalog.core.model.Release
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CollectionRepository @Inject constructor(
    private val dao: CollectionDao
) {

    /**
     * Все записи коллекции как доменные модели.
     */
    fun getAllItems(): Flow<List<CollectionItem>> =
        dao.getAllItems().map { entities ->
            entities.map { it.toDomain() }
        }

    /**
     * Проверяет, есть ли релиз в коллекции.
     */
    suspend fun isInCollection(releaseId: Long): Boolean =
        dao.getByReleaseId(releaseId) != null

    /**
     * Добавляет релиз в коллекцию.
     * Если запись с таким releaseId уже есть — увеличивает quantity.
     */
    suspend fun addToCollection(
        release: Release,
        format: Format,
        notes: String? = null
    ): Long {
        val existing = dao.getByReleaseId(release.id)

        return if (existing != null) {
            // Уже есть — увеличиваем количество
            val updated = existing.copy(quantity = existing.quantity + 1)
            dao.update(updated)
            updated.id
        } else {
            // Новая запись
            val entity = CollectionItemEntity(
                releaseId = release.id,
                title = release.title,
                artist = release.artist,
                year = release.year,
                barcode = release.barcode,
                coverImageUrl = release.coverImageUrl,
                label = release.label,
                rawFormat = release.rawFormat,
                country = release.country,
                releaseDate = release.releaseDate,
                catalogNumber = release.catalogNumber,
                releaseNotes = release.notes,
                format = format.name,
                quantity = 1,
                addedAt = System.currentTimeMillis(),
                userNotes = notes
            )
            dao.insert(entity)
        }
    }

    /**
     * Удаляет запись из коллекции.
     */
    suspend fun removeFromCollection(id: Long) {
        dao.deleteById(id)
    }

    /**
     * Получает запись из коллекции по её локальному (не release) id.
     */
    suspend fun getItemById(id: Long): CollectionItem? {
        return dao.getById(id)?.toDomain()
    }
}

/**
 * Маппинг Entity → Domain.
 */
private fun CollectionItemEntity.toDomain(): CollectionItem {
    return CollectionItem(
        id = id,
        release = Release(
            id = releaseId,
            title = title,
            artist = artist,
            year = year,
            barcode = barcode,
            coverImageUrl = coverImageUrl,
            label = label,
            rawFormat = rawFormat,
            country = country,
            releaseDate = releaseDate,
            catalogNumber = catalogNumber,
            notes = releaseNotes,
            tracklist = emptyList()
        ),
        format = try {
            Format.valueOf(format)
        } catch (e: IllegalArgumentException) {
            Format.UNKNOWN
        },
        quantity = quantity,
        addedAt = addedAt,
        notes = userNotes
    )
}