package com.kai.cdvinylcatalog.feature.add.manual

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kai.cdvinylcatalog.core.database.CollectionRepository
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
    savedStateHandle: SavedStateHandle,
    private val repository: CollectionRepository
) : ViewModel() {

    private val editItemId: Long? = savedStateHandle.get<Long>("itemId")?.takeIf { it > 0 }

    private val _state = MutableStateFlow(
        ManualAddContract.State(
            mode = if (editItemId != null) {
                ManualAddContract.Mode.Edit(editItemId)
            } else {
                ManualAddContract.Mode.Create
            }
        )
    )
    val state: StateFlow<ManualAddContract.State> = _state.asStateFlow()

    private val _effect = Channel<ManualAddContract.Effect>()
    val effect = _effect.receiveAsFlow()

    init {
        if (editItemId != null) {
            loadExistingItem(editItemId)
        }
    }

    private fun loadExistingItem(id: Long) {
        viewModelScope.launch {
            val item = repository.getItemById(id)
            if (item != null) {
                _state.update {
                    it.copy(
                        title = item.release.title,
                        artist = item.release.artist,
                        year = item.release.year?.toString() ?: "",
                        format = item.format,
                        label = item.release.label ?: "",
                        country = item.release.country ?: "",
                        barcode = item.release.barcode ?: "",
                        notes = item.notes ?: ""
                    )
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
        }
    }

    private fun update(block: (ManualAddContract.State) -> ManualAddContract.State) {
        _state.update(block)
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
                _effect.send(ManualAddContract.Effect.ShowToast("Заполните обязательные поля"))
            }
            return
        }

        val yearInt = s.year.trim().toIntOrNull()

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            try {
                val itemId = when (val mode = s.mode) {
                    is ManualAddContract.Mode.Create -> {
                        repository.addManualItem(
                            title = s.title.trim(),
                            artist = s.artist.trim(),
                            year = yearInt,
                            format = s.format,
                            label = s.label.ifBlank { null },
                            country = s.country.ifBlank { null },
                            barcode = s.barcode.ifBlank { null },
                            notes = s.notes.ifBlank { null }
                        )
                    }
                    is ManualAddContract.Mode.Edit -> {
                        repository.updateManualItem(
                            id = mode.itemId,
                            title = s.title.trim(),
                            artist = s.artist.trim(),
                            year = yearInt,
                            format = s.format,
                            label = s.label.ifBlank { null },
                            country = s.country.ifBlank { null },
                            barcode = s.barcode.ifBlank { null },
                            notes = s.notes.ifBlank { null }
                        )
                        mode.itemId
                    }
                }
                _state.update { it.copy(isSaving = false) }

                when (s.mode) {
                    is ManualAddContract.Mode.Create -> {
                        _effect.send(ManualAddContract.Effect.NavigateToDetails(itemId))
                    }
                    is ManualAddContract.Mode.Edit -> {
                        _effect.send(ManualAddContract.Effect.NavigateBack)
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, error = e.message) }
                _effect.send(ManualAddContract.Effect.ShowToast("Ошибка: ${e.message}"))
            }
        }
    }
}