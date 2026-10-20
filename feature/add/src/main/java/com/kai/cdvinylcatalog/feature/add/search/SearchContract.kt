package com.kai.cdvinylcatalog.feature.add.search

object SearchContract {
    data class State(
        val query: String = "",
    )

    sealed class Intent {
        data class OnQueryChanged(val query: String) : Intent()
        data object OnSearchClicked : Intent()
        data object OnBackClicked : Intent()
    }

    sealed class Effect {
        data object NavigateBack : Effect()
        data class NavigateToSelection(val query: String) : Effect()
    }
}