package com.kai.cdvinylcatalog.feature.add.search

import com.kai.cdvinylcatalog.core.model.Release
import com.kai.cdvinylcatalog.core.model.UiMessage

object SearchContract {

    data class State(
        val query: String = "",
        val isLoading: Boolean = false,
        val error: String? = null
    )

    sealed class Intent {
        data class OnQueryChanged(val query: String) : Intent()
        data object OnSearchClicked : Intent()
        data object OnBackClicked : Intent()
    }

    sealed class Effect {
        data object NavigateBack : Effect()
        data class ShowToast(val message: UiMessage) : Effect()
        data class NavigateToSelection(val results: List<Release>) : Effect()
    }
}