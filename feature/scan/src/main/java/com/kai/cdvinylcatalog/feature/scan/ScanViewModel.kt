package com.kai.cdvinylcatalog.feature.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kai.cdvinylcatalog.core.model.ScanResult
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
    private val searchRelease: SearchReleaseUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ScanContract.State())
    val state: StateFlow<ScanContract.State> = _state.asStateFlow()

    private val _effect = Channel<ScanContract.Effect>()
    val effect = _effect.receiveAsFlow()

    /**
     * Единственная точка входа для всех действий пользователя.
     */
    fun onIntent(intent: ScanContract.Intent) {
        when (intent) {
            is ScanContract.Intent.OnBarcodeScanned -> onBarcodeScanned(intent.barcode)
            ScanContract.Intent.OnScanAgainClicked -> resetState()
            ScanContract.Intent.OnAddToCollectionClicked -> onAddToCollection()
            ScanContract.Intent.OnErrorDismissed -> dismissError()
        }
    }

    private fun onBarcodeScanned(barcode: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            when (val result = searchRelease(barcode)) {
                is ScanResult.Found -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            scannedBarcode = barcode,
                            foundRelease = result.release,
                            isAlreadyInCollection = false
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
                is ScanResult.AlreadyInCollection -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            scannedBarcode = barcode,
                            foundRelease = result.release,
                            isAlreadyInCollection = true
                        )
                    }
                    _effect.send(ScanContract.Effect.ShowToast("Этот диск уже есть в коллекции"))
                }
                is ScanResult.Error -> {
                    _state.update {
                        it.copy(isLoading = false, error = result.error)
                    }
                }
            }
        }
    }

    private fun resetState() {
        _state.value = ScanContract.State()
    }

    private fun onAddToCollection() {
        val release = _state.value.foundRelease ?: return
        // TODO: сохранение в Room — реализуем позже
        viewModelScope.launch {
            _effect.send(ScanContract.Effect.ShowToast("Сохранено: ${release.title}"))
        }
    }

    private fun dismissError() {
        _state.update { it.copy(error = null) }
    }
}