package com.kai.cdvinylcatalog.feature.add.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kai.cdvinylcatalog.common.ui.message.UiMessage
import com.kai.cdvinylcatalog.domain.result.Outcome
import com.kai.cdvinylcatalog.feature.add.domain.SearchReleaseUseCase
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

    private var lastScanTime: Long = 0L

    fun onIntent(intent: ScanContract.Intent) {
        when (intent) {
            is ScanContract.Intent.OnBarcodeScanned -> onBarcodeScanned(intent.barcode)
            ScanContract.Intent.OnErrorDismissed -> dismissError()
        }
    }

    private fun onBarcodeScanned(barcode: String) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastScanTime < SCAN_DEBOUNCE_MS) return

        lastScanTime = currentTime

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            when (val outcome = searchRelease(barcode)) {
                is Outcome.Success -> {
                    _state.update {
                        it.copy(isLoading = false, scannedBarcode = barcode)
                    }
                    if (outcome.data.isEmpty()) {
                        _effect.send(ScanContract.Effect.ShowToast(UiMessage.ReleaseNotFound))
                    } else {
                        _effect.send(ScanContract.Effect.NavigateToSelection(outcome.data))
                    }
                }
                is Outcome.Failure -> {
                    _state.update {
                        it.copy(isLoading = false, error = outcome.error)
                    }
                    _effect.send(ScanContract.Effect.ShowToast(UiMessage.Error(outcome.error)))
                }
            }
        }
    }

    private fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    companion object {
        private const val SCAN_DEBOUNCE_MS = 2000L
    }
}