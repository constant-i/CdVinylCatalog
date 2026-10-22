package com.kai.cdvinylcatalog.domain.usecase.collection

import com.kai.cdvinylcatalog.domain.catalog.Format
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Правила, которым должен соответствовать черновик записи.
 *
 * Раньше они жили в UI-контракте, поэтому проверить их можно было только
 * через экран. Теперь это домен — тест обычный и быстрый.
 */
class DraftCollectionItemTest {

    @Test
    fun `draft without title is not valid`() {
        assertFalse(draft(title = "").isValid)
        assertFalse(draft(title = "   ").isValid)
    }

    @Test
    fun `draft without artist is not valid`() {
        assertFalse(draft(artist = "").isValid)
        assertFalse(draft(artist = "   ").isValid)
    }

    @Test
    fun `draft with title and artist is valid`() {
        assertTrue(draft().isValid)
    }

    @Test
    fun `photos can be added below the limit`() {
        assertTrue(draft(photos = 0).canAddMorePhotos)
        assertTrue(draft(photos = DraftCollectionItem.MAX_PHOTOS - 1).canAddMorePhotos)
    }

    @Test
    fun `photos cannot be added at the limit`() {
        assertFalse(draft(photos = DraftCollectionItem.MAX_PHOTOS).canAddMorePhotos)
    }

    @Test
    fun `year is parsed from the text field`() {
        assertEquals(1991, draft(year = "1991").yearValue)
        assertEquals(1991, draft(year = "  1991  ").yearValue)
    }

    @Test
    fun `blank or malformed year becomes null`() {
        assertNull(draft(year = "").yearValue)
        assertNull(draft(year = "   ").yearValue)
        assertNull(draft(year = "19xx").yearValue)
    }

    @Test
    fun `non-positive year becomes null`() {
        // Год не может быть нулевым или отрицательным — считаем поле пустым
        assertNull(draft(year = "0").yearValue)
        assertNull(draft(year = "-5").yearValue)
    }

    private fun draft(
        title: String = "Once",
        artist: String = "Nightwish",
        year: String = "2004",
        photos: Int = 0
    ) = DraftCollectionItem(
        title = title,
        artist = artist,
        year = year,
        format = Format.CD,
        photoPaths = List(photos) { "/photos/$it.jpg" }
    )
}
