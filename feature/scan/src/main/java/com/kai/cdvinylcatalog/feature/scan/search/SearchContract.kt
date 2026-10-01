package com.kai.cdvinylcatalog.feature.scan.search

import com.kai.cdvinylcatalog.core.model.Format
import com.kai.cdvinylcatalog.core.model.Release

/**
 * Контракт MVI для экрана ручного поиска.
 */
object SearchContract {

    data class State(
        val query: String = "",
        val isLoading: Boolean = false,
        val results: List<Release> = emptyList(),
        val hasSearched: Boolean = false,
        val error: String? = null,
        val selectedRelease: Release? = null,
        val selectedFormat: Format = Format.CD,
        val showFormatPicker: Boolean = false,
        val askAddAnother: Release? = null
    ) {
        val isEmpty: Boolean
            get() = !isLoading && hasSearched && results.isEmpty()
    }

    sealed class Intent {
        data class OnQueryChanged(val query: String) : Intent()
        data object OnSearchClicked : Intent()
        data class OnResultClicked(val release: Release) : Intent()
        data class OnFormatSelected(val format: Format) : Intent()
        data object OnFormatPickerDismissed : Intent()
        data object OnConfirmAdd : Intent()
        data object OnBackClicked : Intent()
        data class OnAddAnotherConfirmed(val release: Release) : Intent()
        data object OnAddAnotherDismissed : Intent()
    }

    sealed class Effect {
        data object NavigateBack : Effect()
        data class ShowToast(val message: String) : Effect()
        data class OnReleaseAdded(val releaseTitle: String) : Effect()
        data class ShowAddAnotherDialog(val releaseTitle: String) : Effect()
    }
}