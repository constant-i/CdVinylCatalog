package com.kai.cdvinylcatalog.feature.add.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor() : ViewModel() {

    private val _effect = Channel<ScanContract.Effect>()
    val effect = _effect.receiveAsFlow()

    private var lastScanTime: Long = 0L

    fun onIntent(intent: ScanContract.Intent) {
        when (intent) {
            is ScanContract.Intent.OnBarcodeScanned -> onBarcodeScanned(intent.barcode)
        }
    }

    private fun onBarcodeScanned(barcode: String) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastScanTime < DEBOUNCE_MS) return
        lastScanTime = currentTime

        viewModelScope.launch {
            _effect.send(ScanContract.Effect.NavigateToSelection(barcode))
        }
    }

    private companion object {
        const val DEBOUNCE_MS = 2_000L
    }
}
