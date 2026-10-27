package com.kai.cdvinylcatalog.feature.collection.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.kai.cdvinylcatalog.common.ui.message.UiMessage
import com.kai.cdvinylcatalog.domain.catalog.Format
import com.kai.cdvinylcatalog.domain.catalog.Release
import com.kai.cdvinylcatalog.domain.collection.CollectionItem
import com.kai.cdvinylcatalog.domain.repository.CollectionReader
import com.kai.cdvinylcatalog.domain.result.AppError
import com.kai.cdvinylcatalog.domain.result.Outcome
import com.kai.cdvinylcatalog.domain.usecase.catalog.GetReleaseDetailsUseCase
import com.kai.cdvinylcatalog.domain.usecase.collection.DeleteCollectionItemUseCase
import com.kai.cdvinylcatalog.feature.collection.detail.CollectionItemDetailContract.Effect
import com.kai.cdvinylcatalog.feature.collection.detail.CollectionItemDetailContract.Intent
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
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

/**
 * Удаление записи с экрана деталей.
 *
 * Экран не обращается к репозиторию напрямую: удаление идёт через
 * `DeleteCollectionItemUseCase`, иначе фотографии оставались бы на диске.
 * Отдельно проверяется, что запись, исчезнувшая из базы, закрывает экран,
 * а не оставляет пользователя на пустом экране.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CollectionItemDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val reader: CollectionReader = mockk()
    private val deleteItem: DeleteCollectionItemUseCase = mockk()
    private val getReleaseDetails: GetReleaseDetailsUseCase = mockk()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `successful delete goes through the use case and leaves the screen`() = runTest {
        every { reader.observeItemById(ITEM_ID) } returns flowOf(manualItem())
        coEvery { deleteItem(ITEM_ID) } returns Outcome.Success(Unit)

        val viewModel = viewModel()
        // Даём observeItem() доставить запись в State: без этого удалять нечего
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onIntent(Intent.OnDeleteClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(Effect.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        // Именно use case: он же удаляет файлы фотографий
        coVerify { deleteItem(ITEM_ID) }
    }

    @Test
    fun `failed delete keeps the screen and reports the error`() = runTest {
        every { reader.observeItemById(ITEM_ID) } returns flowOf(manualItem())
        coEvery { deleteItem(ITEM_ID) } returns Outcome.Failure(AppError.Unknown)

        val viewModel = viewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onIntent(Intent.OnDeleteClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(Effect.ShowToast(UiMessage.Error(AppError.Unknown)), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        // Флаг снимается, иначе экран навсегда останется в состоянии «удаляю»
        assertFalse(viewModel.state.value.isDeleting)
    }

    @Test
    fun `deleted item closes the screen instead of showing an empty state`() = runTest {
        every { reader.observeItemById(ITEM_ID) } returns flowOf(null)

        val viewModel = viewModel()

        viewModel.effect.test {
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(Effect.ShowToast(UiMessage.ItemNotFound), awaitItem())
            assertEquals(Effect.NavigateBack, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `item without a discogs id loads no remote details`() = runTest {
        every { reader.observeItemById(ITEM_ID) } returns flowOf(manualItem())

        viewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // releaseId = 0 — запись добавлена вручную, запрашивать нечего
        coVerify(exactly = 0) { getReleaseDetails(any()) }
        coVerify(exactly = 0) { reader.getOtherCopies(any(), any()) }
    }

    private fun viewModel() = CollectionItemDetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf("itemId" to ITEM_ID)),
        reader = reader,
        deleteItem = deleteItem,
        getReleaseDetails = getReleaseDetails,
    )

    /** Запись, добавленная вручную: связи с Discogs нет, детали грузить не нужно. */
    private fun manualItem() = CollectionItem(
        id = ITEM_ID,
        release = Release(
            id = 0L,
            title = "Once",
            artist = "Nightwish",
            year = 2004,
            barcode = null,
            coverImageUrl = null,
            label = null,
            rawFormat = "CD",
            country = null,
            releaseDate = null,
            catalogNumber = null,
            notes = null
        ),
        format = Format.CD,
        addedAt = 0L
    )

    private companion object {
        const val ITEM_ID = 42L
    }
}
