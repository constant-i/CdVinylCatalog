package com.kai.cdvinylcatalog.domain.usecase.collection

import androidx.paging.PagingData
import com.kai.cdvinylcatalog.domain.catalog.Format
import com.kai.cdvinylcatalog.domain.catalog.Release
import com.kai.cdvinylcatalog.domain.collection.CollectionItem
import com.kai.cdvinylcatalog.domain.collection.SortOrder
import com.kai.cdvinylcatalog.domain.media.PhotoDestination
import com.kai.cdvinylcatalog.domain.media.PhotoStorage
import com.kai.cdvinylcatalog.domain.repository.CollectionReader
import com.kai.cdvinylcatalog.domain.repository.CollectionWriter
import com.kai.cdvinylcatalog.domain.result.AppError
import com.kai.cdvinylcatalog.domain.result.Outcome
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Удаление записи должно освобождать и файлы на диске.
 *
 * Раньше удалялась только строка в Room: фотографии оставались навсегда,
 * потому что репозиторий базы не знает о файловом хранилище, а хранилище —
 * о записях. Связать их может только доменный сценарий.
 */
class DeleteCollectionItemUseCaseTest {

    private val reader = FakeReader()
    private val writer = FakeWriter()
    private val photoStorage = RecordingPhotoStorage()
    private val useCase = DeleteCollectionItemUseCase(reader, writer, photoStorage)

    @Test
    fun `deletes photos that belong to the item`() = runTest {
        reader.item = itemWith(
            imageUrls = listOf("/files/photos/a.jpg", "/files/photos/b.jpg")
        )

        useCase(ITEM_ID)

        assertEquals(
            listOf("/files/photos/a.jpg", "/files/photos/b.jpg"),
            photoStorage.deleted
        )
    }

    @Test
    fun `deletes the primary cover when the item has no gallery`() = runTest {
        reader.item = itemWith(
            imageUrls = emptyList(),
            coverImageUrl = "/files/photos/cover.jpg"
        )

        useCase(ITEM_ID)

        assertEquals(listOf("/files/photos/cover.jpg"), photoStorage.deleted)
    }

    @Test
    fun `deletes each photo once even when it repeats`() = runTest {
        reader.item = itemWith(
            imageUrls = listOf("/files/photos/a.jpg", "/files/photos/a.jpg")
        )

        useCase(ITEM_ID)

        assertEquals(listOf("/files/photos/a.jpg"), photoStorage.deleted)
    }

    @Test
    fun `keeps discogs pictures - they are urls, not files`() = runTest {
        reader.item = itemWith(
            imageUrls = listOf("https://img.discogs.com/a.jpg"),
            coverImageUrl = "https://img.discogs.com/cover.jpg"
        )

        val outcome = useCase(ITEM_ID)

        assertTrue(outcome is Outcome.Success)
        assertEquals(emptyList<String>(), photoStorage.deleted)
    }

    @Test
    fun `does not touch files when the database delete fails`() = runTest {
        reader.item = itemWith(imageUrls = listOf("/files/photos/a.jpg"))
        writer.deleteOutcome = Outcome.Failure(AppError.Unknown)

        val outcome = useCase(ITEM_ID)

        assertTrue(outcome is Outcome.Failure)
        assertEquals(emptyList<String>(), photoStorage.deleted)
    }

    @Test
    fun `succeeds without writing when the item is already gone`() = runTest {
        reader.item = null

        val outcome = useCase(ITEM_ID)

        assertTrue(outcome is Outcome.Success)
        assertEquals(null, writer.deletedId)
        assertEquals(emptyList<String>(), photoStorage.deleted)
    }

    private fun itemWith(
        imageUrls: List<String>,
        coverImageUrl: String? = null
    ) = CollectionItem(
        id = ITEM_ID,
        release = Release(
            id = 1L,
            title = "Once",
            artist = "Nightwish",
            year = 2004,
            barcode = null,
            coverImageUrl = coverImageUrl,
            imageUrls = imageUrls,
            label = null,
            rawFormat = "CD, Album",
            country = null,
            releaseDate = null,
            catalogNumber = null,
            notes = null
        ),
        format = Format.CD,
        addedAt = 0L
    )

    private class FakeReader : CollectionReader {
        var item: CollectionItem? = null

        override fun getItems(sortOrder: SortOrder): Flow<PagingData<CollectionItem>> =
            flowOf(PagingData.empty())

        override fun observeItemById(id: Long): Flow<CollectionItem?> = flowOf(item)

        override suspend fun getItemById(id: Long): Outcome<CollectionItem?> =
            Outcome.Success(item)

        override suspend fun getOtherCopies(
            releaseId: Long,
            currentItemId: Long
        ): Outcome<List<CollectionItem>> = Outcome.Success(emptyList())

        override suspend fun findByBarcode(barcode: String): Outcome<List<CollectionItem>> =
            Outcome.Success(emptyList())
    }

    private class FakeWriter : CollectionWriter {
        var deleteOutcome: Outcome<Unit> = Outcome.Success(Unit)
        var deletedId: Long? = null

        override suspend fun addToCollection(
            release: Release,
            format: Format,
            notes: String?
        ): Outcome<Long> = error("not used")

        override suspend fun addManualItem(
            title: String,
            artist: String,
            year: Int?,
            format: Format,
            label: String?,
            country: String?,
            barcode: String?,
            notes: String?,
            photoPaths: List<String>
        ): Outcome<Long> = error("not used")

        override suspend fun updateItem(
            id: Long,
            title: String,
            artist: String,
            year: Int?,
            format: Format,
            label: String?,
            country: String?,
            barcode: String?,
            notes: String?,
            photoPaths: List<String>,
            preserveDiscogsImages: List<String>
        ): Outcome<Unit> = error("not used")

        override suspend fun removeFromCollection(id: Long): Outcome<Unit> {
            deletedId = id
            return deleteOutcome
        }
    }

    private class RecordingPhotoStorage : PhotoStorage {
        val deleted = mutableListOf<String>()

        override fun createDestination(): PhotoDestination = error("not used")

        override fun shareableUri(path: String): String = error("not used")

        override suspend fun import(sourceUri: String): PhotoDestination? {
            error("not used")
        }

        override fun isUsable(path: String): Boolean = error("not used")

        override fun delete(path: String) {
            deleted += path
        }
    }

    private companion object {
        const val ITEM_ID = 42L
    }
}
