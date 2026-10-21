package com.kai.cdvinylcatalog.feature.add.manual

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kai.cdvinylcatalog.common.ui.message.UiMessage
import com.kai.cdvinylcatalog.domain.catalog.Format
import com.kai.cdvinylcatalog.domain.repository.CollectionRepository
import com.kai.cdvinylcatalog.domain.result.Outcome
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
    private val repository: CollectionRepository
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
            format = savedStateHandle.get<String>(KEY_FORMAT)
                ?.let { runCatching { Format.valueOf(it) }.getOrNull() }
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
            pendingCropPath = savedStateHandle.get<String>(KEY_PENDING_CROP_PATH)
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
            when (val outcome = repository.getItemById(id)) {
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
            is ManualAddContract.Intent.OnPhotoAdded -> onPhotoAdded(intent.path)
            is ManualAddContract.Intent.OnPhotoRemoved -> onPhotoRemoved(intent.path)
            is ManualAddContract.Intent.OnCameraLaunched -> onCameraLaunched(intent.pendingPath)
            ManualAddContract.Intent.OnCheckPendingPhoto -> onCheckPendingPhoto()
            ManualAddContract.Intent.OnPendingPhotoCleared -> onPendingPhotoCleared()
            is ManualAddContract.Intent.OnStartCrop -> onStartCrop(intent.imagePath)
            is ManualAddContract.Intent.OnCropFinished -> onCropFinished(intent.croppedPath)
            ManualAddContract.Intent.OnCropCancelled -> onCropCancelled()

        }
    }

    private fun onStartCrop(imagePath: String) {
        update {
            it.copy(
                pendingPhotoPath = null,
                pendingCropPath = imagePath
            )
        }
    }

    private fun onCropFinished(croppedPath: String) {
        update {
            it.copy(
                photoPaths = it.photoPaths + croppedPath,
                pendingCropPath = null
            )
        }
    }

    private fun onCropCancelled() {
        update { it.copy(pendingCropPath = null) }
    }

    private fun onCameraLaunched(pendingPath: String) {
        update { it.copy(pendingPhotoPath = pendingPath) }
    }

    private fun onCheckPendingPhoto() {
        val pendingPath = _state.value.pendingPhotoPath ?: return
        val file = java.io.File(pendingPath)

        if (file.exists() && file.length() > 0) {
            // Файл успешно сохранён камерой — запускаем обрезку
            update {
                it.copy(
                    pendingPhotoPath = null,
                    pendingCropPath = pendingPath
                )
            }
        } else {
            // Файл не сохранился — сбрасываем
            update { it.copy(pendingPhotoPath = null) }
        }
    }

    private fun onPendingPhotoCleared() {
        update { it.copy(pendingPhotoPath = null) }
    }
    private fun onPhotoAdded(path: String) {
        update {
            if (it.photoPaths.size >= ManualAddContract.State.MAX_PHOTOS) {
                it
            } else {
                it.copy(photoPaths = it.photoPaths + path)
            }
        }
    }

    private fun onPhotoRemoved(path: String) {
        update { it.copy(photoPaths = it.photoPaths - path) }
    }

    private fun onBack() {
        viewModelScope.launch {
            _effect.send(ManualAddContract.Effect.NavigateBack)
        }
    }

    private fun onSave() {
        val s = _state.value
        if (!s.isValid) {
            viewModelScope.launch {
                _effect.send(ManualAddContract.Effect.ShowToast(UiMessage.RequiredFieldsEmpty))
            }
            return
        }

        val yearInt = s.year.trim().toIntOrNull()?.takeIf { it > 0 }

        viewModelScope.launch {
            update { it.copy(isSaving = true, error = null) }

            when (val mode = s.mode) {
                is ManualAddContract.Mode.Create -> {
                    val outcome = repository.addManualItem(
                        title = s.title.trim(),
                        artist = s.artist.trim(),
                        year = yearInt,
                        format = s.format,
                        label = s.label.ifBlank { null },
                        country = s.country.ifBlank { null },
                        barcode = s.barcode.ifBlank { null },
                        notes = s.notes.ifBlank { null },
                        photoPaths = s.photoPaths
                    )
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
                    val outcome = repository.updateItem(
                        id = mode.itemId,
                        title = s.title.trim(),
                        artist = s.artist.trim(),
                        year = yearInt,
                        format = s.format,
                        label = s.label.ifBlank { null },
                        country = s.country.ifBlank { null },
                        barcode = s.barcode.ifBlank { null },
                        notes = s.notes.ifBlank { null },
                        photoPaths = s.photoPaths,
                        preserveDiscogsImages = s.discogsImages
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
        private const val KEY_DISCOGS_IMAGES = "discogs_images"
        private const val SEPARATOR = "|"
    }
}