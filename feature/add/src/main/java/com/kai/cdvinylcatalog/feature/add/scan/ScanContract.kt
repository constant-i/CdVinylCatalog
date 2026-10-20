package com.kai.cdvinylcatalog.feature.add.scan

/**
 * Контракт экрана сканирования.
 *
 * У экрана нет состояния: камера и запрос разрешения живут в
 * [CameraPermissionHandler], а результат сканирования сразу уходит навигацией
 * на экран результатов. Поэтому контракт содержит только Intent и Effect.
 *
 * Поиск в Discogs выполняется не здесь, а в
 * [com.kai.cdvinylcatalog.feature.add.selection.ReleaseSelectionViewModel] —
 * вместе с ним сюда переехали и ошибки сети.
 */
object ScanContract {

    sealed class Intent {
        data class OnBarcodeScanned(val barcode: String) : Intent()
    }

    sealed class Effect {
        data class NavigateToSelection(val barcode: String) : Effect()
    }
}
