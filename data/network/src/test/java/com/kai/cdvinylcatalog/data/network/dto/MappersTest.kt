package com.kai.cdvinylcatalog.data.network.dto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Штрихкод — основа офлайн-проверки «есть ли диск в коллекции».
 *
 * Discogs отдаёт его только в блоке `identifiers` с `type = "Barcode"`,
 * причём напечатанным с пробелами. Сканер отдаёт непрерывные цифры,
 * поэтому маппер обязан нормализовать значение — иначе сверка не найдёт совпадения.
 */
class MappersTest {

    @Test
    fun `details mapper reads barcode from identifiers`() {
        val dto = detailsDto(
            identifiers = listOf(
                IdentifierDto(type = "Barcode", value = "5051099709942")
            )
        )

        assertEquals("5051099709942", dto.toDomain().barcode)
    }

    @Test
    fun `details mapper strips spaces and dashes from barcode`() {
        val dto = detailsDto(
            identifiers = listOf(
                IdentifierDto(type = "Barcode", value = "5 051099-709942")
            )
        )

        assertEquals("5051099709942", dto.toDomain().barcode)
    }

    @Test
    fun `details mapper ignores non-barcode identifiers`() {
        val dto = detailsDto(
            identifiers = listOf(
                IdentifierDto(type = "Matrix / Runout", value = "SONOPRESS 123"),
                IdentifierDto(type = "Barcode", value = "602547378781"),
                IdentifierDto(type = "Rights Society", value = "GEMA")
            )
        )

        assertEquals("602547378781", dto.toDomain().barcode)
    }

    @Test
    fun `details mapper matches barcode type case-insensitively`() {
        val dto = detailsDto(
            identifiers = listOf(
                IdentifierDto(type = "barcode", value = "602547378781")
            )
        )

        assertEquals("602547378781", dto.toDomain().barcode)
    }

    @Test
    fun `details mapper returns null when identifiers are missing`() {
        assertNull(detailsDto(identifiers = null).toDomain().barcode)
    }

    @Test
    fun `details mapper returns null when there is no barcode identifier`() {
        val dto = detailsDto(
            identifiers = listOf(
                IdentifierDto(type = "Matrix / Runout", value = "SONOPRESS 123")
            )
        )

        assertNull(dto.toDomain().barcode)
    }

    @Test
    fun `details mapper returns null when barcode has no digits`() {
        val dto = detailsDto(
            identifiers = listOf(
                IdentifierDto(type = "Barcode", value = "none")
            )
        )

        assertNull(dto.toDomain().barcode)
    }

    @Test
    fun `search mapper also normalizes barcode`() {
        val dto = SearchResultDto(
            id = 1L,
            title = "Nightwish - Once",
            year = "2004",
            country = "EU",
            format = listOf("CD"),
            label = listOf("Nuclear Blast"),
            coverImage = null,
            barcode = listOf("6 02547-378781")
        )

        assertEquals("602547378781", dto.toDomain().barcode)
    }

    private fun detailsDto(identifiers: List<IdentifierDto>?) = ReleaseDetailsDto(
        id = 1L,
        title = "Nightwish - Once",
        year = 2004,
        country = "EU",
        released = "2004-05-31",
        notes = null,
        artists = listOf(ArtistDto(name = "Nightwish")),
        labels = listOf(LabelDto(name = "Nuclear Blast", catno = "NB 1234")),
        formats = listOf(FormatDto(name = "CD", descriptions = listOf("Album"))),
        tracklist = emptyList(),
        images = emptyList(),
        identifiers = identifiers
    )
}
