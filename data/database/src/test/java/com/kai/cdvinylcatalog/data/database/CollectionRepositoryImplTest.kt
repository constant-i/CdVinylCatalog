package com.kai.cdvinylcatalog.data.database

import com.kai.cdvinylcatalog.data.database.dao.CollectionDao
import com.kai.cdvinylcatalog.data.database.entity.CollectionItemEntity
import com.kai.cdvinylcatalog.domain.catalog.Format
import com.kai.cdvinylcatalog.domain.catalog.Release
import com.kai.cdvinylcatalog.domain.result.AppError
import com.kai.cdvinylcatalog.domain.result.Outcome
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Маппинг между доменной моделью и строкой Room.
 *
 * Покрывает два бага, которые прожили в коде с первого коммита Clean
 * Architecture и были найдены только чтением глазами:
 *
 * 1. `releaseNotes` и `userNotes` заполнялись одним и тем же значением,
 *    из-за чего заметки Discogs терялись;
 * 2. `rawFormat` при редактировании затирался именем enum-а,
 *    из-за чего терялись детали издания («CD, Album» → «CD»).
 */
class CollectionRepositoryImplTest {

    private val dao: CollectionDao = mockk()
    private val repository = CollectionRepositoryImpl(dao)

    private val updateSlot = slot<CollectionItemEntity>()

    // --- Release -> Entity ---

    @Test
    fun `keeps discogs notes apart from the user note`() = runTest {
        val entity = capturedEntity()

        assertEquals("Заметки Discogs", entity.releaseNotes)
        assertEquals("подписана", entity.userNotes)
    }

    @Test
    fun `normalizes barcode before writing`() = runTest {
        val entity = capturedEntity()

        // Discogs печатает штрихкод с пробелами, сканер — только цифры
        assertEquals("727361129125", entity.barcode)
    }

    @Test
    fun `joins gallery urls into a single column`() = runTest {
        val entity = capturedEntity()

        assertEquals("https://img/a.jpg,https://img/b.jpg", entity.imageUrls)
    }

    @Test
    fun `writes null image column when the release has no pictures`() = runTest {
        val entity = capturedEntity(imageUrls = emptyList())

        assertNull(entity.imageUrls)
    }

    // --- Entity -> Release ---

    @Test
    fun `restores release notes to the release and user note to the item`() = runTest {
        coEvery { dao.getById(ID) } returns entity()

        val item = (repository.getItemById(ID) as Outcome.Success).data

        assertEquals("Заметки Discogs", item?.release?.notes)
        assertEquals("подписана", item?.notes)
    }

    @Test
    fun `manual item has no release id and falls back to zero`() = runTest {
        coEvery { dao.getById(ID) } returns entity(releaseId = null)

        val item = (repository.getItemById(ID) as Outcome.Success).data

        // 0L — признак «добавлено вручную»: связи с Discogs нет
        assertEquals(0L, item?.release?.id)
    }

    @Test
    fun `splits the stored image column back into a list`() = runTest {
        coEvery { dao.getById(ID) } returns entity(imageUrls = "a.jpg,b.jpg")

        val item = (repository.getItemById(ID) as Outcome.Success).data

        assertEquals(listOf("a.jpg", "b.jpg"), item?.release?.imageUrls)
    }

    @Test
    fun `unknown format in the database degrades to UNKNOWN`() = runTest {
        coEvery { dao.getById(ID) } returns entity(format = "8-TRACK")

        val item = (repository.getItemById(ID) as Outcome.Success).data

        assertEquals(Format.UNKNOWN, item?.format)
    }

    // --- updateItem ---

    @Test
    fun `editing keeps the discogs raw format`() = runTest {
        coEvery { dao.getById(ID) } returns entity()
        coEvery { dao.update(capture(updateSlot)) } returns Unit

        repository.updateItem(
            id = ID,
            title = "Once",
            artist = "Nightwish",
            year = 2004,
            format = Format.CD,
            label = null,
            country = null,
            barcode = null,
            notes = null,
            photoPaths = emptyList(),
            preserveDiscogsImages = emptyList()
        )

        val updated = updateSlot.captured
        assertEquals("CD, Album", updated.rawFormat)
        assertEquals(Format.CD.name, updated.format)
    }

    @Test
    fun `editing a missing item reports not found and writes nothing`() = runTest {
        coEvery { dao.getById(ID) } returns null

        val outcome = repository.updateItem(
            id = ID,
            title = "Once",
            artist = "Nightwish",
            year = null,
            format = Format.CD,
            label = null,
            country = null,
            barcode = null,
            notes = null,
            photoPaths = emptyList(),
            preserveDiscogsImages = emptyList()
        )

        assertEquals(Outcome.Failure(AppError.ResourceNotFound), outcome)
        coVerify(exactly = 0) { dao.update(any()) }
    }

    @Test
    fun `first user photo becomes the cover`() = runTest {
        coEvery { dao.getById(ID) } returns entity()
        coEvery { dao.update(capture(updateSlot)) } returns Unit

        repository.updateItem(
            id = ID,
            title = "Once",
            artist = "Nightwish",
            year = null,
            format = Format.CD,
            label = null,
            country = null,
            barcode = null,
            notes = null,
            photoPaths = listOf("photo1.jpg", "photo2.jpg"),
            preserveDiscogsImages = listOf("https://img/a.jpg")
        )

        val updated = updateSlot.captured
        assertEquals("photo1.jpg", updated.coverImageUrl)
        assertEquals("photo1.jpg,photo2.jpg,https://img/a.jpg", updated.imageUrls)
    }

    // --- findByBarcode ---

    @Test
    fun `barcode lookup normalizes the query`() = runTest {
        coEvery { dao.getByBarcode("727361129125") } returns emptyList()

        repository.findByBarcode(" 727-361 129125 ")

        coVerify { dao.getByBarcode("727361129125") }
    }

    @Test
    fun `barcode without digits never reaches the database`() = runTest {
        val outcome = repository.findByBarcode("нет цифр")

        assertEquals(Outcome.Success(emptyList<Any>()), outcome)
        coVerify(exactly = 0) { dao.getByBarcode(any()) }
    }

    // --- Ошибки ---

    @Test
    fun `database failure becomes a domain error instead of an exception`() = runTest {
        coEvery { dao.getById(ID) } throws IllegalStateException("disk full")

        val outcome = repository.getItemById(ID)

        assertEquals(Outcome.Failure(AppError.LocalStorage), outcome)
    }

    // --- Вспомогательное ---

    private suspend fun capturedEntity(
        imageUrls: List<String> = listOf("https://img/a.jpg", "https://img/b.jpg")
    ): CollectionItemEntity {
        val entitySlot = slot<CollectionItemEntity>()
        coEvery { dao.insert(capture(entitySlot)) } returns ID

        repository.addToCollection(
            release = release(imageUrls = imageUrls),
            format = Format.CD,
            notes = "подписана"
        )

        return entitySlot.captured
    }

    private fun entity(
        releaseId: Long? = 1L,
        imageUrls: String? = null,
        format: String = Format.CD.name
    ) = CollectionItemEntity(
        id = ID,
        releaseId = releaseId,
        title = "Once",
        artist = "Nightwish",
        year = 2004,
        barcode = "727361129125",
        coverImageUrl = null,
        imageUrls = imageUrls,
        label = "Nuclear Blast",
        rawFormat = "CD, Album",
        country = "Europe",
        releaseDate = null,
        catalogNumber = "NB 1291-2",
        releaseNotes = "Заметки Discogs",
        format = format,
        addedAt = 0L,
        userNotes = "подписана"
    )

    private fun release(
        imageUrls: List<String> = listOf("https://img/a.jpg", "https://img/b.jpg")
    ) = Release(
        id = 1L,
        title = "Once",
        artist = "Nightwish",
        year = 2004,
        barcode = "7 27361 12912 5",
        coverImageUrl = null,
        imageUrls = imageUrls,
        label = "Nuclear Blast",
        rawFormat = "CD, Album",
        country = "Europe",
        releaseDate = null,
        catalogNumber = "NB 1291-2",
        notes = "Заметки Discogs",
        tracklist = emptyList()
    )

    private companion object {
        const val ID = 42L
    }
}
