package com.kai.cdvinylcatalog.feature.add.manual

import com.kai.cdvinylcatalog.common.ui.message.UiMessage
import com.kai.cdvinylcatalog.domain.catalog.Format
import com.kai.cdvinylcatalog.domain.result.AppError

object ManualAddContract {

    data class State(
        val mode: Mode = Mode.Create,
        val title: String = "",
        val artist: String = "",
        val year: String = "",
        val format: Format = Format.CD,
        val label: String = "",
        val country: String = "",
        val barcode: String = "",
        val notes: String = "",
        val isSaving: Boolean = false,
        val error: AppError? = null,
        val photoPaths: List<String> = emptyList(),
        val discogsImages: List<String> = emptyList(),
        val pendingPhotoPath: String? = null,
        val pendingCropPath: String? = null,
        /** Файл, подготовленный под результат кропа. */
        val pendingCropDestination: String? = null,
    ) {
        val canAddMorePhotos: Boolean
            get() = photoPaths.size < MAX_PHOTOS

        val isValid: Boolean
            get() = title.isNotBlank() && artist.isNotBlank()

        val isEditMode: Boolean
            get() = mode is Mode.Edit

        companion object {
            const val MAX_PHOTOS = 5
        }
    }

    sealed class Mode {
        data object Create : Mode()
        data class Edit(val itemId: Long) : Mode()
    }

    sealed class Intent {
        data object OnBackClicked : Intent()
        data class OnTitleChanged(val value: String) : Intent()
        data class OnArtistChanged(val value: String) : Intent()
        data class OnYearChanged(val value: String) : Intent()
        data class OnFormatChanged(val format: Format) : Intent()
        data class OnLabelChanged(val value: String) : Intent()
        data class OnCountryChanged(val value: String) : Intent()
        data class OnBarcodeChanged(val value: String) : Intent()
        data class OnNotesChanged(val value: String) : Intent()
        data object OnSaveClicked : Intent()

        /** Пользователь нажал «Камера». ViewModel готовит файл и отвечает [Effect.LaunchCamera]. */
        data object OnCameraClicked : Intent()

        /** Результат съёмки: `true`, если камера записала файл. */
        data class OnCameraResult(val success: Boolean) : Intent()

        /** Результат системного пикера: строка `content://` выбранного изображения. */
        data class OnPhotoPicked(val uri: String) : Intent()

        data object OnCheckPendingPhoto : Intent()
        data object OnPendingPhotoCleared : Intent()
        data class OnPhotoRemoved(val path: String) : Intent()
        data object OnCropFinished : Intent()
        data object OnCropCancelled : Intent()
    }

    sealed class Effect {
        data object NavigateBack : Effect()
        data class NavigateToDetails(val itemId: Long) : Effect()
        data class ShowToast(val message: UiMessage) : Effect()

        /** Запустить системную камеру; снимок будет записан в [shareableUri]. */
        data class LaunchCamera(val shareableUri: String) : Effect()

        /** Запустить UCrop: прочитать [sourceUri] и записать результат в [destinationUri]. */
        data class LaunchCrop(val sourceUri: String, val destinationUri: String) : Effect()
    }
}