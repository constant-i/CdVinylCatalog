package com.kai.cdvinylcatalog.core.model

import com.kai.cdvinylcatalog.domain.catalog.Release
import com.kai.cdvinylcatalog.domain.catalog.Track
import com.kai.cdvinylcatalog.domain.catalog.mergeWith
import org.junit.Assert.assertEquals
import org.junit.Test

class ReleaseMergeTest {

    private fun createRelease(
        id: Long = 1,
        title: String = "Encore",
        artist: String = "Sarah Brightman",
        year: Int? = 2007,
        label: String? = "Universal",
        country: String? = "Russia",
        imageUrls: List<String> = emptyList()
    ) = Release(
        id = id,
        title = title,
        artist = artist,
        year = year,
        barcode = null,
        coverImageUrl = null,
        imageUrls = imageUrls,
        label = label,
        rawFormat = "CD",
        country = country,
        releaseDate = null,
        catalogNumber = null,
        notes = null,
        tracklist = emptyList()
    )

    @Test
    fun `mergeWith returns this when discogs is null`() {
        val userRelease = createRelease(title = "Моё название")
        val result = userRelease.mergeWith(null)
        assertEquals(userRelease, result)
    }

    @Test
    fun `mergeWith prefers user title over discogs`() {
        val userRelease = createRelease(title = "Моё название")
        val discogs = createRelease(title = "Оригинал")
        val result = userRelease.mergeWith(discogs)
        assertEquals("Моё название", result.title)
    }

    @Test
    fun `mergeWith falls back to discogs when user title is blank`() {
        val userRelease = createRelease(title = "")
        val discogs = createRelease(title = "Оригинал")
        val result = userRelease.mergeWith(discogs)
        assertEquals("Оригинал", result.title)
    }

    @Test
    fun `mergeWith prefers user year when set`() {
        val userRelease = createRelease(year = 2020)
        val discogs = createRelease(year = 2007)
        val result = userRelease.mergeWith(discogs)
        assertEquals(2020, result.year)
    }

    @Test
    fun `mergeWith falls back to discogs year when null`() {
        val userRelease = createRelease(year = null)
        val discogs = createRelease(year = 2007)
        val result = userRelease.mergeWith(discogs)
        assertEquals(2007, result.year)
    }

    @Test
    fun `mergeWith prefers user images over discogs`() {
        val userRelease = createRelease(imageUrls = listOf("/local/photo.jpg"))
        val discogs = createRelease(imageUrls = listOf("https://discogs.com/cover.jpg"))
        val result = userRelease.mergeWith(discogs)
        assertEquals(listOf("/local/photo.jpg"), result.imageUrls)
    }

    @Test
    fun `mergeWith falls back to discogs images when user images empty`() {
        val userRelease = createRelease(imageUrls = emptyList())
        val discogs = createRelease(imageUrls = listOf("https://discogs.com/cover.jpg"))
        val result = userRelease.mergeWith(discogs)
        assertEquals(listOf("https://discogs.com/cover.jpg"), result.imageUrls)
    }

    @Test
    fun `mergeWith takes tracklist from discogs`() {
        val userRelease = createRelease()
        val discogs = createRelease().copy(
            tracklist = listOf(
                Track("1", "Song A", "3:45"),
                Track("2", "Song B", "4:12")
            )
        )
        val result = userRelease.mergeWith(discogs)
        assertEquals(2, result.tracklist.size)
        assertEquals("Song A", result.tracklist[0].title)
    }
}