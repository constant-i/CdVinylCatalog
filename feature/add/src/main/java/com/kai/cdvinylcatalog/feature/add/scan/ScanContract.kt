package com.kai.cdvinylcatalog.feature.add.scan

import com.kai.cdvinylcatalog.common.ui.message.UiMessage
import com.kai.cdvinylcatalog.domain.catalog.Release
import com.kai.cdvinylcatalog.domain.result.AppError

object ScanContract {

    data class State(
        val isLoading: Boolean = false,
        val scannedBarcode: String? = null,
        val error: AppError? = null
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