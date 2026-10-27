package com.kai.cdvinylcatalog.feature.add.search

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Экран поиска — это форма, и введённый запрос не должен теряться
 * при смерти процесса: ровно так же ведёт себя форма ручного добавления.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `starts empty when nothing was saved`() = runTest {
        val viewModel = SearchViewModel(SavedStateHandle())

        assertEquals("", viewModel.state.value.query)
    }

    @Test
    fun `restores the typed query after process death`() = runTest {
        val saved = SavedStateHandle(mapOf("query" to "Nightwish Once"))

        val viewModel = SearchViewModel(saved)

        assertEquals("Nightwish Once", viewModel.state.value.query)
    }

    @Test
    fun `typing writes the query to the saved state`() = runTest {
        val saved = SavedStateHandle()
        val viewModel = SearchViewModel(saved)

        viewModel.onIntent(SearchContract.Intent.OnQueryChanged("Nirvana"))

        assertEquals("Nirvana", viewModel.state.value.query)
        assertEquals("Nirvana", saved.get<String>("query"))
    }
}
