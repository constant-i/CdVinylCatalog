package com.kai.cdvinylcatalog.feature.add.manual

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.kai.cdvinylcatalog.domain.media.PhotoDestination
import com.kai.cdvinylcatalog.domain.media.PhotoStorage
import com.kai.cdvinylcatalog.domain.usecase.collection.AddManualItemUseCase
import com.kai.cdvinylcatalog.domain.usecase.collection.GetCollectionItemUseCase
import com.kai.cdvinylcatalog.domain.usecase.collection.UpdateCollectionItemUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Тесты формы ручного добавления.
 *
 * Покрывают две группы поведения:
 * - экономная запись в `SavedStateHandle` (на каждый символ пишется один ключ);
 * - фото-флоу, который целиком переехал в ViewModel из Compose-экрана.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ManualAddViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var photoStorage: PhotoStorage

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Пустой Bundle на старте: форма в режиме создания, ничего не восстановлено
        savedStateHandle = mockk(relaxed = true)
        every { savedStateHandle.get<Long>(any()) } returns null
        every { savedStateHandle.get<Boolean>(any()) } returns null
        every { savedStateHandle.get<String>(any()) } returns null

        photoStorage = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ─── SavedStateHandle ───

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

    // ─── Камера ───

    @Test
    fun `camera click prepares destination and asks to launch camera`() = runTest {
        every { photoStorage.createDestination() } returns CAMERA_DESTINATION

        val viewModel = viewModel()

        viewModel.effect.test {
            viewModel.onIntent(ManualAddContract.Intent.OnCameraClicked)

            assertEquals(
                ManualAddContract.Effect.LaunchCamera(CAMERA_DESTINATION.shareableUri),
                awaitItem()
            )
        }
    }

    @Test
    fun `successful camera result sends file to crop`() = runTest {
        every { photoStorage.createDestination() } returnsMany
            listOf(CAMERA_DESTINATION, CROP_DESTINATION)
        every { photoStorage.isUsable(CAMERA_DESTINATION.path) } returns true
        every { photoStorage.shareableUri(CAMERA_DESTINATION.path) } returns
            CAMERA_DESTINATION.shareableUri

        val viewModel = viewModel()

        viewModel.effect.test {
            viewModel.onIntent(ManualAddContract.Intent.OnCameraClicked)
            skipItems(1) // LaunchCamera

            viewModel.onIntent(ManualAddContract.Intent.OnCameraResult(success = true))

            assertEquals(
                ManualAddContract.Effect.LaunchCrop(
                    sourceUri = CAMERA_DESTINATION.shareableUri,
                    destinationUri = CROP_DESTINATION.shareableUri
                ),
                awaitItem()
            )
        }
    }

    @Test
    fun `empty photo file is deleted and crop never starts`() = runTest {
        every { photoStorage.createDestination() } returns CAMERA_DESTINATION
        every { photoStorage.isUsable(CAMERA_DESTINATION.path) } returns false

        val viewModel = viewModel()

        viewModel.effect.test {
            viewModel.onIntent(ManualAddContract.Intent.OnCameraClicked)
            skipItems(1) // LaunchCamera

            viewModel.onIntent(ManualAddContract.Intent.OnCameraResult(success = true))

            expectNoEvents()
            verify(exactly = 1) { photoStorage.delete(CAMERA_DESTINATION.path) }
            assertEquals(null, viewModel.state.value.pendingPhotoPath)
        }
    }

    @Test
    fun `cancelled camera deletes the prepared file`() = runTest {
        every { photoStorage.createDestination() } returns CAMERA_DESTINATION

        val viewModel = viewModel()

        viewModel.onIntent(ManualAddContract.Intent.OnCameraClicked)
        viewModel.onIntent(ManualAddContract.Intent.OnCameraResult(success = false))

        verify(exactly = 1) { photoStorage.delete(CAMERA_DESTINATION.path) }
        assertEquals(null, viewModel.state.value.pendingPhotoPath)
    }

    // ─── Галерея ───

    @Test
    fun `picked photo is imported and sent to crop`() = runTest {
        coEvery { photoStorage.import(EXTERNAL_URI) } returns IMPORTED_DESTINATION
        every { photoStorage.createDestination() } returns CROP_DESTINATION
        every { photoStorage.shareableUri(IMPORTED_DESTINATION.path) } returns
            IMPORTED_DESTINATION.shareableUri

        val viewModel = viewModel()

        viewModel.effect.test {
            viewModel.onIntent(ManualAddContract.Intent.OnPhotoPicked(EXTERNAL_URI))

            assertEquals(
                ManualAddContract.Effect.LaunchCrop(
                    sourceUri = IMPORTED_DESTINATION.shareableUri,
                    destinationUri = CROP_DESTINATION.shareableUri
                ),
                awaitItem()
            )
        }
    }

    @Test
    fun `failed import shows toast and does not start crop`() = runTest {
        coEvery { photoStorage.import(EXTERNAL_URI) } returns null

        val viewModel = viewModel()

        viewModel.effect.test {
            viewModel.onIntent(ManualAddContract.Intent.OnPhotoPicked(EXTERNAL_URI))

            val effect = awaitItem()
            assertTrue(effect is ManualAddContract.Effect.ShowToast)
            expectNoEvents()
        }
    }

    @Test
    fun `camera click is ignored when photo limit reached`() = runTest {
        every { photoStorage.createDestination() } returns CAMERA_DESTINATION
        every { photoStorage.isUsable(CAMERA_DESTINATION.path) } returns true
        every { photoStorage.shareableUri(CAMERA_DESTINATION.path) } returns
            CAMERA_DESTINATION.shareableUri

        val viewModel = viewModel()

        // Набираем максимум фото штатным путём: камера → кроп → добавление
        repeat(ManualAddContract.State.MAX_PHOTOS) {
            viewModel.onIntent(ManualAddContract.Intent.OnCameraClicked)
            viewModel.onIntent(ManualAddContract.Intent.OnCameraResult(success = true))
            viewModel.onIntent(ManualAddContract.Intent.OnCropFinished)
        }
        assertEquals(
            ManualAddContract.State.MAX_PHOTOS,
            viewModel.state.value.photoPaths.size
        )

        val callsBefore = 2 * ManualAddContract.State.MAX_PHOTOS
        viewModel.effect.test {
            viewModel.onIntent(ManualAddContract.Intent.OnCameraClicked)
            expectNoEvents()
        }
        // Два файла на цикл: под снимок и под результат кропа
        verify(exactly = callsBefore) { photoStorage.createDestination() }
        assertEquals(
            ManualAddContract.State.MAX_PHOTOS,
            viewModel.state.value.photoPaths.size
        )
    }

    // ─── Кроп ───

    @Test
    fun `crop result adds destination to photo list`() = runTest {
        every { photoStorage.createDestination() } returnsMany
            listOf(CAMERA_DESTINATION, CROP_DESTINATION)
        every { photoStorage.isUsable(CAMERA_DESTINATION.path) } returns true
        every { photoStorage.shareableUri(CAMERA_DESTINATION.path) } returns
            CAMERA_DESTINATION.shareableUri

        val viewModel = viewModel()

        viewModel.onIntent(ManualAddContract.Intent.OnCameraClicked)
        viewModel.onIntent(ManualAddContract.Intent.OnCameraResult(success = true))
        viewModel.onIntent(ManualAddContract.Intent.OnCropFinished)

        assertEquals(
            listOf(CROP_DESTINATION.path),
            viewModel.state.value.photoPaths
        )
        assertEquals(null, viewModel.state.value.pendingCropDestination)
    }

    @Test
    fun `cancelled crop deletes prepared destination`() = runTest {
        every { photoStorage.createDestination() } returnsMany
            listOf(CAMERA_DESTINATION, CROP_DESTINATION)
        every { photoStorage.isUsable(CAMERA_DESTINATION.path) } returns true
        every { photoStorage.shareableUri(CAMERA_DESTINATION.path) } returns
            CAMERA_DESTINATION.shareableUri

        val viewModel = viewModel()

        viewModel.onIntent(ManualAddContract.Intent.OnCameraClicked)
        viewModel.onIntent(ManualAddContract.Intent.OnCameraResult(success = true))
        viewModel.onIntent(ManualAddContract.Intent.OnCropCancelled)

        verify(exactly = 1) { photoStorage.delete(CROP_DESTINATION.path) }
        assertTrue(viewModel.state.value.photoPaths.isEmpty())
    }

    @Test
    fun `successful crop deletes the multi-megabyte source file`() = runTest {
        every { photoStorage.createDestination() } returnsMany
            listOf(CAMERA_DESTINATION, CROP_DESTINATION)
        every { photoStorage.isUsable(CAMERA_DESTINATION.path) } returns true
        every { photoStorage.shareableUri(CAMERA_DESTINATION.path) } returns
            CAMERA_DESTINATION.shareableUri

        val viewModel = viewModel()

        viewModel.onIntent(ManualAddContract.Intent.OnCameraClicked)
        viewModel.onIntent(ManualAddContract.Intent.OnCameraResult(success = true))
        viewModel.onIntent(ManualAddContract.Intent.OnCropFinished)

        // Исходный снимок остаётся на диске, если его не удалить, — утечка в мегабайты
        verify(exactly = 1) { photoStorage.delete(CAMERA_DESTINATION.path) }
        assertEquals(
            listOf(CROP_DESTINATION.path),
            viewModel.state.value.photoPaths
        )
    }

    @Test
    fun `removing photo deletes its file`() {
        val viewModel = createViewModel()

        viewModel.onIntent(ManualAddContract.Intent.OnPhotoRemoved(CAMERA_DESTINATION.path))

        verify(exactly = 1) { photoStorage.delete(CAMERA_DESTINATION.path) }
    }

    private fun createViewModel() = ManualAddViewModel(
        savedStateHandle = savedStateHandle,
        getItem = mockk<GetCollectionItemUseCase>(relaxed = true),
        addManualItem = mockk<AddManualItemUseCase>(relaxed = true),
        updateItem = mockk<UpdateCollectionItemUseCase>(relaxed = true),
        photoStorage = photoStorage
    )

    private fun viewModel() = createViewModel()

    private companion object {
        const val KEY_TITLE = "title"
        const val KEY_NOTES = "notes"
        const val KEY_PHOTO_PATHS = "photo_paths"
        const val EXTERNAL_URI = "content://picker/photo/1"

        val CAMERA_DESTINATION = PhotoDestination(
            path = "/data/photos/camera.jpg",
            shareableUri = "content://app.fileprovider/photos/camera.jpg"
        )
        val CROP_DESTINATION = PhotoDestination(
            path = "/data/photos/cropped.jpg",
            shareableUri = "content://app.fileprovider/photos/cropped.jpg"
        )
        val IMPORTED_DESTINATION = PhotoDestination(
            path = "/data/photos/imported.jpg",
            shareableUri = "content://app.fileprovider/photos/imported.jpg"
        )
    }
}
