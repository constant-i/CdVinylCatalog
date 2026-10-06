package com.kai.cdvinylcatalog.feature.add.details

import com.kai.cdvinylcatalog.core.model.Format
import com.kai.cdvinylcatalog.core.model.Release

object ReleaseDetailsContract {

    data class State(
        val isLoading: Boolean = true,
        val release: Release? = null,
        val isInCollection: Boolean = false,
        val error: String? = null,
        val selectedFormat: Format = Format.CD,
        val showFormatPicker: Boolean = false,
        val askAddAnother: Boolean = false,
        val userNotes: String = "",
        val justAdded: Boolean = false
    )

    sealed class Intent {
        data object OnBackClicked : Intent()
        data object OnAddClicked : Intent()
        data class OnFormatSelected(val format: Format) : Intent()
        data object OnFormatPickerDismissed : Intent()
        data object OnConfirmAdd : Intent()
        data object OnAddAnotherConfirmed : Intent()
        data object OnAddAnotherDismissed : Intent()
        data class OnNotesChanged(val notes: String) : Intent()
    }

    sealed class Effect {
        data class ShowToast(val message: String) : Effect()
        data object NavigateBack : Effect()
        data object NavigateToCollection : Effect()
    }
}