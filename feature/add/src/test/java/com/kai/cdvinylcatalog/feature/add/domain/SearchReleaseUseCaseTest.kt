package com.kai.cdvinylcatalog.feature.add.domain

import androidx.paging.PagingData
import com.kai.cdvinylcatalog.domain.catalog.Release
import com.kai.cdvinylcatalog.domain.repository.ReleaseRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

/**
 * Сценарий использования: поиск релиза в Discogs по штрихкоду.
 *
 * Начиная с Paging 3 UseCase не выполняет сетевой вызов сам — он возвращает
 * `Flow<PagingData<Release>>`, который лениво запускает `PagingSource`.
 * Поэтому проверяем контракт делегирования: обрезку пробелов и проброс потока.
 *
 * Валидация штрихкода живёт на уровне UI (кнопка недоступна для пустого ввода),
 * а не в UseCase — это тоже зафиксировано тестом.
 */
class SearchReleaseUseCaseTest {

    private lateinit var repository: ReleaseRepository
    private lateinit var useCase: SearchReleaseUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = SearchReleaseUseCase(repository)
    }

    @Test
    fun `returns paging flow produced by repository`() {
        val expected = flowOf(PagingData.from(listOf(createRelease())))
        every { repository.searchByBarcode(BARCODE) } returns expected

        val actual = useCase(BARCODE)

        assertSame(expected, actual)
        verify(exactly = 1) { repository.searchByBarcode(BARCODE) }
    }

    @Test
    fun `trims surrounding whitespace before calling repository`() {
        val expected = flowOf(PagingData.empty<Release>())
        every { repository.searchByBarcode(BARCODE) } returns expected

        val actual = useCase("  $BARCODE  ")

        assertSame(expected, actual)
        verify(exactly = 1) { repository.searchByBarcode(BARCODE) }
    }

    @Test
    fun `does not validate input - blank barcode is delegated as is`() {
        val expected = flowOf(PagingData.empty<Release>())
        every { repository.searchByBarcode("") } returns expected

        val actual = useCase("")

        assertSame(expected, actual)
        verify(exactly = 1) { repository.searchByBarcode("") }
    }

    private fun createRelease() = Release(
        id = 1L,
        title = "Encore",
        artist = "Sarah Brightman",
        year = 2007,
        barcode = null,
        coverImageUrl = null,
        imageUrls = emptyList(),
        label = null,
        rawFormat = null,
        country = null,
        releaseDate = null,
        catalogNumber = null,
        notes = null,
        tracklist = emptyList()
    )

    private companion object {
        const val BARCODE = "602547378781"
    }
}
