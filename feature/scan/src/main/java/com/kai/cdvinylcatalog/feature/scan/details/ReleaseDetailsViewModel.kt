package com.kai.cdvinylcatalog.feature.scan.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kai.cdvinylcatalog.core.model.Format
import com.kai.cdvinylcatalog.core.model.parseFormat
import com.kai.cdvinylcatalog.core.network.DiscogsRepository
import com.kai.cdvinylcatalog.feature.scan.domain.AddToCollectionUseCase
import com.kai.cdvinylcatalog.feature.scan.domain.CheckCollectionUseCase
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
class ReleaseDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: DiscogsRepository,
    private val checkCollection: CheckCollectionUseCase,
    private val addToCollection: AddToCollectionUseCase
) : ViewModel() {

    private val releaseId: Long = savedStateHandle.get<Long>("releaseId") ?: -1L

    private val _state = MutableStateFlow(ReleaseDetailsContract.State())
    val state: StateFlow<ReleaseDetailsContract.State> = _state.asStateFlow()

    private val _effect = Channel<ReleaseDetailsContract.Effect>()
    val effect = _effect.receiveAsFlow()

    init {
        loadDetails()
    }

    private fun loadDetails() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            repository.getReleaseDetails(releaseId)
                .onSuccess { release ->
                    val inCollection = checkCollection(release.id)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            release = release,
                            isInCollection = inCollection
                        )
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(isLoading = false, error = e.message)
                    }
                }
        }
    }

    fun onIntent(intent: ReleaseDetailsContract.Intent) {
        when (intent) {
            ReleaseDetailsContract.Intent.OnBackClicked -> onBack()
            ReleaseDetailsContract.Intent.OnAddClicked -> onAddClicked()
            is ReleaseDetailsContract.Intent.OnFormatSelected -> onFormatSelected(intent.format)
            ReleaseDetailsContract.Intent.OnFormatPickerDismissed -> onFormatPickerDismissed()
            ReleaseDetailsContract.Intent.OnConfirmAdd -> onConfirmAdd()
            ReleaseDetailsContract.Intent.OnAddAnotherConfirmed -> onAddAnotherConfirmed()
            ReleaseDetailsContract.Intent.OnAddAnotherDismissed -> onAddAnotherDismissed()
            is ReleaseDetailsContract.Intent.OnNotesChanged -> onNotesChanged(intent.notes)
        }
    }

    private fun onBack() {
        viewModelScope.launch {
            _effect.send(ReleaseDetailsContract.Effect.NavigateBack)
        }
    }

    private fun onAddClicked() {
        val release = _state.value.release ?: return
        if (_state.value.isInCollection) {
            _state.update { it.copy(askAddAnother = true) }
            return
        }

        val detectedFormat = parseFormat(release.rawFormat)
        if (detectedFormat != Format.UNKNOWN) {
            addRelease(release, detectedFormat)
        } else {
            _state.update {
                it.copy(selectedFormat = Format.CD, showFormatPicker = true)
            }
        }
    }

    private fun onFormatSelected(format: Format) {
        _state.update { it.copy(selectedFormat = format) }
    }

    private fun onFormatPickerDismissed() {
        _state.update { it.copy(showFormatPicker = false) }
    }

    private fun onConfirmAdd() {
        val release = _state.value.release ?: return
        val format = _state.value.selectedFormat
        _state.update { it.copy(showFormatPicker = false) }
        addRelease(release, format)
    }

    private fun onAddAnotherConfirmed() {
        val release = _state.value.release ?: return
        _state.update { it.copy(askAddAnother = false) }
        val detectedFormat = parseFormat(release.rawFormat).takeIf { it != Format.UNKNOWN }
            ?: _state.value.selectedFormat
        addRelease(release, detectedFormat)
    }

    private fun onAddAnotherDismissed() {
        _state.update { it.copy(askAddAnother = false) }
    }

    private fun onNotesChanged(notes: String) {
        _state.update { it.copy(userNotes = notes) }
    }

    private fun addRelease(release: com.kai.cdvinylcatalog.core.model.Release, format: Format) {
        val notes = _state.value.userNotes.ifBlank { null }
        viewModelScope.launch {
            try {
                addToCollection(release, format, notes)
                _state.update { it.copy(justAdded = true, isInCollection = true) }
                _effect.send(ReleaseDetailsContract.Effect.ShowToast("Добавлено: ${release.title}"))
                _effect.send(ReleaseDetailsContract.Effect.NavigateToCollection)
            } catch (e: Exception) {
                _effect.send(ReleaseDetailsContract.Effect.ShowToast("Ошибка: ${e.message}"))
            }
        }
    }
}