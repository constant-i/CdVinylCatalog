package com.kai.cdvinylcatalog.feature.scan

import com.kai.cdvinylcatalog.core.model.DiscogsError
import com.kai.cdvinylcatalog.core.model.Release

/**
 * Контракт MVI для экрана сканирования.
 * Содержит State, Intent и Effect.
 */
object ScanContract {

    /**
     * Состояние экрана. Единый источник правды для UI.
     */
    data class State(
        val isLoading: Boolean = false,
        val scannedBarcode: String? = null,
        val foundRelease: Release? = null,
        val isAlreadyInCollection: Boolean = false,
        val justAdded: Boolean = false,
        val error: DiscogsError? = null,
        val userNotes: String = ""
    ) {
        /** Есть ли что показать пользователю */
        val hasResult: Boolean
            get() = foundRelease != null
    }

    /**
     * Действия пользователя.
     */
    sealed class Intent {
        data class OnBarcodeScanned(val barcode: String) : Intent()
        data object OnScanAgainClicked : Intent()
        data object OnAddToCollectionClicked : Intent()
        data object OnErrorDismissed : Intent()
        data class OnNotesChanged(val notes: String) : Intent()
    }

    /**
     * Одноразовые события (навигация, тосты).
     */
    sealed class Effect {
        data class ShowToast(val message: String) : Effect()
        data object NavigateToCollection : Effect()
    }
}