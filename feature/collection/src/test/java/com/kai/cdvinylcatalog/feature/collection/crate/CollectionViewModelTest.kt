package com.kai.cdvinylcatalog.feature.collection.crate

import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingData
import com.kai.cdvinylcatalog.domain.collection.SortOrder
import com.kai.cdvinylcatalog.domain.repository.CollectionReader
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
import org.junit.Before
import org.junit.Test

/**
 * Сортировка должна переживать смерть процесса.
 *
 * `MutableStateFlow` живёт в ViewModel, а ViewModel исчезает вместе с записью
 * в стеке навигации после того, как система убьёт процесс. Сохранение в
 * `SavedStateHandle` — единственный способ вернуть пользователя к тому же
 * виду списка.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CollectionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository: CollectionReader = mockk()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repository.getItems(any()) } returns flowOf(PagingData.empty())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `starts with the default order when nothing was saved`() = runTest {
        val viewModel = CollectionViewModel(repository, SavedStateHandle())

        assertEquals(SortOrder.ADDED_DESC, viewModel.state.value.sortOrder)
    }

    @Test
    fun `restores the saved order after process death`() = runTest {
        val saved = SavedStateHandle(mapOf("sort_order" to SortOrder.ARTIST_ASC.name))

        val viewModel = CollectionViewModel(repository, saved)

        assertEquals(SortOrder.ARTIST_ASC, viewModel.state.value.sortOrder)
    }

    @Test
    fun `unknown saved value falls back to the default order`() = runTest {
        // Так выглядела бы запись, сделанная версией с другим набором сортировок
        val saved = SavedStateHandle(mapOf("sort_order" to "ALBUM_TITLE_ASC"))

        val viewModel = CollectionViewModel(repository, saved)

        assertEquals(SortOrder.ADDED_DESC, viewModel.state.value.sortOrder)
    }

    @Test
    fun `changing the order writes it to the saved state`() = runTest {
        val saved = SavedStateHandle()
        val viewModel = CollectionViewModel(repository, saved)

        viewModel.onIntent(CollectionContract.Intent.OnSortChanged(SortOrder.YEAR_DESC))

        assertEquals(SortOrder.YEAR_DESC, viewModel.state.value.sortOrder)
        assertEquals(SortOrder.YEAR_DESC.name, saved.get<String>("sort_order"))
    }

    @Test
    fun `repeating the same order writes nothing extra`() = runTest {
        val saved = SavedStateHandle()
        val viewModel = CollectionViewModel(repository, saved)

        viewModel.onIntent(CollectionContract.Intent.OnSortChanged(SortOrder.ADDED_DESC))

        // Порядок не изменился — запись в Bundle была бы лишней работой
        assertEquals(null, saved.get<String>("sort_order"))
    }
}
