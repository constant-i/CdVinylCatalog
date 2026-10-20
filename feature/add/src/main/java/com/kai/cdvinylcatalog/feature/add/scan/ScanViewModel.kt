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

    private var lastBarcode: String? = null
    private var lastScanTime: Long = 0L

    fun onIntent(intent: ScanContract.Intent) {
        when (intent) {
            is ScanContract.Intent.OnBarcodeScanned -> onBarcodeScanned(intent.barcode)
        }
    }

    /**
     * Камера отдаёт один и тот же штрихкод десятки раз в секунду, пока он
     * в кадре, поэтому повтор того же значения в пределах [DEBOUNCE_MS]
     * игнорируется. Другой штрихкод — это другой диск, его пропускаем сразу.
     */
    private fun onBarcodeScanned(barcode: String) {
        val currentTime = System.currentTimeMillis()
        val isSameBarcode = barcode == lastBarcode
        val isWithinDebounce = (currentTime - lastScanTime) < DEBOUNCE_MS

        if (isSameBarcode && isWithinDebounce) return

        lastBarcode = barcode
        lastScanTime = currentTime

        viewModelScope.launch {
            _effect.send(ScanContract.Effect.NavigateToSelection(barcode))
        }
    }

    private companion object {
        const val DEBOUNCE_MS = 2_000L
    }
}
