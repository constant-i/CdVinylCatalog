package com.kai.cdvinylcatalog.feature.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kai.cdvinylcatalog.core.model.Format
import com.kai.cdvinylcatalog.core.model.ScanResult
import com.kai.cdvinylcatalog.core.model.parseFormat
import com.kai.cdvinylcatalog.feature.scan.domain.AddToCollectionUseCase
import com.kai.cdvinylcatalog.feature.scan.domain.CheckCollectionUseCase
import com.kai.cdvinylcatalog.feature.scan.domain.SearchReleaseUseCase
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
class ScanViewModel @Inject constructor(
    private val searchRelease: SearchReleaseUseCase,
    private val checkCollection: CheckCollectionUseCase,
    private val addToCollection: AddToCollectionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ScanContract.State())
    val state: StateFlow<ScanContract.State> = _state.asStateFlow()

    private val _effect = Channel<ScanContract.Effect>()
    val effect = _effect.receiveAsFlow()

    private var lastScanTime: Long = 0L

    fun onIntent(intent: ScanContract.Intent) {
        when (intent) {
            is ScanContract.Intent.OnBarcodeScanned -> onBarcodeScanned(intent.barcode)
            ScanContract.Intent.OnScanAgainClicked -> resetState()
            ScanContract.Intent.OnAddToCollectionClicked -> onAddToCollection()
            ScanContract.Intent.OnErrorDismissed -> dismissError()
            is ScanContract.Intent.OnNotesChanged -> onNotesChanged(intent.notes)
        }
    }

    private fun onNotesChanged(notes: String) {
        _state.update { it.copy(userNotes = notes) }
    }

    private fun onBarcodeScanned(barcode: String) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastScanTime < SCAN_DEBOUNCE_MS) return

        val currentState = _state.value
        if (currentState.scannedBarcode == barcode &&
            (currentState.isLoading || currentState.foundRelease != null)) {
            return
        }

        lastScanTime = currentTime

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            when (val result = searchRelease(barcode)) {
                is ScanResult.Found -> {
                    // ПРОВЕРКА ДУБЛИКАТОВ: есть ли уже в коллекции?
                    val isInCollection = checkCollection(result.release.id)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            scannedBarcode = barcode,
                            foundRelease = result.release,
                            isAlreadyInCollection = isInCollection
                        )
                    }
                }
                is ScanResult.NotFound -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            scannedBarcode = barcode,
                            foundRelease = null,
                            error = null
                        )
                    }
                    _effect.send(ScanContract.Effect.ShowToast("Релиз не найден в Discogs"))
                }
                is ScanResult.Error -> {
                    _state.update {
                        it.copy(isLoading = false, error = result.error)
                    }
                }
            }
        }
    }

    private fun onAddToCollection() {
        val release = _state.value.foundRelease ?: return
        val format = parseFormat(release.rawFormat)
        val notes = _state.value.userNotes.ifBlank { null }

        viewModelScope.launch {
            try {
                addToCollection(release, format, notes)
                _state.update { it.copy(justAdded = true) }
            } catch (e: Exception) {
                _effect.send(ScanContract.Effect.ShowToast("Ошибка: ${e.message}"))
            }
        }
    }

    private fun resetState() {
        _state.value = ScanContract.State()
        lastScanTime = 0L
    }

    private fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    companion object {
        private const val SCAN_DEBOUNCE_MS = 2000L
    }
}