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
    fun getItems(sortOrder: SortOrder): Flow<List<CollectionItem>> {
        val flow = when (sortOrder) {
            SortOrder.ADDED_DESC -> dao.getAllByAddedDesc()
            SortOrder.ADDED_ASC -> dao.getAllByAddedAsc()
            SortOrder.ARTIST_ASC -> dao.getAllByArtistAsc()
            SortOrder.YEAR_DESC -> dao.getAllByYearDesc()
        }
        return flow.map { entities ->
            // Считаем количество копий для каждого releaseId
            val countByReleaseId = entities
                .filter { it.releaseId != null }
                .groupingBy { it.releaseId!! }
                .eachCount()

            entities.map { entity ->
                entity.toDomain().copy(
                    copyCount = if (entity.releaseId != null) {
                        countByReleaseId[entity.releaseId] ?: 1
                    } else {
                        1
                    }
                )
            }
        }
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
        val entity = CollectionItemEntity(
            releaseId = release.id,
            title = release.title,
            artist = release.artist,
            year = release.year,
            barcode = release.barcode,
            coverImageUrl = release.coverImageUrl,
            imageUrls = release.imageUrls.joinToString(",").ifBlank { null },
            label = release.label,
            rawFormat = release.rawFormat,
            country = release.country,
            releaseDate = release.releaseDate,
            catalogNumber = release.catalogNumber,
            releaseNotes = release.notes,
            format = format.name,
            addedAt = System.currentTimeMillis(),
            userNotes = notes
        )
        return dao.insert(entity)
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

    /**
     * Добавляет созданную в ручную запись в коллекцию (для позиций, которых нет в Discogs)
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
    ): Long {
        val entity = CollectionItemEntity(
            releaseId = null,
            title = title,
            artist = artist,
            year = year,
            barcode = barcode,
            coverImageUrl = photoPaths.firstOrNull(),
            imageUrls = photoPaths.joinToString(",").ifBlank { null },
            label = label,
            rawFormat = format.name,
            country = country,
            releaseDate = null,
            catalogNumber = null,
            releaseNotes = null,
            format = format.name,
            addedAt = System.currentTimeMillis(),
            userNotes = notes
        )
        return dao.insert(entity)
    }

    /**
     * Редактирование созданной в ручную записи в коллекции
     */
    suspend fun updateManualItem(
        id: Long,
        title: String,
        artist: String,
        year: Int?,
        format: Format,
        label: String?,
        country: String?,
        barcode: String?,
        notes: String?,
        photoPaths: List<String> = emptyList()
    ) {
        val existing = dao.getById(id) ?: return
        dao.update(
            existing.copy(
                title = title,
                artist = artist,
                year = year,
                format = format.name,
                rawFormat = format.name,
                label = label,
                country = country,
                barcode = barcode,
                userNotes = notes,
                coverImageUrl = if (photoPaths.isNotEmpty()) {
                    photoPaths.firstOrNull()
                } else {
                    null
                },
                imageUrls = if (photoPaths.isNotEmpty()) {
                    photoPaths.joinToString(",")
                } else {
                    existing.imageUrls
                }
            )
        )
    }

    /**
     *  Для подписки на запись в коллекции созданной в ручную
     */
    fun observeItemById(id: Long): Flow<CollectionItem?> {
        return dao.observeById(id).map { it?.toDomain() }
    }
}

/**
 * Маппинг Entity → Domain.
 *
 * В ручных записях releaseId = 0
 */
private fun CollectionItemEntity.toDomain(): CollectionItem {
    return CollectionItem(
        id = id,
        release = Release(
            id = releaseId ?: 0L,
            title = title,
            artist = artist,
            year = year,
            barcode = barcode,
            coverImageUrl = coverImageUrl,
            imageUrls = imageUrls?.split(",")?.filter { it.isNotBlank() } ?: emptyList(),
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
        addedAt = addedAt,
        notes = userNotes
    )
}