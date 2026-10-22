package com.kai.cdvinylcatalog.domain.usecase.collection

import com.kai.cdvinylcatalog.domain.catalog.Format
import com.kai.cdvinylcatalog.domain.catalog.Release
import com.kai.cdvinylcatalog.domain.repository.CollectionWriter
import com.kai.cdvinylcatalog.domain.result.Outcome
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Нормализация пользовательского ввода — обязанность домена, а не UI.
 *
 * Раньше `trim()` и разбор года жили в ViewModel, поэтому любой другой вход
 * в систему мог записать в базу мусор: пробелы, «19xx» вместо года.
 */
class AddManualItemUseCaseTest {

    private val writer = RecordingWriter()
    private val useCase = AddManualItemUseCase(writer)

    @Test
    fun `trims title and artist before writing`() = runTest {
        useCase(draft(title = "  Once  ", artist = "  Nightwish  "))

        assertEquals("Once", writer.lastTitle)
        assertEquals("Nightwish", writer.lastArtist)
    }

    @Test
    fun `converts year text to a number`() = runTest {
        useCase(draft(year = " 2004 "))

        assertEquals(2004, writer.lastYear)
    }

    @Test
    fun `malformed year is written as null`() = runTest {
        useCase(draft(year = "19xx"))

        assertNull(writer.lastYear)
    }

    @Test
    fun `blank optional fields become null`() = runTest {
        useCase(draft(label = "   ", country = "", notes = "  "))

        assertNull(writer.lastLabel)
        assertNull(writer.lastCountry)
        assertNull(writer.lastNotes)
    }

    @Test
    fun `optional fields are trimmed`() = runTest {
        useCase(draft(label = "  Nuclear Blast ", country = " Europe "))

        assertEquals("Nuclear Blast", writer.lastLabel)
        assertEquals("Europe", writer.lastCountry)
    }

    private fun draft(
        title: String = "Once",
        artist: String = "Nightwish",
        year: String = "2004",
        label: String? = null,
        country: String? = null,
        notes: String? = null
    ) = DraftCollectionItem(
        title = title,
        artist = artist,
        year = year,
        format = Format.CD,
        label = label,
        country = country,
        notes = notes
    )

    /** Запоминает, что реально дошло до хранилища. */
    private class RecordingWriter : CollectionWriter {
        var lastTitle: String? = null
        var lastArtist: String? = null
        var lastYear: Int? = null
        var lastLabel: String? = null
        var lastCountry: String? = null
        var lastNotes: String? = null

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
        ): Outcome<Long> {
            lastTitle = title
            lastArtist = artist
            lastYear = year
            lastLabel = label
            lastCountry = country
            lastNotes = notes
            return Outcome.Success(1L)
        }

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

        override suspend fun removeFromCollection(id: Long): Outcome<Unit> = error("not used")
    }
}
