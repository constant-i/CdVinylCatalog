package com.kai.cdvinylcatalog.data.database

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.kai.cdvinylcatalog.data.database.dao.CollectionDao
import com.kai.cdvinylcatalog.data.database.entity.CollectionItemEntity
import com.kai.cdvinylcatalog.data.database.entity.CollectionItemWithCount
import com.kai.cdvinylcatalog.domain.catalog.Format
import com.kai.cdvinylcatalog.domain.catalog.Release
import com.kai.cdvinylcatalog.domain.collection.CollectionItem
import com.kai.cdvinylcatalog.domain.collection.SortOrder
import com.kai.cdvinylcatalog.domain.repository.CollectionMembership
import com.kai.cdvinylcatalog.domain.repository.CollectionReader
import com.kai.cdvinylcatalog.domain.repository.CollectionWriter
import com.kai.cdvinylcatalog.domain.result.AppError
import com.kai.cdvinylcatalog.domain.result.Outcome
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CollectionRepositoryImpl @Inject constructor(
    private val dao: CollectionDao
) : CollectionReader, CollectionMembership, CollectionWriter {

    override fun getItems(sortOrder: SortOrder): Flow<PagingData<CollectionItem>> {
        val pagingSourceFactory = {
            when (sortOrder) {
                SortOrder.ADDED_DESC -> dao.getPagingByAddedDesc()
                SortOrder.ADDED_ASC -> dao.getPagingByAddedAsc()
                SortOrder.ARTIST_ASC -> dao.getPagingByArtistAsc()
                SortOrder.YEAR_DESC -> dao.getPagingByYearDesc()
            }
        }

        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                // Две страницы сразу — чтобы первый экран заполнялся без паузы
                initialLoadSize = 40
            ),
            pagingSourceFactory = pagingSourceFactory
        ).flow.map { pagingData ->
            pagingData.map { entityWithCount ->
                entityWithCount.toDomain()
            }
        }
    }

    override fun observeItemById(id: Long): Flow<CollectionItem?> {
        return dao.observeById(id).map { it?.toDomain() }
    }

    override suspend fun getItemById(id: Long): Outcome<CollectionItem?> {
        return try {
            val entity = dao.getById(id)
            Outcome.Success(entity?.toDomain())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Outcome.Failure(AppError.Unknown)
        }
    }

    override suspend fun getOtherCopies(
        releaseId: Long,
        currentItemId: Long
    ): Outcome<List<CollectionItem>> {
        return try {
            Outcome.Success(
                dao.getAllByReleaseId(releaseId)
                    .filter { it.id != currentItemId }
                    .map { it.toDomain() }
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Outcome.Failure(AppError.Unknown)
        }
    }

    override suspend fun isInCollection(releaseId: Long): Outcome<Boolean> {
        return try {
            val exists = dao.getByReleaseId(releaseId) != null
            Outcome.Success(exists)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Outcome.Failure(AppError.Unknown)
        }
    }

    override fun observeCollectionReleaseIds(): Flow<Set<Long>> =
        dao.observeCollectionReleaseIds().map { it.toSet() }

    override suspend fun addToCollection(release: Release, format: Format, notes: String?): Outcome<Long> {
        return try {
            val entity = release.toEntity(format = format, notes = notes)
            val id = dao.insert(entity)
            Outcome.Success(id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Outcome.Failure(AppError.Unknown)
        }
    }

    override suspend fun addManualItem(
        title: String, artist: String, year: Int?, format: Format,
        label: String?, country: String?, barcode: String?, notes: String?,
        photoPaths: List<String>
    ): Outcome<Long> {
        return try {
            val entity = CollectionItemEntity(
                releaseId = null, title = title, artist = artist, year = year,
                barcode = barcode, coverImageUrl = photoPaths.firstOrNull(),
                imageUrls = photoPaths.joinToString(",").ifBlank { null },
                label = label, rawFormat = format.name, country = country,
                releaseDate = null, catalogNumber = null, releaseNotes = null,
                format = format.name, addedAt = System.currentTimeMillis(),
                userNotes = notes
            )
            val id = dao.insert(entity)
            Outcome.Success(id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Outcome.Failure(AppError.Unknown)
        }
    }

    override suspend fun updateItem(
        id: Long, title: String, artist: String, year: Int?, format: Format,
        label: String?, country: String?, barcode: String?, notes: String?,
        photoPaths: List<String>, preserveDiscogsImages: List<String>
    ): Outcome<Unit> {
        return try {
            val existing = dao.getById(id) ?: return Outcome.Failure(AppError.ResourceNotFound)
            val allImages = photoPaths + preserveDiscogsImages

            dao.update(
                existing.copy(
                    title = title, artist = artist, year = year,
                    format = format.name, rawFormat = format.name,
                    label = label, country = country, barcode = barcode,
                    userNotes = notes,
                    coverImageUrl = photoPaths.firstOrNull() ?: preserveDiscogsImages.firstOrNull() ?: existing.coverImageUrl,
                    imageUrls = if (allImages.isNotEmpty()) allImages.joinToString(",") else existing.imageUrls
                )
            )
            Outcome.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Outcome.Failure(AppError.Unknown)
        }
    }

    override suspend fun removeFromCollection(id: Long): Outcome<Unit> {
        return try {
            dao.deleteById(id)
            Outcome.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Outcome.Failure(AppError.Unknown)
        }
    }
}

private fun CollectionItemWithCount.toDomain(): CollectionItem {
    return this.item.toDomain().copy(copyCount = this.copyCount)
}

private fun CollectionItemEntity.toDomain(): CollectionItem {
    return CollectionItem(
        id = id,
        release = Release(
            id = releaseId ?: 0L, title = title, artist = artist, year = year,
            barcode = barcode, coverImageUrl = coverImageUrl,
            imageUrls = imageUrls?.split(",")?.filter { it.isNotBlank() } ?: emptyList(),
            label = label, rawFormat = rawFormat, country = country,
            releaseDate = releaseDate, catalogNumber = catalogNumber,
            notes = releaseNotes, tracklist = emptyList()
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

private fun Release.toEntity(format: Format, notes: String?): CollectionItemEntity {
    return CollectionItemEntity(
        releaseId = id, title = title, artist = artist, year = year,
        barcode = barcode, coverImageUrl = coverImageUrl,
        imageUrls = imageUrls.joinToString(",").ifBlank { null },
        label = label, rawFormat = rawFormat, country = country,
        releaseDate = releaseDate, catalogNumber = catalogNumber,
        releaseNotes = notes, format = format.name, addedAt = System.currentTimeMillis(),
        userNotes = notes
    )
}