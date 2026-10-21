package com.kai.cdvinylcatalog.feature.add.manual

import androidx.lifecycle.SavedStateHandle
import com.kai.cdvinylcatalog.domain.repository.CollectionRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

/**
 * `update()` вызывается на каждый введённый символ, поэтому в Bundle должны
 * попадать только изменившиеся ключи. Иначе набор названия альбома пишет
 * несколько сотен значений и роняет отзывчивость ввода.
 */
class ManualAddViewModelTest {

    private lateinit var savedStateHandle: SavedStateHandle

    @Before
    fun setup() {
        // Пустой Bundle на старте: форма в режиме создания, ничего не восстановлено
        savedStateHandle = mockk(relaxed = true)
        every { savedStateHandle.get<Long>(any()) } returns null
        every { savedStateHandle.get<Boolean>(any()) } returns null
        every { savedStateHandle.get<String>(any()) } returns null
    }

    @Test
    fun `typing writes the title only once per keystroke`() {
        val viewModel = createViewModel()

        viewModel.onIntent(ManualAddContract.Intent.OnTitleChanged("N"))
        viewModel.onIntent(ManualAddContract.Intent.OnTitleChanged("Ne"))

        verify(exactly = 1) { savedStateHandle[KEY_TITLE] = "N" }
        verify(exactly = 1) { savedStateHandle[KEY_TITLE] = "Ne" }
    }

    @Test
    fun `typing does not rewrite unrelated keys`() {
        val viewModel = createViewModel()

        viewModel.onIntent(ManualAddContract.Intent.OnTitleChanged("N"))
        viewModel.onIntent(ManualAddContract.Intent.OnTitleChanged("Ne"))
        viewModel.onIntent(ManualAddContract.Intent.OnTitleChanged("Nev"))

        // Список фото не менялся — значит в Bundle он попадает один раз,
        // при первой записи State, а не на каждый символ.
        verify(exactly = 1) { savedStateHandle[KEY_PHOTO_PATHS] = any<String>() }
        verify(exactly = 1) { savedStateHandle[KEY_NOTES] = any<String>() }
    }

    private fun createViewModel() =
        ManualAddViewModel(savedStateHandle, mockk<CollectionRepository>(relaxed = true))

    private companion object {
        const val KEY_TITLE = "title"
        const val KEY_NOTES = "notes"
        const val KEY_PHOTO_PATHS = "photo_paths"
    }
}
