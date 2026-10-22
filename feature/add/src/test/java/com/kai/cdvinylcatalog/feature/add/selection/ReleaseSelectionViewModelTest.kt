package com.kai.cdvinylcatalog.feature.add.selection

import androidx.paging.PagingData
import com.kai.cdvinylcatalog.domain.catalog.Format
import com.kai.cdvinylcatalog.domain.catalog.Release
import com.kai.cdvinylcatalog.domain.collection.CollectionItem
import com.kai.cdvinylcatalog.domain.result.AppError
import com.kai.cdvinylcatalog.domain.result.Outcome
import com.kai.cdvinylcatalog.domain.usecase.catalog.SearchByTextUseCase
import com.kai.cdvinylcatalog.domain.usecase.catalog.SearchReleaseUseCase
import com.kai.cdvinylcatalog.domain.usecase.collection.FindItemsByBarcodeUseCase
import com.kai.cdvinylcatalog.domain.usecase.collection.ObserveCollectionReleaseIdsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Офлайн-фолбэк: когда сети нет, вместо ошибки отвечаем по локальной базе.
 *
 * Ключевое ограничение: локальный поиск возможен только по штрихкоду.
 * Текстовый поиск локально выполнить нельзя — для него нужен полнотекстовый индекс.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReleaseSelectionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var findItemsByBarcode: FindItemsByBarcodeUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        findItemsByBarcode = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `offline fallback finds local matches by barcode`() = runTest {
        val item = localItem(id = 7L)
        coEvery { findItemsByBarcode(BARCODE) } returns Outcome.Success(listOf(item))

        val viewModel = viewModel()
        viewModel.initSearch(BARCODE, ReleaseSelectionContract.Source.SCAN)
        viewModel.onIntent(ReleaseSelectionContract.Intent.OnOfflineFallback)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(listOf(item), viewModel.state.value.offlineMatches)
        assertTrue(viewModel.state.value.isOffline)
    }

    @Test
    fun `offline fallback reports empty list when nothing matches`() = runTest {
        coEvery { findItemsByBarcode(BARCODE) } returns Outcome.Success(emptyList())

        val viewModel = viewModel()
        viewModel.initSearch(BARCODE, ReleaseSelectionContract.Source.SCAN)
        viewModel.onIntent(ReleaseSelectionContract.Intent.OnOfflineFallback)
        testDispatcher.scheduler.advanceUntilIdle()

        // Пустой список — тоже валидный ответ: «диска с таким штрихкодом нет»
        assertEquals(emptyList<CollectionItem>(), viewModel.state.value.offlineMatches)
        assertTrue(viewModel.state.value.isOffline)
    }

    @Test
    fun `offline fallback is ignored for text search`() = runTest {
        val viewModel = viewModel()
        viewModel.initSearch("Nightwish Once", ReleaseSelectionContract.Source.SEARCH)
        viewModel.onIntent(ReleaseSelectionContract.Intent.OnOfflineFallback)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.state.value.offlineMatches)
        coVerify(exactly = 0) { findItemsByBarcode(any()) }
    }

    @Test
    fun `offline fallback runs only once`() = runTest {
        coEvery { findItemsByBarcode(BARCODE) } returns Outcome.Success(emptyList())

        val viewModel = viewModel()
        viewModel.initSearch(BARCODE, ReleaseSelectionContract.Source.SCAN)
        viewModel.onIntent(ReleaseSelectionContract.Intent.OnOfflineFallback)
        viewModel.onIntent(ReleaseSelectionContract.Intent.OnOfflineFallback)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { findItemsByBarcode(BARCODE) }
    }

    @Test
    fun `retry online clears offline state`() = runTest {
        coEvery { findItemsByBarcode(BARCODE) } returns Outcome.Success(listOf(localItem(1L)))

        val viewModel = viewModel()
        viewModel.initSearch(BARCODE, ReleaseSelectionContract.Source.SCAN)
        viewModel.onIntent(ReleaseSelectionContract.Intent.OnOfflineFallback)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onIntent(ReleaseSelectionContract.Intent.OnRetryOnline)

        assertNull(viewModel.state.value.offlineMatches)
    }

    @Test
    fun `failure from local lookup shows empty result instead of crashing`() = runTest {
        coEvery { findItemsByBarcode(BARCODE) } returns Outcome.Failure(AppError.Unknown)

        val viewModel = viewModel()
        viewModel.initSearch(BARCODE, ReleaseSelectionContract.Source.SCAN)
        viewModel.onIntent(ReleaseSelectionContract.Intent.OnOfflineFallback)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(emptyList<CollectionItem>(), viewModel.state.value.offlineMatches)
    }

    private fun viewModel(): ReleaseSelectionViewModel {
        val searchByText = mockk<SearchByTextUseCase>()
        every { searchByText(any()) } returns flowOf(PagingData.empty())

        val searchByBarcode = mockk<SearchReleaseUseCase>()
        every { searchByBarcode(any()) } returns flowOf(PagingData.empty())

        val observeIds = mockk<ObserveCollectionReleaseIdsUseCase>()
        every { observeIds() } returns flowOf(emptySet())

        return ReleaseSelectionViewModel(
            searchByText = searchByText,
            searchByBarcode = searchByBarcode,
            observeCollectionReleaseIds = observeIds,
            findItemsByBarcode = findItemsByBarcode
        )
    }

    private fun localItem(id: Long) = CollectionItem(
        id = id,
        release = Release(
            id = 0L,
            title = "Once",
            artist = "Nightwish",
            year = 2004,
            barcode = BARCODE,
            coverImageUrl = null,
            label = "Nuclear Blast",
            rawFormat = "CD",
            country = "Europe",
            releaseDate = null,
            catalogNumber = null,
            notes = null
        ),
        format = Format.CD,
        addedAt = 0L,
        notes = null
    )

    private companion object {
        const val BARCODE = "727361129125"
    }
}
