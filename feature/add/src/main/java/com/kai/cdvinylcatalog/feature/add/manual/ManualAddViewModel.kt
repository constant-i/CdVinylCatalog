package com.kai.cdvinylcatalog.feature.add.manual

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kai.cdvinylcatalog.common.ui.message.UiMessage
import com.kai.cdvinylcatalog.domain.catalog.Format
import com.kai.cdvinylcatalog.domain.catalog.parseStoredFormat
import com.kai.cdvinylcatalog.domain.media.PhotoStorage
import com.kai.cdvinylcatalog.domain.result.Outcome
import com.kai.cdvinylcatalog.domain.usecase.collection.AddManualItemUseCase
import com.kai.cdvinylcatalog.domain.usecase.collection.CollectionItemUpdate
import com.kai.cdvinylcatalog.domain.usecase.collection.GetCollectionItemUseCase
import com.kai.cdvinylcatalog.domain.usecase.collection.UpdateCollectionItemUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManualAddViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getItem: GetCollectionItemUseCase,
    private val addManualItem: AddManualItemUseCase,
    private val updateItem: UpdateCollectionItemUseCase,
    private val photoStorage: PhotoStorage,
) : ViewModel() {

    private val editItemId: Long? = savedStateHandle.get<Long>("itemId")?.takeIf { it > 0 }

    /** Последний записанный в Bundle State — чтобы писать только изменения. */
    private var lastPersistedState: ManualAddContract.State? = null

    private val _state = MutableStateFlow(restoreState())
    val state: StateFlow<ManualAddContract.State> = _state.asStateFlow()

    private val _effect = Channel<ManualAddContract.Effect>()
    val effect = _effect.receiveAsFlow()

    init {
        val alreadyInitialized = savedStateHandle.get<Boolean>(KEY_INITIALIZED) ?: false

        if (editItemId != null && !alreadyInitialized) {
            loadExistingItem(editItemId)
        }
        savedStateHandle[KEY_INITIALIZED] = true
    }

    /**
     * Восстановить State из SavedStateHandle.
     * Вызывается при создании ViewModel — после возможного пересоздания процесса.
     */
    private fun restoreState(): ManualAddContract.State {
        return ManualAddContract.State(
            mode = if (editItemId != null) {
                ManualAddContract.Mode.Edit(editItemId)
            } else {
                ManualAddContract.Mode.Create
            },
            title = savedStateHandle.get<String>(KEY_TITLE) ?: "",
            artist = savedStateHandle.get<String>(KEY_ARTIST) ?: "",
            year = savedStateHandle.get<String>(KEY_YEAR) ?: "",
            format = parseStoredFormat(savedStateHandle.get<String>(KEY_FORMAT))
                .takeIf { it != Format.UNKNOWN }
                ?: Format.CD,
            label = savedStateHandle.get<String>(KEY_LABEL) ?: "",
            country = savedStateHandle.get<String>(KEY_COUNTRY) ?: "",
            barcode = savedStateHandle.get<String>(KEY_BARCODE) ?: "",
            notes = savedStateHandle.get<String>(KEY_NOTES) ?: "",
            photoPaths = savedStateHandle.get<String>(KEY_PHOTO_PATHS)
                ?.split(SEPARATOR)
                ?.filter { it.isNotBlank() }
                ?: emptyList(),
            discogsImages = savedStateHandle.get<String>(KEY_DISCOGS_IMAGES)
                ?.split(SEPARATOR)
                ?.filter { it.isNotBlank() }
                ?: emptyList(),
            pendingPhotoPath = savedStateHandle.get<String>(KEY_PENDING_PHOTO_PATH),
            pendingCropPath = savedStateHandle.get<String>(KEY_PENDING_CROP_PATH),
            pendingCropDestination = savedStateHandle.get<String>(KEY_PENDING_CROP_DESTINATION),
        )
    }

    /**
     * Сохранить State в SavedStateHandle.
     *
     * Пишем только изменившиеся ключи: State обновляется на каждый введённый
     * символ, и записывать все 12 ключей на каждое нажатие слишком дорого для
     * Bundle (а для списков — ещё и лишние joinToString).
     */
    private fun persistState(state: ManualAddContract.State) {
        val previous = lastPersistedState
        lastPersistedState = state

        if (previous?.title != state.title) savedStateHandle[KEY_TITLE] = state.title
        if (previous?.artist != state.artist) savedStateHandle[KEY_ARTIST] = state.artist
        if (previous?.year != state.year) savedStateHandle[KEY_YEAR] = state.year
        if (previous?.format != state.format) savedStateHandle[KEY_FORMAT] = state.format.name
        if (previous?.label != state.label) savedStateHandle[KEY_LABEL] = state.label
        if (previous?.country != state.country) savedStateHandle[KEY_COUNTRY] = state.country
        if (previous?.barcode != state.barcode) savedStateHandle[KEY_BARCODE] = state.barcode
        if (previous?.notes != state.notes) savedStateHandle[KEY_NOTES] = state.notes
        if (previous?.photoPaths != state.photoPaths) {
            savedStateHandle[KEY_PHOTO_PATHS] = state.photoPaths.joinToString(SEPARATOR)
        }
        if (previous?.discogsImages != state.discogsImages) {
            savedStateHandle[KEY_DISCOGS_IMAGES] = state.discogsImages.joinToString(SEPARATOR)
        }
        if (previous?.pendingPhotoPath != state.pendingPhotoPath) {
            savedStateHandle[KEY_PENDING_PHOTO_PATH] = state.pendingPhotoPath
        }
        if (previous?.pendingCropPath != state.pendingCropPath) {
            savedStateHandle[KEY_PENDING_CROP_PATH] = state.pendingCropPath
        }
        if (previous?.pendingCropDestination != state.pendingCropDestination) {
            savedStateHandle[KEY_PENDING_CROP_DESTINATION] = state.pendingCropDestination
        }
    }

    /**
     * Единая точка обновления State — с сохранением в SavedStateHandle.
     */
    private fun update(block: (ManualAddContract.State) -> ManualAddContract.State) {
        _state.update { current ->
            val newState = block(current)
            persistState(newState)
            newState
        }
    }

    private fun loadExistingItem(id: Long) {
        viewModelScope.launch {
            when (val outcome = getItem(id)) {
                is Outcome.Success -> {
                    val item = outcome.data
                    if (item != null) {
                        val allImages = item.release.imageUrls
                        val userPhotos = allImages.filter { !it.startsWith("http") }
                        val discogsImages = allImages.filter { it.startsWith("http") }

                        update {
                            it.copy(
                                title = item.release.title,
                                artist = item.release.artist,
                                year = item.release.year?.toString() ?: "",
                                format = item.format,
                                label = item.release.label ?: "",
                                country = item.release.country ?: "",
                                barcode = item.release.barcode ?: "",
                                notes = item.notes ?: "",
                                photoPaths = userPhotos,
                                discogsImages = discogsImages
                            )
                        }
                    }
                }
                is Outcome.Failure -> {
                    _effect.send(ManualAddContract.Effect.ShowToast(UiMessage.Error(outcome.error)))
                }
            }
        }
    }

    fun onIntent(intent: ManualAddContract.Intent) {
        when (intent) {
            ManualAddContract.Intent.OnBackClicked -> onBack()
            is ManualAddContract.Intent.OnTitleChanged -> update { it.copy(title = intent.value) }
            is ManualAddContract.Intent.OnArtistChanged -> update { it.copy(artist = intent.value) }
            is ManualAddContract.Intent.OnYearChanged -> update { it.copy(year = intent.value) }
            is ManualAddContract.Intent.OnFormatChanged -> update { it.copy(format = intent.format) }
            is ManualAddContract.Intent.OnLabelChanged -> update { it.copy(label = intent.value) }
            is ManualAddContract.Intent.OnCountryChanged -> update { it.copy(country = intent.value) }
            is ManualAddContract.Intent.OnBarcodeChanged -> update { it.copy(barcode = intent.value) }
            is ManualAddContract.Intent.OnNotesChanged -> update { it.copy(notes = intent.value) }
            ManualAddContract.Intent.OnSaveClicked -> onSave()
            ManualAddContract.Intent.OnCameraClicked -> onCameraClicked()
            is ManualAddContract.Intent.OnCameraResult -> onCameraResult(intent.success)
            is ManualAddContract.Intent.OnPhotoPicked -> onPhotoPicked(intent.uri)
            ManualAddContract.Intent.OnCheckPendingPhoto -> onCheckPendingPhoto()
            ManualAddContract.Intent.OnPendingPhotoCleared -> onPendingPhotoCleared()
            is ManualAddContract.Intent.OnPhotoRemoved -> onPhotoRemoved(intent.path)
            ManualAddContract.Intent.OnCropFinished -> onCropFinished()
            ManualAddContract.Intent.OnCropCancelled -> onCropCancelled()
        }
    }

    /**
     * Готовит файл под снимок и просит экран запустить камеру.
     */
    private fun onCameraClicked() {
        if (!_state.value.draft.canAddMorePhotos) return

        val destination = photoStorage.createDestination()
        update { it.copy(pendingPhotoPath = destination.path) }
        viewModelScope.launch {
            _effect.send(ManualAddContract.Effect.LaunchCamera(destination.shareableUri))
        }
    }

    /**
     * Камера вернула управление. При отказе удаляем пустышку,
     * при успехе — отправляем файл в кроп.
     */
    private fun onCameraResult(success: Boolean) {
        val pendingPath = _state.value.pendingPhotoPath

        if (!success || pendingPath == null) {
            pendingPath?.let { photoStorage.delete(it) }
            update { it.copy(pendingPhotoPath = null) }
            return
        }

        if (!photoStorage.isUsable(pendingPath)) {
            // Камера отдала RESULT_OK, но файл пустой или не создан
            photoStorage.delete(pendingPath)
            update { it.copy(pendingPhotoPath = null) }
            return
        }

        update { it.copy(pendingPhotoPath = null) }
        launchCrop(pendingPath)
    }

    private fun onPhotoPicked(uri: String) {
        if (!_state.value.draft.canAddMorePhotos) return

        viewModelScope.launch {
            val imported = photoStorage.import(uri)
            if (imported == null) {
                _effect.send(
                    ManualAddContract.Effect.ShowToast(UiMessage.PhotoImportFailed)
                )
                return@launch
            }
            update { it.copy(pendingCropPath = imported.path) }
            launchCrop(imported.path)
        }
    }

    /**
     * Просит экран открыть UCrop для файла [sourcePath].
     * Файл назначения создаётся заранее — UCrop пишет результат именно в него.
     */
    private fun launchCrop(sourcePath: String) {
        val destination = photoStorage.createDestination()
        update {
            it.copy(
                pendingCropPath = sourcePath,
                pendingCropDestination = destination.path
            )
        }
        viewModelScope.launch {
            _effect.send(
                ManualAddContract.Effect.LaunchCrop(
                    sourceUri = photoStorage.shareableUri(sourcePath),
                    destinationUri = destination.shareableUri
                )
            )
        }
    }

    private fun onCropFinished() {
        val croppedPath = _state.value.pendingCropDestination
        val sourcePath = _state.value.pendingCropPath

        // Исходник после успешного кропа больше не нужен: он весит несколько МБ
        if (sourcePath != null && sourcePath != croppedPath) {
            photoStorage.delete(sourcePath)
        }

        update {
            it.copy(
                photoPaths = if (croppedPath == null) {
                    it.photoPaths
                } else {
                    it.photoPaths + croppedPath
                },
                pendingCropPath = null,
                pendingCropDestination = null
            )
        }
    }

    private fun onCropCancelled() {
        // Файл под результат кропа и исходник больше не нужны
        _state.value.pendingCropDestination?.let { photoStorage.delete(it) }
        _state.value.pendingCropPath?.let { photoStorage.delete(it) }
        update { it.copy(pendingCropPath = null, pendingCropDestination = null) }
    }

    private fun onCheckPendingPhoto() {
        val pendingPath = _state.value.pendingPhotoPath ?: return

        if (photoStorage.isUsable(pendingPath)) {
            update { it.copy(pendingPhotoPath = null) }
            launchCrop(pendingPath)
        } else {
            photoStorage.delete(pendingPath)
            update { it.copy(pendingPhotoPath = null) }
        }
    }

    private fun onPendingPhotoCleared() {
        _state.value.pendingPhotoPath?.let { photoStorage.delete(it) }
        update { it.copy(pendingPhotoPath = null) }
    }

    /**
     * Убирает фото из формы и удаляет его файл — иначе он останется сиротой.
     */
    private fun onPhotoRemoved(path: String) {
        photoStorage.delete(path)
        update { it.copy(photoPaths = it.photoPaths - path) }
    }

    private fun onBack() {
        viewModelScope.launch {
            _effect.send(ManualAddContract.Effect.NavigateBack)
        }
    }

    private fun onSave() {
        val state = _state.value
        val draft = state.draft

        if (!draft.isValid) {
            viewModelScope.launch {
                _effect.send(ManualAddContract.Effect.ShowToast(UiMessage.RequiredFieldsEmpty))
            }
            return
        }

        viewModelScope.launch {
            update { it.copy(isSaving = true, error = null) }

            when (val mode = state.mode) {
                is ManualAddContract.Mode.Create -> {
                    val outcome = addManualItem(draft)
                    when (outcome) {
                        is Outcome.Success -> {
                            update { it.copy(isSaving = false) }
                            _effect.send(ManualAddContract.Effect.NavigateToDetails(itemId = outcome.data))
                        }
                        is Outcome.Failure -> {
                            update { it.copy(isSaving = false, error = outcome.error) }
                            _effect.send(ManualAddContract.Effect.ShowToast(UiMessage.Error(outcome.error)))
                        }
                    }
                }

                is ManualAddContract.Mode.Edit -> {
                    val outcome = updateItem(
                        CollectionItemUpdate(
                            id = mode.itemId,
                            draft = draft,
                            preserveDiscogsImages = state.discogsImages
                        )
                    )
                    when (outcome) {
                        is Outcome.Success -> {
                            update { it.copy(isSaving = false) }
                            _effect.send(ManualAddContract.Effect.NavigateBack)
                        }
                        is Outcome.Failure -> {
                            update { it.copy(isSaving = false, error = outcome.error) }
                            _effect.send(ManualAddContract.Effect.ShowToast(UiMessage.Error(outcome.error)))
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val KEY_INITIALIZED = "initialized"
        private const val KEY_TITLE = "title"
        private const val KEY_ARTIST = "artist"
        private const val KEY_YEAR = "year"
        private const val KEY_FORMAT = "format"
        private const val KEY_LABEL = "label"
        private const val KEY_COUNTRY = "country"
        private const val KEY_BARCODE = "barcode"
        private const val KEY_NOTES = "notes"
        private const val KEY_PHOTO_PATHS = "photo_paths"
        private const val KEY_PENDING_PHOTO_PATH = "pending_photo_path"
        private const val KEY_PENDING_CROP_PATH = "pending_crop_path"
        private const val KEY_PENDING_CROP_DESTINATION = "pending_crop_destination"
        private const val KEY_DISCOGS_IMAGES = "discogs_images"
        private const val SEPARATOR = "|"
    }
}