package com.kai.cdvinylcatalog.feature.add.scan

import com.kai.cdvinylcatalog.core.model.Release
import com.kai.cdvinylcatalog.core.model.UiMessage

object ScanContract {

    data class State(
        val isLoading: Boolean = false,
        val scannedBarcode: String? = null,
        val error: String? = null
    )

    sealed class Intent {
        data class OnBarcodeScanned(val barcode: String) : Intent()
        data object OnErrorDismissed : Intent()
    }

    sealed class Effect {
        data class ShowToast(val message: UiMessage) : Effect()
        data class NavigateToSelection(val results: List<Release>) : Effect()
    }
}