package com.kai.cdvinylcatalog.domain.usecase.catalog

import androidx.paging.PagingData
import com.kai.cdvinylcatalog.domain.catalog.Release
import com.kai.cdvinylcatalog.domain.repository.ReleaseRepository
import com.kai.cdvinylcatalog.domain.result.Outcome
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

/**
 * Поиск релиза в Discogs по штрихкоду.
 *
 * UseCase не выполняет сетевой вызов сам — возвращает `Flow<PagingData>`,
 * который лениво запускает PagingSource. Поэтому проверяем контракт
 * делегирования: обрезку пробелов и проброс потока.
 *
 * Валидация штрихкода живёт на уровне UI (кнопка недоступна для пустого
 * ввода), а не в UseCase — это тоже зафиксировано тестом.
 */
class SearchReleaseUseCaseTest {

    private lateinit var repository: RecordingReleaseRepository
    private lateinit var useCase: SearchReleaseUseCase

    @Before
    fun setup() {
        repository = RecordingReleaseRepository()
        useCase = SearchReleaseUseCase(repository)
    }

    @Test
    fun `returns paging flow produced by repository`() {
        val expected = flowOf(PagingData.empty<Release>())
        repository.result = expected

        val actual = useCase(BARCODE)

        assertSame(expected, actual)
        assertEquals(listOf(BARCODE), repository.requestedBarcodes)
    }

    @Test
    fun `trims surrounding whitespace before calling repository`() {
        val expected = flowOf(PagingData.empty<Release>())
        repository.result = expected

        val actual = useCase("  $BARCODE  ")

        assertSame(expected, actual)
        assertEquals(listOf(BARCODE), repository.requestedBarcodes)
    }

    @Test
    fun `does not validate input - blank barcode is delegated as is`() {
        repository.result = flowOf(PagingData.empty())

        useCase("")

        assertEquals(listOf(""), repository.requestedBarcodes)
    }

    /**
     * Фейк вместо мока: запоминает, с какими штрихкодами его позвали.
     * Мокк здесь не нужен — тест проверяет делегирование, а не взаимодействие
     * с фреймворком.
     */
    private class RecordingReleaseRepository : ReleaseRepository {

        val requestedBarcodes = mutableListOf<String>()
        var result: Flow<PagingData<Release>> = flowOf(PagingData.empty())

        override fun searchByBarcode(barcode: String): Flow<PagingData<Release>> {
            requestedBarcodes += barcode
            return result
        }

        override fun searchByText(query: String): Flow<PagingData<Release>> =
            flowOf(PagingData.empty())

        override suspend fun getReleaseDetails(releaseId: Long): Outcome<Release> =
            error("not used")
    }

    private companion object {
        const val BARCODE = "602547378781"
    }
}
